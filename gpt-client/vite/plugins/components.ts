/**
 * 按需加载，自动引入组件
 */
import IconsResolver from 'unplugin-icons/resolver';
import { ElementPlusResolver, VueUseComponentsResolver } from 'unplugin-vue-components/resolvers';
import Components from 'unplugin-vue-components/vite';

export default function autoRegistryComponents() {
  return Components({
    // dirs: ['src/components'],
    globs: ['**/src/components/**/index.vue'],
    extensions: ['vue'],
    deep: true,
    dts: 'types/components.d.ts',
    directoryAsNamespace: false,
    globalNamespaces: [],
    directives: true,
    importPathTransform: (v) => v,
    allowOverrides: false,
    include: [/\.vue$/, /\.vue\?vue/, /\.vue\?v=/],
    exclude: [/[\\/]node_modules[\\/]/, /[\\/]\.git[\\/]/, /[\\/]\.nuxt[\\/]/],
    resolvers: [
      // 自动导入 Element Plus 组件
      ElementPlusResolver(),
      VueUseComponentsResolver(),
      // Icon自动引入解析器
      IconsResolver({
        // 自动引入的Icon组件统一前缀，默认为 i，设置false为不需要前缀
        prefix: 'icon',
        // 当图标集名字过长时，可使用集合别名
        alias: {
          system: 'system-uicons'
        },
        // 标识自定义图标集
        customCollections: ['qj']
        // prefix - 前缀，默认为 i，上面我们配置成了 icon，即组件名以 icon 开头
        // collection - 图标集名
        // icon - 图标名
        // {prefix}-{collection}-{icon}
        // 当然大驼峰也可以，下面是用的就是大驼峰，因为看着顺眼
      })
    ]
  });
}
