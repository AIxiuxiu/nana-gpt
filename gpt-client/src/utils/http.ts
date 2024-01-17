import { clearToken, getToken } from '@/utils/auth';
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import router from '../router';
import { TokenPrefix } from './auth';

// 如果请求超过 `timeout` 的时间，请求将被中断
axios.defaults.timeout = 30000;
// 表示跨域请求时是否需要使用凭证
axios.defaults.withCredentials = false;
axios.defaults.headers.common['Content-Type'] = 'application/json;charset=UTF-8';
// 默认 token
// axios.defaults.headers.common['token'] =  AUTH_TOKEN
// 允许跨域
// axios.defaults.headers.post['Access-Control-Allow-Origin-Type'] = '*';

const axiosInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_OPEN_PROXY === 'true' ? '/proxy/' : import.meta.env.VITE_APP_API_BASEURL?.toString()
});

// axios实例拦截请求
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers.token = `${TokenPrefix}${token}`;
    }
    return config;
  },
  (error: any) => {
    return Promise.reject(error);
  }
);

let showLoginInfo = false;
// axios实例拦截响应
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.status === 200) {
      // 未设置状态码则默认成功状态
      const code = response.data.code || 0;
      if (code == 0) {
        return Promise.resolve(response);
      } else if (code == 403) {
        if (!showLoginInfo) {
          showLoginInfo = true;
          ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          })
            .then(() => {
              showLoginInfo = false;
              clearToken();
              const fullPath = encodeURIComponent(router.currentRoute.value.fullPath);
              // encodeURIComponent 转换uri编码，防止解析地址出问题
              router.push('/login?redirectUrl=' + fullPath);
            })
            .catch(() => {
              showLoginInfo = false;
            });
        }
        return Promise.reject(response);
      } else if (response.data && response.data.msg && response.data.msg != '') {
        ElMessage({
          message: response.data.msg,
          grouping: true,
          type: 'error'
        });
        return Promise.reject(response);
      }
    } else {
      showMessage(response.status);
    }
    return Promise.reject(response);
  },
  // 请求失败
  (error: any) => {
    const { response } = error;
    if (response) {
      // 请求已发出，但是不在2xx的范围
      showMessage(response.status);
      return Promise.reject(response.data);
    }
    showMessage('网络连接异常,请稍后再试!');
  }
);

const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
  const conf = config;
  return new Promise((resolve, reject) => {
    axiosInstance
      .request<any, AxiosResponse<QjResponse>>(conf)
      .then((res: AxiosResponse<QjResponse>) => {
        // resolve(res as unknown as Promise<T>);
        const {
          data: { data }
        } = res;
        resolve(data as T);
      })
      .catch((res: AxiosResponse<QjResponse>) => {
        reject(res);
      });
  });
};

export function get<T = any>(url: string, params?: object, config?: AxiosRequestConfig): Promise<T> {
  return request({ url, params, ...config, method: 'GET' });
}

export function post<T = any>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
  return request({ url, data, ...config, method: 'POST' });
}

export function upload<T = any>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
  config = Object.assign({}, config, {
    timeout: 180000,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  return request({ url, data, ...config, method: 'POST' });
}

export default request;

const showMessage = (status: number | string) => {
  let message = '';
  switch (status) {
    case 400:
      message = '请求错误(400)';
      break;
    case 401:
      message = '未授权，请重新登录(401)';
      break;
    case 403:
      message = '拒绝访问(403)';
      break;
    case 404:
      message = '请求出错(404)';
      break;
    case 408:
      message = '请求超时(408)';
      break;
    case 500:
      message = '服务器错误(500)';
      break;
    case 501:
      message = '服务未实现(501)';
      break;
    case 502:
      message = '网络错误(502)';
      break;
    case 503:
      message = '服务不可用(503)';
      break;
    case 504:
      message = '网络超时(504)';
      break;
    case 505:
      message = 'HTTP版本不受支持(505)';
      break;
    default:
      message = `连接出错(${status})!`;
  }
  ElMessage({
    message: `${message}，请检查网络或联系管理员！`,
    grouping: true,
    type: 'error'
  });
};

export interface QjResponse<T = any> {
  [x: string]: any;
  code?: number;
  msg?: string;
  total?: number;
  data?: T;
}
