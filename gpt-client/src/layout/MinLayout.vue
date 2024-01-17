<template>
  <el-scrollbar ref="scrollBarRef" wrap-class="main-wrap" view-class="main-view">
    <div class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </el-scrollbar>
</template>

<script setup lang="ts">
/**
 * 公共布局
 */
</script>

<style lang="scss" scoped>
:deep(.main-view) {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  min-width: $content-min-width;
}

:deep(.main-wrap) {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: auto;
}

.main-content {
  flex: 1;
  position: relative;
  overflow: hidden;
}
</style>
