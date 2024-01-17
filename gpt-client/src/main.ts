import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import piniaStore from './store';
// 时间插件
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
// 图片懒加载
import VueLazyLoad from 'vue3-lazyload';

import useImage from './hooks/useImg';
// 全局样式
import './styles/index.scss';

import 'animate.css';
// 这块一定要加,否者会有部分动画无法执行
import 'animate.css/animate.compat.css';

import imageView from '@/components/QjImageView';

const app = createApp(App);

// 图片懒加载设置
app.use(VueLazyLoad, {
  // error: ;
  // loading: ;
});

app.use(router);
app.use(piniaStore);
app.use(imageView);

// dayjs 时间库
dayjs.locale('zh-cn');
app.config.globalProperties.$dayjs = dayjs;
app.config.globalProperties.$img = useImage;
app.config.globalProperties.$const = {
  // 路演默认图片
  roadshowCover: 'https://rs.p5w.net/theme/default/images/road_show_detail/road_cover.png'
};

app.config.errorHandler = (error, vm, info) => {
  console.error('抛出全局异常', error, vm, info);
};

app.mount('#app');
