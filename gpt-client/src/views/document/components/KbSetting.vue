<template>
  <el-dialog :model-value="modelValue" title="知识库设置" width="880px" top="10vh" append-to-body center @close="handleClose()">
    <div class="marginB15">
      <span class="marginR5 black">基础设置</span>
      <el-row>
        <el-col :span="12">
          <div class="marginR5">chatgpt模型选择</div>
          <el-select v-model="currentEdit.model" value-key="name" placeholder="选择模型" @change="modelChange">
            <el-option v-for="item in modelOptions" :key="item.name" :label="item.name" :value="item.name" :disabled="item.disabled" />
          </el-select>
        </el-col>
        <el-col :span="12">
          <div class="marginR5">进程数</div>
          <el-input-number v-model="currentEdit.poolSize" :min="1" :max="20" />
        </el-col>
      </el-row>
    </div>

    <div class="marginB15">
      <span class="marginR5 black">段落分段设置</span>
      <el-row>
        <el-col :span="12">
          <div class="marginR5">每段最大长度</div>
          <el-input-number v-model="currentEdit.splitterChunkSize" :min="1" :max="128000" />
        </el-col>
        <el-col :span="12">
          <div class="marginR5">每拼接最大块</div>
          <el-input-number v-model="currentEdit.splitterChunkOverlap" :min="1" :max="10000" />
        </el-col>
      </el-row>
    </div>
    <div class="marginB15">
      <span class="marginR5 black">聊天设置</span>
      <el-row>
        <el-col :span="8">
          <div class="marginR5">回上下文最大长度</div>
          <el-input-number v-model="currentEdit.replyMaxToken" :min="1" :max="4096" />
        </el-col>
        <el-col :span="8">
          <div class="marginR5">相似度搜索的数量</div>
          <el-input-number v-model="currentEdit.replySearchTopK" :min="1" :max="100" />
        </el-col>
        <el-col :span="8">
          <div class="marginR5">最小相似度</div>
          <el-input-number v-model="currentEdit.replyScore" :precision="2" :step="0.1" :max="1" />
        </el-col>
      </el-row>
    </div>
    <div class="marginB15">
      <span class="marginR5 black">生成问答对设置</span>
      <el-row>
        <el-col :span="12">
          <div class="marginR5">优化问题</div>
          <el-switch v-model="currentEdit.qaOptimize" inline-prompt active-text="是" inactive-text="否" :active-value="1" :inactive-value="0" />
        </el-col>
        <el-col :span="12">
          <div class="marginR5">问答对存储到向量数据库</div>
          <el-switch v-model="currentEdit.qaEmbed" inline-prompt active-text="是" inactive-text="否" :active-value="1" :inactive-value="0" />
        </el-col>
        <el-col :span="12">
          <div class="marginR5">问答添加上文总结</div>
          <el-switch v-model="currentEdit.qaAddSummary" inline-prompt active-text="是" inactive-text="否" :active-value="1" :inactive-value="0" />
        </el-col>
      </el-row>
    </div>

    <div class="d-flex ai-center">
      <span class="marginR5">QA生成提示词</span>
      <el-popover
        placement="bottom-start"
        :popper-options="{
          modifiers: [
            {
              name: 'offset',
              options: {
                offset: [-94, 8]
              }
            }
          ]
        }"
        :width="816"
        trigger="hover"
      >
        <template #reference>
          <el-icon>
            <icon-ep-QuestionFilled />
          </el-icon>
        </template>
        <div>
          <div class="title">
            QA生成提示词，默认：
            <el-button type="primary" link @click="currentEdit.qaPrompt = promptTopic['QA生成']">填入</el-button>
          </div>
          <p>{{ promptTopic['QA生成'] }}</p>
          <!-- <p class="marginT10"><el-button type="primary" link @click="currentEdit.qaPrompt = promptTopic['QA生成']">填入</el-button></p> -->
        </div>
      </el-popover>
    </div>
    <el-input v-model="currentEdit.qaPrompt" :autosize="{ minRows: 4, maxRows: 12 }" type="textarea" placeholder="请输入QA提示词" />
    <div class="d-flex ai-center marginT10">
      <span class="marginR5">总结提示词</span>
      <el-popover
        placement="bottom-start"
        :popper-options="{
          modifiers: [
            {
              name: 'offset',
              options: {
                offset: [-76, 8]
              }
            }
          ]
        }"
        :width="816"
        trigger="hover"
      >
        <template #reference>
          <el-icon>
            <icon-ep-QuestionFilled />
          </el-icon>
        </template>
        <div>
          <div class="title">
            总结提示词，默认：
            <el-button type="primary" link @click="currentEdit.summaryPrompt = promptTopic['总结内容']">填入</el-button>
          </div>
          <p>{{ promptTopic['总结内容'] }}</p>
          <!-- <p class="marginT10"><el-button type="primary" link @click="currentEdit.summaryPrompt = promptTopic['总结内容']">填入</el-button></p> -->
        </div>
      </el-popover>
    </div>
    <el-input v-model="currentEdit.summaryPrompt" :autosize="{ minRows: 4, maxRows: 12 }" type="textarea" placeholder="请输入总结提示词" />
    <div class="d-flex ai-center marginT10">
      <span class="marginR5">优化问题提示词</span>
      <el-popover
        placement="bottom-start"
        :popper-options="{
          modifiers: [
            {
              name: 'offset',
              options: {
                offset: [-104, 8]
              }
            }
          ]
        }"
        :width="816"
        trigger="hover"
      >
        <template #reference>
          <el-icon>
            <icon-ep-QuestionFilled />
          </el-icon>
        </template>
        <div>
          <div class="title">
            优化问题提示词，默认：
            <el-button type="primary" link @click="currentEdit.optimizePrompt = promptTopic['优化问题']">填入</el-button>
          </div>
          <p>{{ promptTopic['优化问题'] }}</p>
          <!-- <p class="marginT10"><el-button type="primary" link @click="currentEdit.optimizePrompt = promptTopic['优化问题']">填入</el-button></p> -->
        </div>
      </el-popover>
    </div>
    <el-input v-model="currentEdit.optimizePrompt" :autosize="{ minRows: 4, maxRows: 12 }" type="textarea" placeholder="请输入优化问题提示词" />
    <div class="d-flex ai-center marginT10">
      <span class="marginR5">知识库问答提示词</span>
      <el-popover
        placement="bottom-start"
        :popper-options="{
          modifiers: [
            {
              name: 'offset',
              options: {
                offset: [-116, 8]
              }
            }
          ]
        }"
        :width="816"
        trigger="hover"
      >
        <template #reference>
          <el-icon>
            <icon-ep-QuestionFilled />
          </el-icon>
        </template>
        <div>
          <div class="title">
            知识库问答提示词，默认：
            <el-button type="primary" link @click="currentEdit.replyPrompt = promptTopic['知识库问答']">填入</el-button>
          </div>
          <p>{{ promptTopic['知识库问答'] }}</p>
          <!-- <p class="marginT10"><el-button type="primary" link @click="currentEdit.replyPrompt = promptTopic['知识库问答']">填入</el-button></p> -->
        </div>
      </el-popover>
    </div>
    <el-input v-model="currentEdit.replyPrompt" :autosize="{ minRows: 4, maxRows: 12 }" type="textarea" placeholder="请输入回复提示词" />

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose()">取消</el-button>
        <el-button type="primary" @click="submitPromptEdit()"> 提交 </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import KbSettingApi from '@/apis/kbSettingApi';
