<template>
  <div class="document">
    <div v-loading="isLoading" class="contenter">
      <div class="aside">
        <div class="top">
          <el-input v-model="searchValue" class="search-input" placeholder="搜索" :prefix-icon="Search" @submit.prevent @keyup.enter="refresh()" />
          <el-tooltip effect="light" content="新增文档" placement="right">
            <div class="add" @click="addDoc">
              <icon-ep-plus></icon-ep-plus>
            </div>
          </el-tooltip>
        </div>
        <QjInfiniteScroll class="list">
          <div v-for="item in docList" :key="item.id" class="item" :class="{ active: item.id == currentDoc.id }" @click="setCurrentDoc(item)">
            <div class="title ellipsis-1">{{ item.docName }}</div>
            <div v-if="item.tags" style="width: 100%">
              <el-tag v-for="tag in item.tags.split(',')" :key="tag" style="margin: 4px" effect="light">
                {{ tag }}
              </el-tag>
            </div>
            <div v-else class="tag">未设置标签~</div>
            <el-tooltip effect="light" :content="'分段向量化' + getStatusStr(item.embedStatus)" placement="bottom">
              <el-icon class="fenduan-icon" :color="getStatusColor(item.embedStatus)">
                <icon-qj-fenduan></icon-qj-fenduan>
              </el-icon>
            </el-tooltip>
            <el-tooltip effect="light" :content="'总结' + getStatusStr(item.summaryStatus)" placement="bottom">
              <el-icon class="summary-icon" :color="getStatusColor(item.summaryStatus)">
                <icon-qj-zongjie></icon-qj-zongjie>
              </el-icon>
            </el-tooltip>
            <el-tooltip effect="light" :content="'QA问答对' + getStatusStr(item.qaStatus)" placement="bottom">
              <el-icon class="qa-icon" :color="getStatusColor(item.qaStatus)">
                <icon-qj-qa></icon-qj-qa>
              </el-icon>
            </el-tooltip>
          </div>
        </QjInfiniteScroll>
      </div>

      <div class="main">
        <template v-if="currentDoc">
          <el-tabs v-model="activeName" class="demo-tabs">
            <el-tab-pane label="基本信息" name="base">
              <div class="base-info">
                <el-form class="marginT20" :model="docForm" size="large" :label-width="100">
                  <el-form-item label="文档名称">
                    <el-input v-model="docForm.docName" autocomplete="off" style="width: 680px" />
                  </el-form-item>
                  <el-form-item label="标签">
                    <el-select
                      v-model="docForm.tags"
                      multiple
                      filterable
                      allow-create
                      default-first-option
                      :reserve-keyword="false"
                      placeholder="选择或新建标签"
                      style="width: 680px"
                    >
                      <el-option v-for="item in tagsOptions" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                  </el-form-item>
                  <el-form-item v-if="docForm.summary" label="总结">
                    <el-input v-model="docForm.summary" :autosize="{ minRows: 2, maxRows: 4 }" type="textarea" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" plain @click="saveDoc">保存</el-button>
                    <el-button type="danger" plain @click="removeDoc">删除</el-button>
                  </el-form-item>
                </el-form>

                <el-divider style="margin-top: 60px" content-position="left">文档数据</el-divider>
                <div class="d-flex jc-between">
                  <el-card class="box-card">
                    <template #header>
                      <div class="card-header">
                        <span>文本分段</span>
                        <el-button type="primary" :disabled="currentDoc.embedStatus == 1" plain @click="embeddingDoc">
                          {{ currentDoc.embedStatus != 2 ? '保存' : '更新' }}向量数据库
                        </el-button>
                      </div>
                    </template>
                    <div class="d-flex jc-center ai-center">
                      <el-progress type="dashboard" :status="embedProgress.status" :percentage="embedProgress.percentage" :color="colors" />
                    </div>
                  </el-card>
                  <el-card class="box-card">
                    <template #header>
                      <div class="card-header">
                        <span>文本总结</span>
                        <el-button type="primary" :disabled="currentDoc.summaryStatus == 1" plain @click="summaryDoc">
                          {{ currentDoc.summaryStatus != 2 ? '生成' : '更新' }}总结
                        </el-button>
                      </div>
                    </template>
                    <div class="d-flex jc-center ai-center">
                      <el-progress type="dashboard" :status="summaryProgress.status" :percentage="summaryProgress.percentage" :color="colors" />
                    </div>
                  </el-card>
                  <el-card class="box-card">
                    <template #header>
                      <div class="card-header">
                        <span>QA问答对</span>
                        <el-button type="primary" :disabled="currentDoc.qaStatus == 1" plain @click="createQA">
                          {{ currentDoc.qaStatus != 2 ? '生成QA数据' : '更新QA向量数据库' }}
                        </el-button>
                      </div>
                    </template>
                    <div class="d-flex jc-center ai-center">
                      <el-progress type="dashboard" :status="qaProgress.status" :percentage="qaProgress.percentage" :color="colors" />
                    </div>
                  </el-card>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="分段数据" name="segment">
              <ContentList v-if="activeName == 'segment'" :doc-id="currentDoc?.id" />
            </el-tab-pane>
            <el-tab-pane label="QA数据" name="qa">
              <QaList v-if="activeName == 'qa'" :doc-id="currentDoc?.id" />
            </el-tab-pane>
          </el-tabs>
          <el-tooltip effect="light" content="知识库设置" placement="left">
            <icon-ep-Setting class="setting" @click="showEditPrompt = true"></icon-ep-Setting>
          </el-tooltip>
          <el-tooltip effect="light" content="聊天" placement="left">
            <img class="openai" :src="$img('openai.svg')" alt="openAi" @click.stop="goChat()" />
          </el-tooltip>
        </template>
        <template v-else>
          <div>
            <qj-empty>
              <el-button type="warning" @click="showEditPrompt = true">设置 </el-button>

              <el-button type="primary" @click="addDoc"> 添加文档 </el-button>
            </qj-empty>
          </div>
        </template>
      </div>
    </div>

    <el-dialog :model-value="showAdd" title="上传文档" width="580px" center :destroy-on-close="true" @close="showAdd = false">
      <UploadDoc :kb-id="kbId" @success="uploadSuccess()" />
    </el-dialog>

    <KbSetting v-model="showEditPrompt" :kb-id="kbId" />
    <QjWebScoket />
  </div>
