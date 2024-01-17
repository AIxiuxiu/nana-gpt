<template>
  <div class="qj-empty">
    <el-empty description="暂无数据">
      <slot></slot>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
/**
 * 空数据组件
 */
</script>

<style lang="scss" scoped>
.qj-empty {
  line-height: 20px;
  width: 100%;
  display: flex;
  justify-content: center;
}
</style>
