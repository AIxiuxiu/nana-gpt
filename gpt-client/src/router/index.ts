import { setupLayouts } from 'layouts-generated';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { createRouter, createWebHistory } from 'vue-router';
import generatedRoutes from '~pages';
import { isLogin } from '../utils/auth';

const routes = setupLayouts(generatedRoutes);

// 不匹配返回首页
routes.push({
  path: '/:pathMatch(.*)*',
  redirect: '/chatView'
});

//导入生成的路由数据
const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_SERVER_BASE),
  routes
});

router.beforeEach(async (_to, _from, next) => {
  if (_to.path != '/login' && !isLogin()) {
    const fullPath = encodeURIComponent(_to.fullPath);
    next(`/login?redirect=${fullPath}`);
  }
  if (_to.meta && _to.meta.title) {
    window.document.title = _to.meta.title as string;
  }
  NProgress.start();
  next();
});

router.afterEach((_to) => {
  NProgress.done();
});

router.onError(async (error) => {
  const isUpdate = await diffVersion();
  if (isUpdate) {
    ElMessageBox.confirm('检测到新版本，请刷新页面', '提示', {
      type: 'info',
      showCancelButton: false
    }).then(() => {
      document.location.reload();
    });
  } else {
    ElMessageBox.confirm('页面跳转错误，请刷新页面', '提示', {
      type: 'info',
      showCancelButton: false
    }).then(() => {
      document.location.reload();
    });
  }
});

// 比较版本号
async function diffVersion() {
  const newVersion = await getVersion();
  const oldVersion = document.documentElement?.dataset?.version?.trim();
  // 有新版本
  return newVersion !== oldVersion;
}

// 获取最新版本号
function getVersion() {
  return fetch(`/version.json?_t=${Date.now()}`).then((res) => {
    if (res.ok) {
      return res.json().then((json) => {
        return json.version;
      });
    } else {
      throw new Error('获取版本号失败');
    }
  });
}
export default router;
