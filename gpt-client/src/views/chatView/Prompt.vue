<template>
  <div class="prompt">
    <div class="label">选择扮演的角色</div>
    <el-select v-model="selected" clearable size="small" placeholder="选择扮演角色" @change="selectedChange">
      <el-option v-for="item in promptList" :key="item.value" :label="item.topic" :value="item.content" />
    </el-select>
    <div class="label marginT10">
      聊天的提示词
      <span style="float: right; display: flex; align-items: center">推荐：<el-link type="primary" href="https://www.aishort.top">aishort</el-link> </span>
    </div>
    <el-input style="flex: 1" size="small" :model-value="modelValue" clearable type="textarea" placeholder="提示词" @input="inputChange" />
  </div>
</template>
<script setup lang="ts">
import PromptApi from '@/apis/promptApi';

defineProps({
  modelValue: {
    type: String,
    default: ''
  }
});

const emits = defineEmits(['update:modelValue']);

const promptList = ref([]);
PromptApi.list(1).then((data) => {
  promptList.value = data;
});

const selected = ref('');

function selectedChange(value) {
  if (value) {
    ElMessage.warning('选择角色后建议清空历史记录，防止干扰！');
  }
  emits('update:modelValue', value);
}

function inputChange(value) {
  emits('update:modelValue', value);
}
</script>
<style scoped lang="scss">
.prompt {
  height: calc(100% - 146px);
  display: flex;
  flex-direction: column;

  .label {
    margin-left: 4px;
    margin-bottom: 4px;
    color: #666;
  }
}
</style>
