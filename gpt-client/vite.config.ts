import path from 'path';

import { ConfigEnv, loadEnv, UserConfig } from 'vite';
import createVitePlugins from './vite/plugins';

// https://vitejs.dev/config/
export default ({ mode, command }: ConfigEnv): UserConfig => {
  const env = loadEnv(mode, process.cwd());
  return {
    base: env.VITE_SERVER_BASE,
    assetsInclude: path.resolve(__dirname, 'src/assets/images'),
    envDir: process.cwd(),
    // 开发服务器选项 https://cn.vitejs.dev/config/#server-options
    server: {
      port: +env.VITE_SERVER_PORT,
      open: false,
      host: '0.0.0.0', // IP配置，支持从IP启动
      proxy: {
        '/proxy': {
          target: env.VITE_APP_API_BASEURL,
          changeOrigin: command === 'serve' && env.VITE_OPEN_PROXY == 'true',
          rewrite: (path) => path.replace(/\/proxy/, '')
        }
      }
    },
    // 构建选项 https://cn.vitejs.dev/config/#server-fsserve-root
    build: {
      outDir: path.resolve(__dirname, `${env.VITE_BUILD_OUTDIR}`),
      sourcemap: env.VITE_BUILD_SOURCEMAP == 'true',
      emptyOutDir: true,
      minify: 'esbuild',
      reportCompressedSize: false,
      chunkSizeWarningLimit: 2048
    },
    // 删除 console
    esbuild: {
      drop: env.VITE_BUILD_DROP_CONSOLE == 'true' ? ['console', 'debugger'] : ['debugger']
    },
    plugins: createVitePlugins(env, command === 'build'),
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
        '#': path.resolve(__dirname, 'types')
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          charset: false,
          additionalData: `@import "@/styles/variables.scss"; @import "@/styles/utils.scss";`
        }
      },
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove();
                }
              }
            }
          }
        ]
      }
    }
  };
};
