<template>
  <div class="interact">
    <div v-loading="isLoading" class="contenter">
      <div class="aside">
        <div class="top">
          <el-input v-model="searchValue" class="search-input" placeholder="搜索" :prefix-icon="Search" @submit.prevent @keyup.enter="refresh()" />
          <el-tooltip effect="light" content="新增互动问答" placement="right">
            <div class="add" @click="addInteract">
              <icon-ep-plus></icon-ep-plus>
            </div>
          </el-tooltip>
        </div>
        <QjInfiniteScroll class="list">
          <div v-for="item in dataList" :key="item.id" class="item" :class="{ active: item.id == currentInteract.id }" @click="setCurrentInteract(item)">
            <div class="title ellipsis-1">{{ item.companyName }}</div>
            <div class="tag">{{ item.startDate }}~{{ item.endDate }}</div>
            <el-tooltip effect="light" :content="'问题' + getStatusStr(item.questionStatus)" placement="bottom">
              <el-icon class="q-icon" :color="getStatusColor(item.questionStatus)">
                <icon-qj-fenduan></icon-qj-fenduan>
              </el-icon>
            </el-tooltip>
          </div>
        </QjInfiniteScroll>
      </div>

      <div class="main">
        <template v-if="currentInteract">
          <el-tabs v-model="activeName">
            <el-tab-pane label="基本信息" name="base">
              <div class="base-info">
                <el-form class="marginT20" :model="interactForm" size="large" :label-width="100">
                  <el-form-item label="公司信息">
                    <el-input :model-value="`${interactForm.companyShortName}(${interactForm.companyCode})`" :readonly="true" autocomplete="off" style="width: 680px" />
                  </el-form-item>
                  <el-form-item label="生成问题提示词">
                    <el-input v-model="interactForm.qprompt" :autosize="{ minRows: 2, maxRows: 4 }" type="textarea" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item label="优化问题">
                    <el-switch v-model="interactForm.qaOptimize" inline-prompt active-text="是" inactive-text="否" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                  <el-form-item v-if="interactForm.qaOptimize == 1" label="优化问题提示词">
                    <el-input v-model="interactForm.optimizePrompt" :autosize="{ minRows: 2, maxRows: 4 }" type="textarea" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" plain @click="saveInteract">保存</el-button>
                    <el-button type="danger" plain @click="removeInteract">删除</el-button>
                  </el-form-item>
                </el-form>

                <el-divider style="margin-top: 60px" content-position="left">互动问答数据</el-divider>
                <div class="d-flex jc-between">
                  <el-card class="box-card">
                    <template #header>
                      <div class="card-header">
                        <span>互动问题</span>
                        <el-button type="primary" :disabled="currentInteract.questionStatus == 1" plain @click="createQuestion"> 生成问题 </el-button>
                      </div>
                    </template>
                    <div class="d-flex jc-center ai-center">
                      <el-progress type="dashboard" :status="questionProgress.status" :percentage="questionProgress.percentage" :color="colors" />
                    </div>
                  </el-card>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="内容数据" name="contents">
              <ContentList v-if="activeName == 'contents'" :interact-id="currentInteract?.id" />
            </el-tab-pane>
            <el-tab-pane label="问题" name="question">
              <QList v-if="activeName == 'question'" :interact-id="currentInteract?.id" />
            </el-tab-pane>
          </el-tabs>
        </template>
        <template v-else>
          <div>
            <qj-empty>
              <el-button type="primary" @click="addInteract"> 添加互动问答 </el-button>
            </qj-empty>
          </div>
        </template>
      </div>
    </div>

    <el-dialog :model-value="showAdd" title="创建互动问答" width="680px" center :destroy-on-close="true" @close="showAdd = false">
      <AddInteract :qprompt="qprompt" :optimize-prompt="optimizePrompt" @success="addSuccess()" />
    </el-dialog>

    <QjWebScoket />
  </div>
</template>
<script setup lang="ts">
/**
 * 互动问答
 */

import InteractApi from '@/apis/interactApi';
import InteractContentsApi from '@/apis/interactContentsApi';
import PromptApi from '@/apis/promptApi';
import { useAsyncTableApi } from '@/hooks/useAsyncApi';
import { useWSStore } from '@/store';
import { Search } from '@element-plus/icons-vue';
import { reactive, ref } from 'vue';
import AddInteract from './components/AddInteract.vue';
import ContentList from './components/ContentList.vue';
import QList from './components/QList.vue';

