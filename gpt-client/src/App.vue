<template>
  <el-config-provider :locale="zhCn">
    <router-view v-if="isRouterAlive" v-slot="{ Component, route }">
      <transition :name="route.meta.transition" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </el-config-provider>
</template>
<script setup lang="ts">
import { ElConfigProvider } from 'element-plus';
import zhCn from 'element-plus/lib/locale/lang/zh-cn';
import { nextTick, provide, ref } from 'vue';
import { InjectReloadKey } from './types/symbols';

/**
 * 刷新页面
 */
provide(InjectReloadKey, reload);
const isRouterAlive = ref(true);
function reload() {
  isRouterAlive.value = false;
  nextTick(() => (isRouterAlive.value = true));
}
</script>

<style>
#app {
  position: relative;
  margin: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
</style>
