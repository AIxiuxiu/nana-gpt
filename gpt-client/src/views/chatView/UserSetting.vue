<template>
  <el-dialog :model-value="modelValue" title="设置" width="480px" top="10vh" append-to-body center @close="handleClose()">
    <el-row :gutter="8">
      <el-col :span="24">
        <div class="marginR5 marginT10">chatgpt模型选择</div>
        <el-select v-model="currentEdit.model" placeholder="选择模型">
          <el-option v-for="item in modelOptions" :key="item.name" :label="item.name" :value="item.name" :disabled="item.disabled" />
        </el-select>
      </el-col>
      <el-col :span="24">
        <div class="marginR5 marginT10">回复消息最大token数</div>
        <el-input-number v-model="currentEdit.replyMaxToken" :min="1" :max="4096" />
      </el-col>
      <el-col :span="24">
        <div class="marginR5 marginT10">上下文拼接最多历史消息</div>
        <el-input-number v-model="currentEdit.maxHistory" :min="1" :max="100" />
      </el-col>
      <el-col :span="24">
        <div class="marginR5 marginT10">上下文拼接多久的消息（小时）</div>
        <el-input-number v-model="currentEdit.howLongTime" :min="1" :max="12" />
      </el-col>
    </el-row>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose()">取消</el-button>
        <el-button type="primary" @click="submitSetting()"> 提交 </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import UserApi from '@/apis/userApi';

defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
});

const emits = defineEmits(['update:modelValue']);

UserApi.setting().then((data) => {
  if (data) {
    Object.assign(currentEdit, data);
  }
});

const currentEdit = reactive({
  model: '',
  replyMaxToken: 0,
  maxHistory: 0,
  howLongTime: 0
});

const modelOptions = [
  {
    name: 'gpt-3.5-turbo',
    disabled: false
  },
  {
    name: 'gpt-3.5-turbo-16k',
    disabled: false
  },
  {
    name: 'gpt-4',
    disabled: false
  },
  {
    name: 'gpt-4-32k',
    disabled: false
  },
  {
    name: 'gpt-4-1106-preview',
    disabled: false
  }
];

function submitSetting() {
  const params: any = { ...currentEdit };
  UserApi.update(params)
    .then(() => {
      ElMessage.success('编辑成功');
    })
    .finally(() => {
      handleClose();
    });
}

function handleClose() {
  emits('update:modelValue', false);
}
</script>

<style lang="scss" scoped>
//
</style>
