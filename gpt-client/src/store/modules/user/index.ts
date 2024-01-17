import UserApi from '@/apis/userApi';

import { clearToken, setToken } from '@/utils/auth';
import { defineStore } from 'pinia';
import { useWSStore } from '../webscoket';
import { UserState } from './types';

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: undefined,
    username: undefined,
    token: undefined,
    expireTime: 0
  }),
  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
    isLogin(state: UserState): boolean {
      if (state.token && state.token.length > 0 && state.expireTime && state.expireTime > new Date().getTime() / 1000) {
        return true;
      }
      this.resetInfo();
      return false;
    },
    webSocket() {
      const ws = useWSStore();
      return ws;
    }
  },
  actions: {
    // 设置用户的信息
    setInfo(userInfo: Partial<UserState>) {
      // 过期时间24小时
      userInfo.expireTime = Date.now() / 1000 + 23 * 60 * 60;
      this.$patch(userInfo);
    },
    // 设置完成
    setComplete(complete: boolean) {
      this.complete = complete;
    },
    // 重置用户信息
    resetInfo() {
      this.$reset();
    },
    // 异步登录并存储token
    async login(loginForm: { username: string; password: string }) {
      const result = await UserApi.login(loginForm);
      this.setInfo(result);
      const token = result?.token;
      if (token) {
        setToken(token);
      }
      this.webSocket.connectWs();
      return result;
    },
    // Logout
    async logout() {
      this.resetInfo();
      clearToken();
      this.webSocket.closeWs();
    }
  },
  // 持久化
  persist: {
    key: `${import.meta.env.VITE_APP_STORE_PREFIX}_user`,
    storage: localStorage
  }
});
