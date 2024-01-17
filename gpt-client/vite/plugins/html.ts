import { createHtmlPlugin } from 'vite-plugin-html';

/**
 * html注入
 */
export default function createHtml(env: any, mini: boolean) {
  const { VITE_APP_TITLE } = env;
  const html: any[] = createHtmlPlugin({
    inject: {
      data: {
        title: VITE_APP_TITLE
      }
    },
    minify: mini
  });
  return html;
}
