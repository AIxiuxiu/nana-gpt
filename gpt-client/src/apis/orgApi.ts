import { get } from '@/utils/http';
/**
 * 公司接口
 */
const OrgApi = {
  searchListed: (data: { searchValue: string; num: number | string }) => get('/org/searchListed', data)
};

export default OrgApi;
