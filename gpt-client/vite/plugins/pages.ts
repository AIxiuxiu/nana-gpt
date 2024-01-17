/**
 * 动态生成路由
 */
import Pages from 'vite-plugin-pages';
export default function vitePagesPlugin() {
  return Pages({
    pagesDir: [{ dir: 'src/views', baseRoute: '' }],
    extensions: ['vue', 'md'],
    exclude: ['**/components/*.vue'],
    routeStyle: 'next',
    importMode(filepath, options) {
      for (const page of options.dirs) {
        if (page.baseRoute === '' && filepath.startsWith(`/${page.dir}/index`)) {
          return 'sync';
        }
      }
      return 'async';
    }
  });
}
