<template>
  <div class="page">
    <el-menu v-if="!currentRoute.meta.hideNav" :default-active="$route.path" router mode="horizontal">
      <el-menu-item index="/chatView">聊天室</el-menu-item>
      <el-menu-item index="/kb">知识库</el-menu-item>
      <el-menu-item index="/interact">互动问答</el-menu-item>
      <el-menu-item index="/setting">提示词</el-menu-item>

      <el-menu-item>
        <template #title>
          <a class="link" href="http://172.22.9.116/qa" target="_blank">
            <span>全景智慧问答</span>
          </a>
        </template>
      </el-menu-item>
    </el-menu>
    <img class="logo" :src="$img('logo_gray.png')" alt="logo" />

    <el-scrollbar ref="scrollBarRef" wrap-class="main-wrap" view-class="main-view">
      <div class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
      <el-backtop :right="10" :bottom="100" target=".main-wrap">
        <div
          style="
            height: 100%;
            width: 100%;
            background-color: var(--el-bg-color-overlay);
            box-shadow: var(--el-box-shadow-lighter);
            text-align: center;
            color: #1989fa;
            line-height: 40px;
          "
        >
          <icon-ep-arrow-up></icon-ep-arrow-up>
        </div>
      </el-backtop>
    </el-scrollbar>
    <div class="doodle-bg">
      <css-doodle>
        :doodle {@grid: 1x4 / 92vw 92vh; } @place-cell: center; width: @rand(40vw, 80vw); height: @rand(40vh, 80vh); transform: translate(@rand(-120%, 120%), @rand(-80%, 80%))
        scale(@rand(.8, 2.8)) skew(@rand(45deg)); clip-path: polygon( @r(0, 30%) @r(0, 50%), @r(30%, 60%) @r(0%, 30%), @r(60%, 100%) @r(0%, 50%), @r(60%, 100%) @r(50%, 100%),
        @r(30%, 60%) @r(60%, 100%), @r(0, 30%) @r(60%, 100%) ); background: @pick(#f44336, #e91e63, #9c27b0, #673ab7, #3f51b5, #60569e, #e6437d, #ebbf4d, #00bcd4, #03a9f4, #2196f3,
        , #ffc107, #e136eb, #f57c23); opacity: @rand(.4, .9); position: absolute; transform: translateZ(0); top: @rand(-40vh, 80vh); left: @rand(-40vw, 80vw); animation:
        colorChange @rand(6.1s, 26.1s) infinite @rand(-.5s, -2.5s) linear alternate; @keyframes colorChange { 100% { left: 0; top: 0; filter: hue-rotate(360deg); } }
      </css-doodle>
    </div>
  </div>
</template>

<script setup lang="ts">
import router from '@/router';

/**
 * 公共布局
 */

const scrollBarRef = ref();
router.afterEach((_to) => {
  scrollBarRef.value && scrollBarRef.value.setScrollTop(0);
});

const currentRoute = useRouter().currentRoute;
</script>

<style lang="scss" scoped>
.page {
  position: relative;
  overflow: hidden;
  height: 100%;
  width: 100%;
  // background-color: #efefef;
}

.logo {
  position: absolute;
  top: 6px;
  left: 16px;
  height: 28px;
}

:deep(.main-view) {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  width: 100vw;
  min-width: $content-min-width;
}

:deep(.main-wrap) {
  width: 100%;
  height: calc(100% - 40px);
  position: relative;
  overflow: auto;
  z-index: 1;
}

.main-content {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.doodle-bg {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 0;
  left: 0;
  z-index: -1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: #fff;
}
.doodle-bg::after {
  content: '';
  position: absolute;
  top: -100%;
  left: -100%;
  right: -100%;
  bottom: -100%;
  backdrop-filter: blur(100px);
  -webkit-backdrop-filter: blur(100px);
  z-index: 1;
  transform: translateZ(0);
}

:deep(.el-menu) {
  height: 40px;
  display: flex;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.4);
  border: none;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
</style>
