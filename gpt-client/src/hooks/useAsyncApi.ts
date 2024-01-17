import { InjectTabkeKey } from '@/types/symbols';
import { noop, promiseTimeout } from '@vueuse/shared';
import { provide, ref, shallowRef } from 'vue';

export interface AsyncApiOptions<R> {
  /**
   * 默认值， 可配合 resetOnExecute使用
   */
  initialData?: R[];
  /**
   * 延迟执行
   * @default 0
   */
  delay?: number;
  /**
   * 函数被调用后立即执行, 当设置为 false 时，您将需要手动执行它 refresh()
   * @default true
   */
  immediate?: boolean;

  /**
   * 执行承诺之前将返回值data设置为 initialState
   * @default true
   */
  resetOnExecute?: boolean;
  /**
   * 当返回结果为空时，更新为初始值 initialData
   * @default true
   */
  nodataReset?: boolean;

  /**
   * 最大返回总数
   */
  maxTotal?: number;
  /**
   * 赋值回调
   */
  onFilter?: (e: R) => R;
  /**
   * 成功回调
   */
  onSuccess?: (e: R) => void;
  /**
   * 错误回调
   */
  onError?: (e: unknown) => void;

  /**
   * 数据list的 prop, 接口对应的页码key 默认 records
   */
  listProp?: string;
}

export function useAsyncApi<R, Args = any>(apiFun: (args: Args) => Promise<R>, options?: AsyncApiOptions<R>) {
  const { immediate = true, delay = 0, onError = noop, onSuccess = noop, resetOnExecute = false, nodataReset = true, initialData, onFilter, maxTotal = 0 } = options || {};

  const data = shallowRef(initialData);
  const isNoData = ref(false);
  const isLoading = ref(false);
  const isReady = ref(false);
  const error = ref<unknown | undefined>(undefined);

  async function refresh(newParams?: any) {
    if (resetOnExecute) data.value = initialData;
    error.value = undefined;
    isReady.value = false;
    isLoading.value = true;
    isNoData.value = false;
    if (delay > 0) await promiseTimeout(delay);

    try {
      let result: any = await apiFun({ ...newParams });
      if (!result) {
        isNoData.value = true;
        if (nodataReset) {
          result = initialData;
        }
      }
      if (onFilter) {
        result = onFilter(result);
      }

      data.value = result;

      isReady.value = true;
      onSuccess(result.data);
    } catch (e) {
      error.value = e;
      onError(e);
    }
    isLoading.value = false;
  }

  if (immediate) refresh();

  return {
    // 返回值
    data,
    isLoading,
    isReady,
    isNoData,
    error,
    refresh
  };
}

/**
 * 使用异步页码Api请求
 * @param promise 接口方法
 * @param options AsyncApiOptions
 * @returns
 */
export function useAsyncPageApi<R, Args = any>(apiFun: (args: Args) => Promise<R>, options?: AsyncApiOptions<R>) {
  const {
    immediate = true,
    delay = 0,
    onError = noop,
    onSuccess = noop,
    resetOnExecute = false,
    nodataReset = true,
    initialData = [],
    onFilter,
    maxTotal = 0,
    listProp = 'records'
  } = options || {};

  const total = ref(0);
  const data = shallowRef(initialData);
  const isNoData = ref(false);
  const isLoading = ref(false);
  const isReady = ref(false);
  const error = ref<unknown | undefined>(undefined);

  async function refresh(newParams?: any, pushData?: boolean) {
    if (resetOnExecute) data.value = initialData;
    error.value = undefined;
    isReady.value = false;
    isLoading.value = true;
    isNoData.value = false;
    if (delay > 0) await promiseTimeout(delay);

    try {
      let result: any = await apiFun({ ...newParams });
      if (!result || !result[listProp]) {
        isNoData.value = true;
        if (nodataReset) {
          result = { ...result, [listProp]: initialData };
        }
      }
      if (onFilter) {
        result[listProp] = onFilter(result[listProp]);
      }

      if (pushData) {
        data.value.push(...result[listProp]);
      } else {
        data.value = result[listProp];
      }

      maxTotal && result.total > maxTotal && (result.total = maxTotal);
      total.value = result.total;
      isReady.value = true;
      onSuccess(result[listProp]);
    } catch (e) {
      error.value = e;
      onError(e);
    }
    isLoading.value = false;
  }

  if (immediate) refresh();

  return {
    // 总数，列表时使用
    total,
    // 返回值
    data,
    isLoading,
    isReady,
    isNoData,
    error,
    refresh
  };
}

