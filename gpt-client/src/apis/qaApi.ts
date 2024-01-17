import { get, post } from '@/utils/http';
/**
 * 问答对接口
 */
const QaApi = {
  list: (params: { docId: number; page: number; pageSize: number; searchValue?: string }) => get('/qa/list', params),
  addOrUpdate: (params: { question: string; answer: string; id?: number | string; contentId?: number | string }) => post('/qa/addOrUpdate', params),
  remove: (id: string) => get('/qa/remove', { id }),
  export: (params: { docId: string; fileName?: string }) => get('/qa/export', params)
};

export default QaApi;