</template>
<script setup lang="ts">
/**
 * 文档
 */
import DocApi from '@/apis/docApi';
import { useAsyncTableApi } from '@/hooks/useAsyncApi';
import { Search } from '@element-plus/icons-vue';
import DocContentsApi from '../../apis/docContentsApi';
import { useWSStore } from '../../store';
import ContentList from './components/ContentList.vue';
import KbSetting from './components/KbSetting.vue';
import QaList from './components/QaList.vue';
import UploadDoc from './components/UploadDoc.vue';

const route = useRoute();
const kbId = route.query.kbId as string;

const searchValue = ref('');

const {
  isLoading,
  data: docList,
  refresh
} = useAsyncTableApi(
  (params) => {
    params.kbId = kbId;
    if (searchValue.value) {
      params.searchValue = searchValue.value;
    }
    return DocApi.list(params);
  },
  {
    onSuccess: (data) => {
      if (data && data.length > 0 && !currentDoc.value) {
        setCurrentDoc(data[0]);
      }
    }
  }
);

const showAdd = ref(false);
const currentDoc = ref();

const docForm = reactive({ id: null, docName: '', tags: [], summary: '' });
const tagsOptions = [
  {
    value: '上市公司',
    label: '上市公司'
  },
  {
    value: '互动问答',
    label: '互动问答'
  },
  {
    value: '年报',
    label: '年报'
  }
];
function addDoc() {
  showAdd.value = true;
}

function uploadSuccess() {
  currentDoc.value = null;
  refresh();
  showAdd.value = false;
}

function setCurrentDoc(doc) {
  currentDoc.value = { ...doc };
  docForm.id = doc.id;
  docForm.docName = doc.docName;
  docForm.tags = doc.tags ? doc.tags.split(',') : [];
  docForm.summary = doc.summary;
  getEmbedProgress(doc.id);
  getSummaryProgress(doc.id);
  getQAProgress(doc.id);
}

function saveDoc() {
  DocApi.update({ id: docForm.id, docName: docForm.docName, tags: docForm.tags.join(','), summary: docForm.summary }).then(() => {
    ElMessage.success('保存成功！');
    refresh();
  });
}

