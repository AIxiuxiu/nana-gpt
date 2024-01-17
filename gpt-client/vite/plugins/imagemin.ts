import viteImagemin from 'vite-plugin-imagemin';

/**
 * 图片压缩
 */
export default function viteImageminPlugin() {
  const plugin = viteImagemin({
    gifsicle: {
      optimizationLevel: 7,
      interlaced: false
    },
    mozjpeg: {
      quality: 20
    },
    optipng: {
      optimizationLevel: 7
    },
    pngquant: {
      quality: [0.8, 0.9],
      speed: 4
    },
    svgo: {
      plugins: [
        {
          name: 'removeViewBox'
        },
        {
          name: 'removeEmptyAttrs',
          active: false
        }
      ]
    }
  });
  return plugin;
}
