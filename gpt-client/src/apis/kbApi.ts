import { get, post } from '@/utils/http';
/**
 * 知识库接口
 */

const KbApi = {
  list: (searchValue: string) => get('/knowledgeBase/list', { searchValue }),
  addOrUpdate: (params: { id?: number; companyCode: string; companyName: string; kbName: string; tags: string }) => post('/knowledgeBase/addOrUpdate', params),
  remove: (id: string) => get('/knowledgeBase/remove', { id }),
  detail: (id: string) => get('/knowledgeBase/detail', { id })
};

export default KbApi;
