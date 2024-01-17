import { writeFileSync } from 'fs';

/**
 * version版本注入
 */
export default function htmlVersion() {
  const version = `${Date.now()}`;
  return {
    name: 'html-version',
    transformIndexHtml(html: string) {
      return html.replace(/<html/, `<html data-version="${version}"`);
    },
    configResolved(config) {
      writeFileSync(config.publicDir + '/version.json', JSON.stringify({ version }));
    }
  };
}
