import { get, post } from '@/utils/http';
/**
 * 互动问答接口
 */
const InteractApi = {
  list: (params: { page: number; pageSize: number; searchValue?: string }) => get('/interact/list', params),
  add: (data: { orgId: string; startDate: string; endDate: string; maxNum: number; qprompt: string; isCore: number; qaOptimize: number; optimizePrompt: string }) =>
    post<any>('/interact/add', data),
  update: (data: { id: string; qprompt: string }) => post('/interact/update', data),
  remove: (id: string) => get('/interact/remove', { id })
};

export default InteractApi;
