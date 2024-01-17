import { createVNode, render } from 'vue';
import QJImageView from './index.vue';

export default {
  install(app) {
    //createVNode vue提供的底层方法 可以给我们组件创建一个虚拟DOM 也就是Vnode
    const vnode = createVNode(QJImageView);
    //render 把我们的Vnode 生成真实DOM 并且挂载到指定节点
    render(vnode, document.body);
    // Vue 提供的全局配置 可以自定义

    app.config.globalProperties.$imgPreview = {
      open: (imgs: string | string[], opt?: QJImageViewOptions) => vnode.component?.exposed?.open(imgs, opt),
      close: () => vnode.component?.exposed?.close()
    };
    app.component('QJImageView', QJImageView);
  }
};

export type QJImageViewOptions = {
  zIndex: number;
  initialIndex: boolean;
  infinite: boolean;
  hideOnClickModal: boolean;
};
