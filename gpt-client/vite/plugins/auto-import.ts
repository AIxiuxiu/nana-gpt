/**
 * 按需加载，自动引入
 */
import AutoImport from 'unplugin-auto-import/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';

export default function autoImportDeps() {
  return AutoImport({
    resolvers: [ElementPlusResolver()],
    dts: 'types/auto-imports.d.ts',
    imports: [
      'vue',
      'pinia',
      'vue-router',
      {
        '@vueuse/core': []
      }
    ]
  });
}
