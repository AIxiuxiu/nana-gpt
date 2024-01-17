import { get, post } from '@/utils/http';
/**
 * 知识库设置接口
 */

const KbSettingApi = {
  detail: (kbId: string) => get('/kbSetting/detail', { kbId }),
  update: (params: any) => post('/kbSetting/update', params)
};

export default KbSettingApi;
