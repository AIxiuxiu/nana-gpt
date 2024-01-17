/**
 * 动态路由-布局文件
 */
import Layouts from 'vite-plugin-vue-layouts';

export default function viteLayoutPlugin() {
  return Layouts({
    layoutsDirs: 'src/layout', // 布局文件存放目录
    exclude: ['**/components/*.vue'],
    defaultLayout: 'index' // 默认布局，对应 src/layout/index.vue
  });
}
