import { get, post } from '@/utils/http';
/**
 * 用户相关接口
 */

const UserApi = {
  login: (data: { username: string; password: string }) => post<any>('/user/login', data),
  setting: () => get('/user/setting'),
  update: (params: any) => post('/user/update', params)
};

export default UserApi;
