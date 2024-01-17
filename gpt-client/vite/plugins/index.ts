import vue from '@vitejs/plugin-vue';
import vueSetupExtend from 'vite-plugin-vue-setup-extend';
import autoImportDeps from './auto-import';
import autoRegistryComponents from './components';
import createCompression from './compression';
import createIcon from './icons';
// import viteImageminPlugin from './imagemin';
import viteLayoutPlugin from './layout';
import vitePagesPlugin from './pages';
import { viteProgressPlugin } from './progress';
import viteRestartConfig from './restart';
import htmlVersion from './version';
// import { visualizerConfig } from './visualizer';

/**
 * vite 插件
 */
export default function createVitePlugins(viteEnv: any, isBuild = false) {
  const vitePlugins: any[] = [
    // vue支持
    vue(),
    // setup语法糖组件名支持
    vueSetupExtend()
  ];

  // 自动按需引入依赖
  vitePlugins.push(autoImportDeps());

  // 自动注册组件
  vitePlugins.push(autoRegistryComponents());

  // html版本注入
  vitePlugins.push(htmlVersion());

  // iconify图标
  vitePlugins.push(createIcon());

  // 构建时显示进度条
  vitePlugins.push(viteProgressPlugin());

  // rollup-plugin-visualizer
  // vitePlugins.push(visualizerConfig(viteEnv));

  // 自动生成路由
  vitePlugins.push(vitePagesPlugin());
  // 路由-布局
  vitePlugins.push(viteLayoutPlugin());

  if (isBuild) {
    vitePlugins.push(...createCompression(viteEnv));
    // vitePlugins.push(viteImageminPlugin());
  } else {
    // 监听配置文件改动重启
    vitePlugins.push(viteRestartConfig());
  }

  return vitePlugins;
}