function removeDoc() {
  ElMessageBox.confirm('确认删除当前文档吗？').then(() => {
    DocApi.remove(docForm.id).then((data) => {
      if (data) {
        ElMessage.success('删除成功！');
      }
      currentDoc.value = null;
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

function embeddingDoc() {
  DocContentsApi.embedding(currentDoc.value.id).then((res) => {
    if (res) {
      updateDocInfo('embedStatus', 1);
      ElMessage.success('分段文档向量化中，请稍等');
    }
  });
}

const embedProgress = reactive<any>({
  status: '',
  percentage: 0
});

function getEmbedProgress(docId) {
  embedProgress.status = '';
  embedProgress.percentage = 0;
  DocContentsApi.embeddingStatus(docId).then((res) => {
    if (res.docId == currentDoc.value.id) {
      updateDocInfo('embedStatus', res.embedStatus);
      embedProgress.status = res.status;
      embedProgress.percentage = res.percentage;
    }
  });
}

function summaryDoc() {
  DocContentsApi.summary(currentDoc.value.id).then((res) => {
    if (res) {
      updateDocInfo('summaryStatus', 1);
      ElMessage.success('文档总结中，请稍等');
    }
  });
}

const summaryProgress = reactive<any>({
  status: '',
  percentage: 0
});

function getSummaryProgress(docId) {
  summaryProgress.status = '';
  summaryProgress.percentage = 0;
  DocContentsApi.summaryStatus(docId).then((res) => {
    if (res.docId == currentDoc.value.id) {
      updateDocInfo('summaryStatus', res.summaryStatus);
      summaryProgress.status = res.status;
      summaryProgress.percentage = res.percentage;
    }
  });
}

const wsStore = useWSStore();
wsStore.connectWs();

const onEmbedProgress = (data) => {
  if (data.docId == currentDoc.value.id) {
    embedProgress.percentage = data.percentage;
    embedProgress.status = data.status;
    if (data.status == 'success') {
      updateDocInfo('embedStatus', 2);
    } else if (data.status == 'exception') {
      updateDocInfo('embedStatus', 3);
    } else {
      updateDocInfo('embedStatus', 1);
    }
  }
};

wsStore.emitter.on('embedProgress', onEmbedProgress);

const onSummaryProgress = (data) => {
  if (data.docId == currentDoc.value.id) {
    summaryProgress.percentage = data.percentage;
    summaryProgress.status = data.status;
    if (data.status == 'success') {
      updateDocInfo('summaryStatus', 2);
      updateDocInfo('summary', data.summary);
      docForm.summary = data.summary;
    } else if (data.status == 'exception') {
      updateDocInfo('summaryStatus', 3);
    } else {
      updateDocInfo('summaryStatus', 1);
    }
  }
};

wsStore.emitter.on('summaryProgress', onSummaryProgress);

const qaProgress = reactive<any>({
  status: '',
  percentage: 0
});

function getQAProgress(docId) {
  qaProgress.status = '';
  qaProgress.percentage = 0;
  DocContentsApi.qaStatus(docId).then((res) => {
    if (res.docId == currentDoc.value.id) {
      updateDocInfo('qaStatus', res.qaStatus);
      qaProgress.status = res.status;
      qaProgress.percentage = res.percentage;
    }
  });
}

function createQA() {
  DocContentsApi.createQA(currentDoc.value.id).then((res) => {
    if (res) {
      updateDocInfo('qaStatus', 1);
      ElMessage.success('创建QA问答对中，请稍等');
    }
  });
}

const onQaProgress = (data) => {
  if (data.docId == currentDoc.value.id) {
    qaProgress.percentage = data.percentage;
    qaProgress.status = data.status;
    if (data.status == 'success') {
      updateDocInfo('qaStatus', 2);
    } else if (data.status == 'exception') {
      updateDocInfo('qaStatus', 3);
    } else {
      updateDocInfo('qaStatus', 1);
    }
  }
};
wsStore.emitter.on('qaProgress', onQaProgress);

function updateDocInfo(key: string, value: any) {
  if (currentDoc.value && currentDoc.value[key] != value) {
    currentDoc.value[key] = value;
    docList.value = docList.value.map(function (item) {
      if (currentDoc.value.id == item.id) {
        return currentDoc.value;
      }
      return item;
    });
  }
}

const router = useRouter();
function goChat() {
  router.push({ path: '/chat', query: { kbId } });
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

const showEditPrompt = ref(false);

onUnmounted(() => {
  wsStore.emitter.off('embedProgress', onEmbedProgress);
  wsStore.emitter.off('summaryProgress', onSummaryProgress);
  wsStore.emitter.off('qaProgress', onQaProgress);
});
</script>

<style lang="scss" scoped>
.document {
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
      background: rgba(255, 255, 255, 0.7);
      box-shadow: 0 6px 12px 0 rgba(31, 38, 135, 0.2);
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
          --el-input-bg-color: rgba(255, 255, 255, 0.6);
          --el-input-border: none;
          --el-input-border-color: transparent;
          :deep(.el-input__wrapper) {
            background: rgba(255, 255, 255, 0.55);
            box-shadow: 0 8px 32px 0 rgba(240, 240, 247, 0.37);
            backdrop-filter: blur(4px);
            -webkit-backdrop-filter: blur(4px);
            border-radius: 12px 0px 0px 12px;
            border: 1px solid rgba(255, 255, 255, 0.18);
          }
          :deep(.el-input__inner) {
            &::input-placeholder {
              color: #999;
            }
            &::-webkit-input-placeholder {
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
          border-radius: 0 12px 12px 0px;
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
          background-color: rgba(255, 255, 255, 0.8);
          background-image: linear-gradient(to right bottom, rgb(214, 232, 255) 0%, rgb(240, 247, 255) 100%);
          background-size: 200% 100%;
          background-repeat: no-repeat;
          background-position: 200% 0;
          &.active,
          &:hover {
            background-position: 0 0;
            box-shadow: 5px 6px 10px 0px rgba(214, 232, 255, 0.5);
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
      background: rgba(255, 255, 255, 0.8);
      box-shadow: 0 6px 12px 0 rgba(31, 38, 135, 0.2);
      border-radius: 12px;
      border: 1px solid rgba(255, 255, 255, 0.18);
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

.fenduan-icon {
  position: absolute;
  top: -8px;
  right: 8px;
  font-size: 16px;
}

.summary-icon {
  position: absolute;
  top: -8px;
  right: 32px;
  font-size: 16px;
}

.qa-icon {
  position: absolute;
  top: -8px;
  right: 56px;
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
