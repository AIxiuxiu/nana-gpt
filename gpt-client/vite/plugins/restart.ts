import ViteRestart from 'vite-plugin-restart';

/**
 * 监听文件修改，自动重启 vite 服务
 */
export default function viteRestartConfig() {
  return ViteRestart({
    restart: ['**/.env.*', '**/vite.config.js', '**/vite/plugins/*.ts']
  });
}