export interface AsyncTableApiOptions<R> extends AsyncApiOptions<R> {
  /**
   * id, 用于区分不同的列表
   */
  id?: string;
  /**
   * 每页数量pageSize, 默认是10
   */
  pageSize?: number;
  /**
   * pageSize的 prop, 接口对应的每页数量key 默认 pageSize
   */
  pageSizeProp?: string;
  /**
   * currentPage的 prop, 接口对应的页码key 默认 page
   */
  currentPageProp?: string;

  /**
   * 数据list的 prop, 接口对应的页码key 默认 records
   */
  listProp?: string;
}

// eslint-disable-next-line @typescript-eslint/no-unnecessary-type-constraint
export function useAsyncTableApi<R, Args extends any = any>(apiFun: (args: Args) => Promise<R>, options?: AsyncTableApiOptions<R>) {
  const { id = '', immediate = true, pageSize = 10, pageSizeProp = 'pageSize', currentPageProp = 'page', listProp = 'records' } = (options = options || {});
  const oldImmediate = immediate;
  if (immediate) {
    options.immediate = false;
  }
  // 页码重置
  let pageInitFlag = true;
  const oldOnSuccess = options.onSuccess;
  options.onSuccess = (e) => {
    pageInitFlag = false;
    oldOnSuccess && oldOnSuccess(e);
  };
  const asyncApi = useAsyncPageApi(apiFun, options);

  const pageSizeRef = ref(pageSize);
  const currentPage = ref(1);
  const oldRefresh = asyncApi.refresh;
  let tempParams;

  // 分页序号
  const indexMethod = (index: number) => {
    return (currentPage.value - 1) * pageSizeRef.value + index + 1;
  };

  const newRefresh = (pageFlag?: boolean, pushData?: boolean) => {
    pageInitFlag = pageFlag;
    if (pageInitFlag) {
      currentPage.value = 1;
    }
    return oldRefresh(Object.assign(tempParams || {}, { [currentPageProp]: currentPage.value, [pageSizeProp]: pageSizeRef.value }), pushData);
  };

  const onCurrentChange = (currentPage, pushData?: boolean) => {
    // 兼容无数据，页码改变重复请求
    if (pageInitFlag || asyncApi.total.value == 0) {
      return;
    }
    newRefresh(false, pushData);
  };
  const onSizeChange = () => {
    newRefresh(true);
  };
  // 覆写 refresh方法，合并page参数
  asyncApi.refresh = (newParams?: any) => {
    let pageInitFlag = true;
    if (newParams && Object.prototype.hasOwnProperty.call(newParams, 'pageInitFlag')) {
      pageInitFlag = newParams.pageInitFlag;
      delete newParams.pageInitFlag;
    }
    tempParams = newParams;
    return newRefresh(pageInitFlag);
  };
  // 判断是否立即执行
  oldImmediate && asyncApi.refresh();

  provide(InjectTabkeKey(id), {
    isLoading: asyncApi.isLoading,
    pageSize: pageSizeRef,
    currentPage,
    onCurrentChange,
    onSizeChange,
    totalCount: asyncApi.total,
    refresh: asyncApi.refresh
  });

  return { ...asyncApi, pageSize: pageSizeRef, currentPage, indexMethod };
}

export type UseAsyncApiReturn = ReturnType<typeof useAsyncApi>;
