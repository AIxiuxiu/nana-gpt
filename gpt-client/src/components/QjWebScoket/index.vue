<template>
  <div class="qj-ws">
    <el-tooltip v-if="wsStore.websocket" effect="light" content="连接中" placement="right">
      <el-alert type="success" show-icon :closable="false" />
    </el-tooltip>
    <el-tooltip v-else-if="!wsStore.websocket && wsStore.webSocketError < wsStore.webSocketErrorMax" effect="light" content="连接已断开，重连中..." placement="right">
      <el-alert type="warning" show-icon :closable="false" />
    </el-tooltip>
    <el-tooltip v-else effect="light" content="连接已断开，请刷新重试" placement="right">
      <el-alert type="error" show-icon :closable="false" />
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { useWSStore } from '@/store';
/**
 * webscoket状态
 */
const wsStore = useWSStore();
</script>

<style lang="scss" scoped>
.qj-ws {
  position: fixed;
  top: 38px;
  left: 16px;
  line-height: 20px;
  max-width: 220px;
  display: flex;
  justify-content: center;
}

:deep(.el-alert) {
  padding: 8px;
}
:deep(.el-alert__content) {
  padding: 0;
}
</style>
