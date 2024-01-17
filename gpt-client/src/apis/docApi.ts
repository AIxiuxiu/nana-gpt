import { get, post, upload } from '@/utils/http';
/**
 * 文档接口
 */
const DocApi = {
  list: (params: { kbId: number; page: number; pageSize: number; searchValue?: string }) => get('/document/list', params),
  uploadDoc: (data: FormData, config) => upload<any>('/document/uploadDoc', data, config),
  update: (params: { id: number; docName: string; tags: string; summary: string }) => post('/document/update', params),
  remove: (id: string) => get('/document/remove', { id })
};

export default DocApi;
