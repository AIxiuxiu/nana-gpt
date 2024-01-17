# chatGPT demo

## 环境搭建

### 安装 Node 环境

下载最新稳定版本安装即可（node16 以上版本）
安装 node.js [下载地址](https://nodejs.org/zh-cn/download/)https://nodejs.org/zh-cn/
打开命令端输入 `node -v 和 npm -v` 打印版本号即安装成功

### 安装依赖

npm 默认和 node 一起安装，但使用时需要代理，不然有些可能无法下载，推荐使用[yarn](https://yarn.bootcss.com/)

```shell
# 安装yarn
npm install -g yarn
# 查看版本
yarn -v
```

## 启动

```shell
# web端启动
yarn start
# 或者
yarn dev
```

## 目录结构

以下是系统的目录结构

```
├── docs                 // 文档相关
├── src
│    ├── apis             // api请求
│    ├── assets          // 静态文件
│    ├── components      // 业务通用组件
│    ├── hooks           // 常用hook
│    ├── layout          // 公共布局
│    ├── router          // 路由文件
│    ├── store           // 状态管理
│    ├── styles          // 全局样式
│    ├── styles          // 全局样式
│    ├── types           // ts声明
│    ├── utils           // 常用工具
│    ├── views           // 业务页面
│    ├── App.vue         // vue模板入口
│    ├── main.ts         // vue模板js
├── .d.ts                // 类型定义
├── index.html          // 入口页面
├── .env.*              // 配置文件
├── .eslintrc.js        // eslint配置
├── .prettierrc         // perttier配置
├── .stylelintrc        // stylelint配置
├── tsconfig.json       // TypeScript 配置文件
├── vite.config.ts      // Vite 配置文件
├── package.json        // 项目基本信息和依赖
└── README.md           // 项目说明
```

## 开发注意事项

### 自动生成 vue-router

项目使用[vite-plugin-pages](https://github.com/hannoeru/vite-plugin-pages)插件
`src/views`文件下 vue 文件自动生成路由，创建组件需新建`components`文件夹

遵循的规则请查看文档，示例

```
src/views/index.vue -> /
src/views/index/a.vue -> /a // 这里的a.vue就是index.vue的子路由（children）
src/views/father.vue -> /father
src/views/father/son.vue -> /father/son
src/views/father/[id].vue -> /father/:id
src/views/[father]/son.vue -> /:father/son
```

### 配置文件

`.env.*`是配置文件，建议拷贝`.env.development`并重命名为`.env.development.local`文件，防止冲突

`VITE_APP_API_BASEURL`为接口请求的地址

### vue.code-snippets

提供快速生成代码段

- page（vue 新页面）
- page-route（vue 新页面带路由 route）
- api（api 新建文件）
- store（store 新建文件）

### 全局组件

`src/components`下的组件自动注册，请以 Qj 前缀命名，当前提供

- QjEmpty（空占位）
- QjPagination（分页），请和`useAsyncTableApi`配合使用

### 图标

把图标 svg 文件放到`src/assets/icons`文件下
使用方式 <icon-qj-svg 文件名字 /> 查看 demo

其他图标如@element-plus/icons-vue
<icon-ep-svg 名字 />

所有图标库 https://icon-sets.iconify.design

## 第三方库

- [element-plus UI 库](https://element-plus.gitee.io/zh-CN/)
- [dayjs 时间库](https://dayjs.gitee.io/zh-CN/)
- [mitt 全局事件监听库](https://github.com/developit/mitt)
- [pinia 状态管理](https://pinia.vuejs.org/zh/introduction.html)
- [vue3-lazyload 图片懒加载](https://github.com/murongg/vue3-lazyload)
- [ViteSSG](https://github.com/antfu/vite-ssg)，SEO 优化，后面可能用到

## 发布

测试环境发布

```
yarn deploy
```
