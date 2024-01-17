import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import { useAppStore } from './modules/app';
import { useUserStore } from './modules/user';
import { useWSStore } from './modules/webscoket';

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);

export { useAppStore, useUserStore, useWSStore };
export default pinia;
