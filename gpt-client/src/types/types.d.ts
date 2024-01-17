/**
 * 实体类型说明
 */

import { Ref } from 'vue';

interface IQjInjectTable {
  isLoading: Ref<boolean>;
  currentPage: Ref<number>;
  pageSize: Ref<number>;
  totalCount: Ref<number>;
  onCurrentChange: (val: number, pushData?: boolean) => void;
  onSizeChange: (val: number) => void;
  refresh: (params?: any) => void;
}