import PromptApi from '@/apis/promptApi';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  kbId: {
    type: String,
    default: ''
  }
});

const emits = defineEmits(['update:modelValue']);

KbSettingApi.detail(props.kbId).then((data) => {
  if (data) {
    Object.assign(currentEdit, data);
  }
});

const currentEdit = reactive({
  id: '',
  replyPrompt: '',
  qaPrompt: '',
  summaryPrompt: '',
  model: '',
  splitterChunkSize: 1000,
  splitterChunkOverlap: 200,
  poolSize: 8,
  replyMaxToken: 3000,
  replySearchTopK: 25,
  replyScore: 0.8,
  qaEmbed: 0,
  qaAddSummary: 0,
  qaOptimize: 0,
  optimizePrompt: ''
});

const modelOptions = [
  {
    name: 'gpt-3.5-turbo',
    disabled: false,
    splitterChunkSize: 2500,
    splitterChunkOverlap: 100,
    replyMaxToken: 1200
  },
  {
    name: 'gpt-3.5-turbo-16k',
    disabled: false,
    splitterChunkSize: 10000,
    splitterChunkOverlap: 400,
    replyMaxToken: 4096
  },
  {
    name: 'gpt-4',
    disabled: false,
    splitterChunkSize: 5000,
    splitterChunkOverlap: 200,
    replyMaxToken: 2500
  },
  {
    name: 'gpt-4-32k',
    disabled: false,
    splitterChunkSize: 25000,
    splitterChunkOverlap: 500,
    replyMaxToken: 4096
  },
  {
    name: 'gpt-4-1106-preview',
    disabled: false,
    splitterChunkSize: 100000,
    splitterChunkOverlap: 2000,
    replyMaxToken: 4096
  }
];

function submitPromptEdit() {
  const params: any = { ...currentEdit };
  KbSettingApi.update(params)
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

const promptTopic = reactive({
  知识库问答: '',
  QA生成: ''
});
PromptApi.list(0).then((data) => {
  data.forEach((v) => (promptTopic[v.topic] = v.content));
});

function modelChange(value) {
  const mopdel = modelOptions.filter((v) => v.name == value)[0];
  currentEdit.splitterChunkSize = mopdel.splitterChunkSize;
  currentEdit.splitterChunkOverlap = mopdel.splitterChunkOverlap;
  currentEdit.replyMaxToken = mopdel.replyMaxToken;
}
</script>

<style lang="scss" scoped>
//
.title {
  font-size: 14px;
  color: #333;
}
</style>
