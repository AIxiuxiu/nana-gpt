import { get, post } from '@/utils/http';
/**
 * 互动问答内容内容接口
 */
const InteractContentsApi = {
  list: (params: { interactId: number; page: number; pageSize: number; searchValue?: string }) => get('/interactContents/list', params),
  addOrUpdate: (params: { content: string; id?: number | string; interactId?: number | string }) => post('/interactContents/addOrUpdate', params),
  remove: (id: string) => get('/interactContents/remove', { id }),
  detail: (id: string) => get('/interactContents/detail', { id }),
  createQuestion: (interactId: string) => get('/interactContents/createQuestion', { interactId }),
  questionStatus: (interactId: string) => get('/interactContents/questionStatus', { interactId })
};

export default InteractContentsApi;