const searchValue = ref('');

const {
  isLoading,
  data: dataList,
  refresh
} = useAsyncTableApi(
  (params) => {
    if (searchValue.value) {
      params.searchValue = searchValue.value;
    }
    return InteractApi.list(params);
  },
  {
    onSuccess: (data) => {
      console.error(data);
      if (data && data.length > 0 && !currentInteract.value) {
        setCurrentInteract(data[0]);
      }
    }
  }
);

const showAdd = ref(false);
const currentInteract = ref();

const interactForm = reactive({ id: null, companyName: '', companyShortName: '', companyCode: '', maxNum: 0, qprompt: '', qaOptimize: 0, optimizePrompt: '' });

function addInteract() {
  showAdd.value = true;
}

function addSuccess() {
  currentInteract.value = null;
  refresh();
  showAdd.value = false;
}

function setCurrentInteract(data) {
  currentInteract.value = { ...data };
  interactForm.id = data.id;
  interactForm.companyName = data.companyName;
  interactForm.companyShortName = data.companyShortName;
  interactForm.companyCode = data.companyCode;
  interactForm.maxNum = data.maxNum;
  interactForm.qprompt = data.qprompt;
  interactForm.qaOptimize = data.qaOptimize;
  interactForm.optimizePrompt = data.optimizePrompt;
  getQProgress(data.id);
}

function saveInteract() {
  InteractApi.update({ id: interactForm.id, qprompt: interactForm.qprompt }).then(() => {
    ElMessage.success('保存成功！');
    refresh();
  });
}

function removeInteract() {
  ElMessageBox.confirm('确认删除当前互动问答吗？').then(() => {
    InteractApi.remove(interactForm.id).then((data) => {
      if (data) {
        ElMessage.success('删除成功！');
      }
      currentInteract.value = null;
      refresh();
    });
  });
}

const activeName = ref('base');

const colors = [
  { color: '#6f7ad3', percentage: 30 },
  { color: '#1989fa', percentage: 60 },
  { color: '#5cb87a', percentage: 90 },
  { color: '#4ce2a7', percentage: 100 }
];

const questionProgress = reactive<any>({
  status: '',
  percentage: 0
});

function getQProgress(interactId) {
  questionProgress.status = '';
  questionProgress.percentage = 0;
  InteractContentsApi.questionStatus(interactId).then((res) => {
    if (res.interactId == currentInteract.value.id) {
      updateInteractInfo('questionStatus', res.questionStatus);
      questionProgress.status = res.status;
      questionProgress.percentage = res.percentage;
    }
  });
}

function createQuestion() {
  InteractContentsApi.createQuestion(currentInteract.value.id).then((res) => {
    if (res) {
      updateInteractInfo('questionStatus', 1);
      ElMessage.success('创建问题中，请稍等');
    }
  });
}

const wsStore = useWSStore();
wsStore.connectWs();

const onQuestionProgress = (data) => {
  if (data.interactId == currentInteract.value.id) {
    questionProgress.percentage = data.percentage;
    questionProgress.status = data.status;
    if (data.status == 'success') {
      updateInteractInfo('questionStatus', 2);
    } else if (data.status == 'exception') {
      updateInteractInfo('questionStatus', 3);
    } else {
      updateInteractInfo('questionStatus', 1);
    }
  }
};

wsStore.emitter.on('questionProgress', onQuestionProgress);

function updateInteractInfo(key: string, value: any) {
  if (currentInteract.value && currentInteract.value[key] != value) {
    currentInteract.value[key] = value;
    dataList.value = dataList.value.map(function (item) {
      if (currentInteract.value.id == item.id) {
        return currentInteract.value;
      }
      return item;
    });
  }
}

function getStatusColor(status) {
  if (status == 0) {
    return '#aaa';
  } else if (status == 1) {
    return '#278cf0';
  } else if (status == 2) {
    return '#4ce2a7';
  } else if (status == 3) {
    return '#e24c4c';
  }
  return '#fff';
}

function getStatusStr(status) {
  if (status == 0) {
    return '未开始';
  } else if (status == 1) {
    return '进行中';
  } else if (status == 2) {
    return '已完成';
  } else if (status == 3) {
    return '失败';
  }
  return '';
}

