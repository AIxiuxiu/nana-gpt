import { defineStore } from 'pinia';
import { AppState } from './types';

export const useAppStore = defineStore(
  // 唯一ID
  'app',
  {
    state: () => ({
      wow: undefined
    }),
    getters: {},
    actions: {
      setAppInfo(partial: Partial<AppState>) {
        this.$patch(partial);
      }
    },
    // 持久化
    persist: {
      key: `${import.meta.env.VITE_APP_STORE_PREFIX}_app`,
      storage: localStorage
    }
  }
);
