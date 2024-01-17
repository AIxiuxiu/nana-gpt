import { get, post } from '@/utils/http';
/**
 * 聊天接口
 */
const ChatApi = {
  history: (kbId: number | string) => get('/chat/history', { kbId }),
  cleanHistory: (kbId: number | string) => get('/chat/cleanHistory', { kbId }),
  saveChat: (params: { kbId: number | string; role: string; content: string; tokens: number; prompt: string }) => post('/chat/saveChat', params),
  subscription: () => get('/chat/subscription'),
  billingUsage: (startDate?: string, endDate?: string) => get('/chat/billingUsage', { startDate, endDate })
};

export default ChatApi;