const qprompt = ref('');
const optimizePrompt = ref('');
PromptApi.list(0).then((data) => {
  data.forEach((v) => {
    if (v.topic == '生成问题') {
      qprompt.value = v.content;
    }
    if (v.topic == '优化问题') {
      optimizePrompt.value = v.content;
    }
  });
});

onUnmounted(() => {
  wsStore.emitter.off('questionProgress', onQuestionProgress);
});
</script>

<style lang="scss" scoped>
.interact {
  width: 100%;
  height: 100%;
  .contenter {
    padding: 16px;
    height: calc(100vh - 40px);
    margin: 0 auto;
    display: flex;
    justify-content: center;

    .aside {
      width: 280px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      background: rgb(255 255 255 / 70%);
      box-shadow: 0 6px 12px 0 rgb(31 38 135 / 20%);
      border-radius: 12px;
      .top {
        display: flex;
        justify-content: space-between;
        height: 44px;
        .search-input {
          font-size: 16px;

          --el-input-text-color: #333;
          --el-input-icon-color: #666;
          --el-input-height: 42px;
          --el-input-bg-color: rgb(255 255 255 / 60%);
          --el-input-border: none;
          --el-input-border-color: transparent;
          :deep(.el-input__wrapper) {
            background: rgb(255 255 255 / 55%);
            box-shadow: 0 8px 32px 0 rgb(240 240 247 / 37%);
            backdrop-filter: blur(4px);
            backdrop-filter: blur(4px);
            border-radius: 12px 0 0 12px;
            border: 1px solid rgb(255 255 255 / 18%);
          }
          :deep(.el-input__inner) {
            &::input-placeholder {
              color: #999;
            }
            &::input-placeholder {
              color: #9999;
            }
          }
        }
        .add {
          height: 44px;
          width: 48px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          color: #fff;
          border-radius: 0 12px 12px 0;
          background: #278cf0;
          background-image: linear-gradient(90deg, #74afea 0%, #278cf0 100%);
          opacity: 0.9;
          transition: all 0.3s;
          cursor: pointer;
          &:hover {
            opacity: 0.7;
          }
        }
      }
      .list {
        padding: 12px;
        flex: 1;
        overflow: scroll;
        &::-webkit-scrollbar {
          display: none;
        }
        .item {
          display: flex;
          position: relative;
          align-items: center;
          flex-direction: column;
          padding: 12px;
          cursor: pointer;
          border-radius: 8px;
          margin-bottom: 16px;
          transition: all 0.6s ease-in;
          background-color: rgb(255 255 255 / 80%);
          background-image: linear-gradient(to right bottom, rgb(214 232 255) 0%, rgb(240 247 255) 100%);
          background-size: 200% 100%;
          background-repeat: no-repeat;
          background-position: 200% 0;
          &.active,
          &:hover {
            background-position: 0 0;
            box-shadow: 5px 6px 10px 0 rgb(214 232 255 / 50%);
          }
          .title {
            margin-bottom: 8px;
            width: 100%;
            font-size: 16px;
            color: #333;
          }
          .tag {
            width: 100%;
            font-size: 14px;
            color: #666;
          }
        }
      }
    }
    .main {
      position: relative;
      overflow: hidden;
      margin-left: 16px;
      padding: 12px;
      width: 1000px;
      background: rgb(255 255 255 / 80%);
      box-shadow: 0 6px 12px 0 rgb(31 38 135 / 20%);
      border-radius: 12px;
      border: 1px solid rgb(255 255 255 / 18%);
    }

    .setting {
      padding: 4px;
      position: absolute;
      top: 10px;
      right: 100px;
      height: 28px;
      width: 28px;
      cursor: pointer;
    }

    .openai {
      position: absolute;
      top: 10px;
      right: 10px;
      height: 32px;
      width: 32px;
      cursor: pointer;
    }
  }

  .box-card {
    width: calc(33% - 4px);
    .card-header {
      padding: 8px 0;
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  .base-info {
    width: 100%;
    height: calc(100vh - 173px);
    padding: 0 16px 16px;
    overflow: scroll;
  }
}

.q-icon {
  position: absolute;
  top: -8px;
  right: 8px;
  font-size: 16px;
}

:deep(.el-progress__text) {
  .el-icon {
    font-size: 26px;
  }
}

:deep(.el-divider__text) {
  background-color: transparent;
}
</style>
