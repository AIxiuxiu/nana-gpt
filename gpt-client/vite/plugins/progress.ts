/**
 * 构建显示进度条
 */

import progress from 'vite-plugin-progress';

export const viteProgressPlugin = () => {
  return progress();
};
