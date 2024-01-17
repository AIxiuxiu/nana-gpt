import { get, post } from '@/utils/http';
/**
 * 文档内容接口
 */
const DocContentsApi = {
  list: (params: { docId: number; page: number; pageSize: number; searchValue?: string }) => get('/docContents/list', params),
  addOrUpdate: (params: { content: string; id?: number | string; docId?: number | string }) => post('/docContents/addOrUpdate', params),
  remove: (id: string) => get('/docContents/remove', { id }),
  embedding: (docId: string) => get('/docContents/embedding', { docId }),
  embeddingStatus: (docId: string) => get('/docContents/embeddingStatus', { docId }),
  summary: (docId: string) => get('/docContents/summary', { docId }),
  summaryStatus: (docId: string) => get('/docContents/summaryStatus', { docId }),
  createQA: (docId: string) => get('/docContents/createQA', { docId }),
  qaStatus: (docId: string) => get('/docContents/qaStatus', { docId })
};

export default DocContentsApi;
