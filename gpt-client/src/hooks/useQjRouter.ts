import router from '@/router';
import { RouteLocationRaw, Router } from 'vue-router';

/**
 * 返回
 * @param path 路由
 */
export default function useQjRouter(_router?: Router) {
  _router = _router ? _router : router;
  const back = (path = '/') => {
    if (window.history.length > 0) {
      _router.go(-1);
    } else {
      _router.replace(path);
    }
  };

  const open = (to: RouteLocationRaw) => {
    const routeData = _router.resolve(to);
    const webUrl = location.protocol + '//' + location.host + routeData.href;
    window.open(webUrl, '_blank');
  };

  return { back, open };
}
