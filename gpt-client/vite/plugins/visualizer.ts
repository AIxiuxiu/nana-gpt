import visualizer from 'rollup-plugin-visualizer';
/**
 * 依赖分析
 */
export function visualizerConfig(env: any) {
  const { VITE_BUILD_ANALYSIS } = env;
  if (VITE_BUILD_ANALYSIS) {
    return visualizer({
      filename: './node_modules/.cache/visualizer/stats.html',
      open: true,
      gzipSize: true,
      brotliSize: true
    });
  }
  return [];
}
