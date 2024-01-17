const imgModules = import.meta.glob('../assets/images/*', { eager: true });

/**
 * 使用图片
 * @param name 图片名称
 */
export default function useImage(name: string) {
  const imgPath = `../assets/images/${name}`;
  const imgModule: any = imgModules[imgPath];
  if (imgModule) {
    return imgModule.default;
  } else {
    return '';
  }
}
