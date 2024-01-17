/**
 * 返回 hasList，上层是列表页
 */
export const useGoBack = function (router, route) {
  const goBack = (hasList?: boolean) => {
    if (hasList && window.history.length <= 2) {
      const routePaths = route.path.split('/');
      if (routePaths.length > 1) {
        routePaths.pop();
      }
      router.push({ path: routePaths.join('/') });
      return false;
    } else {
      router.go(-1);
    }
  };
  return goBack;
};
