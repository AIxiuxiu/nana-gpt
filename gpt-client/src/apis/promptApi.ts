import { get, post } from '@/utils/http';
/**
 * prompt接口
 */
const PromptApi = {
  list: (target) => get('/prompt/list', { target }),
  update: (params: { content: string; id: number | string }) => post('/prompt/update', params),
  add: (params: { content: string; topic: number | string }) => post('/prompt/add', params),
  remove: (id: number | string) => get('/prompt/remove', { id })
};

export default PromptApi;
