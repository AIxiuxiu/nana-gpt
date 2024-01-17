import { FileSystemIconLoader } from 'unplugin-icons/loaders';
import Icons from 'unplugin-icons/vite';

/**
 * iconify图标
 * 所有图标库 https://icon-sets.iconify.design
 */
export default function createIcon() {
  return Icons({
    compiler: 'vue3',
    // 自动安装
    autoInstall: true,
    customCollections: {
      // qj图标集
      // 给svg文件设置fill="currentColor"属性，使图标的颜色具有适应性
      qj: FileSystemIconLoader('src/assets/icons', (svg) => svg.replace(/^<svg /, '<svg fill="currentColor" '))
    }
  });
}
