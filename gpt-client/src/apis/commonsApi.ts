import { get } from '@/utils/http';
/**
 * 公共相关接口
 */

const CommonsApi = {
  getDictionary: (type: string) => get('/commons/dictionary', { type }),
  getMenu: (menuId: number) => get('/menu/getMenu', { menuId })
};

export default CommonsApi;
