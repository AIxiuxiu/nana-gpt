import { get, post } from '@/utils/http';
/**
 * 问题接口
 */
const InteractQuestionApi = {
  list: (params: { interactId: number; page: number; pageSize: number; searchValue?: string }) => get('/interactQuestion/list', params),
  addOrUpdate: (params: { question: string; id?: number | string; contentId?: number | string }) => post('/interactQuestion/addOrUpdate', params),
  remove: (id: string) => get('/interactQuestion/remove', { id }),
  export: (params: { interactId: string; fileName?: string }) => get('/interactQuestion/export', params)
};

export default InteractQuestionApi;
