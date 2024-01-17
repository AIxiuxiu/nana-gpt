<template>
  <div class="content-list">
    <div v-loading="isLoading" class="list">
      <div v-for="item in dataList" :key="item.id" class="item" @click="edit(item)">
        <div class="content">
          <div class="question">{{ item.question }}</div>
          <div class="answer">{{ item.answer }}</div>
        </div>
        <div class="footer">
          <div>
            <span>tokens</span>
            <span class="blue marginL5">{{ item.tokens }}</span>
          </div>
          <el-icon class="icon" :size="18" color="#666" @click.stop="remove(item)">
            <icon-ep-delete></icon-ep-delete>
          </el-icon>
        </div>
      </div>
    </div>
    <qj-pagination />

    <el-dialog v-model="showEdit" title="编辑问答对" width="880px" top="10vh" append-to-body center>
      <el-input v-model="currentEdit.question" type="textarea" :autosize="{ minRows: 2, maxRows: 10 }" style="margin-bottom: 16px" placeholder="请输入问题" />
      <el-input v-model="currentEdit.answer" type="textarea" :autosize="{ minRows: 8, maxRows: 20 }" placeholder="请输入答案" />

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEdit = false">取消</el-button>
          <el-button type="primary" @click="submitEdit()"> 提交 </el-button>
        </span>
      </template>
    </el-dialog>
    <el-popover placement="bottom" :width="100" trigger="hover">
      <template #reference>
        <el-button class="download" circle size="small" type="primary" :icon="Download" />
      </template>
      <template #default>
        <div class="d-flex flex-column">
          <div style="text-align: center">导出下载</div>
          <el-button size="small" link type="primary" @click="downloadQa('excel')">excel文件</el-button>
          <el-button style="margin: 0" size="small" link type="primary" @click="downloadQa('word')">word文件</el-button>
          <el-button style="margin: 0" size="small" link type="primary" @click="downloadQa('txt')">txt文件</el-button>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
/**
 * 分段内容
 */
import QaApi from '@/apis/qaApi';
import { useAsyncTableApi } from '@/hooks/useAsyncApi';
import { Download } from '@element-plus/icons-vue';

const props = defineProps({
  docId: {
    type: String,
    default: ''
  }
});

const {
  isLoading,
  data: dataList,
  refresh
} = useAsyncTableApi(
  (params) => {
    return QaApi.list({ ...params, docId: props.docId });
  },
  {
    immediate: false
  }
);

const showEdit = ref(false);
const currentEdit = reactive({
  id: '',
  question: '',
  answer: '',
  embedStatus: 0
});

function edit(item) {
  currentEdit.id = item.id;
  currentEdit.question = item.question;
  currentEdit.answer = item.answer;
  currentEdit.embedStatus = item.embedStatus;
  showEdit.value = true;
}

function submitEdit() {
  QaApi.addOrUpdate({ id: currentEdit.id, question: currentEdit.question, answer: currentEdit.answer }).then((res) => {
    if (res) {
      if (currentEdit.embedStatus == 1) {
        ElMessage.warning('编辑成功, 需重新生成向量数据库！');
      } else {
        ElMessage.success('编辑成功！');
      }
      refresh();
      showEdit.value = false;
    }
  });
}

function remove(item) {
  ElMessageBox.confirm('确认删除当前文本吗？').then(() => {
    QaApi.remove(item.id).then((data) => {
      if (data) {
        ElMessage.success('删除成功！');
      }
      refresh();
    });
  });
}

function downloadQa(type) {
  window.open(import.meta.env.VITE_APP_API_BASEURL?.toString() + 'qa/export' + `?docId=${props.docId}&fileType=${type}`, '_blank');
}

watch(
  () => props.docId,
  (val) => {
    val && refresh();
  },
  { immediate: true }
);
</script>

<style lang="scss" scoped>
.content-list {
  height: 100%;
}
.list {
  width: 100%;
  height: calc(100vh - 213px);
  padding: 16px;
  overflow: scroll;
}
.item {
  margin-bottom: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  position: relative;
  background-color: #f0f0f0;
  border-radius: 12px;
  background-image: linear-gradient(145deg, #fff, #f0f0f0);
  border: 1px solid #f0f0f0;
  box-shadow: 2px 2px 1px rgba(0, 0, 0, 0.029), 4px 4px 4px rgba(0, 0, 0, 0.039);
  transition: all 0.3s ease-in-out;
  cursor: pointer;
  .content {
    font-size: 14px;
    color: #333;
    .question {
      font-size: 15px;
      margin-bottom: 10px;
    }
    .answer {
      color: #666;
    }
  }
  .footer {
    margin-top: 10px;
    display: flex;
    height: 22px;
    align-items: center;
    justify-content: space-between;
    .icon {
      cursor: pointer;
      transition: opacity 0.3s;
      opacity: 0;
      &:hover {
        color: $error-color;
      }
    }
  }

  &:hover {
    border: 1px solid #e2e2e2;
    box-shadow: 4px 4px 3px rgba(0, 0, 0, 0.032), 7px 7px 7px rgba(0, 0, 0, 0.049);
    .icon {
      opacity: 1;
    }
  }
}

.download {
  position: absolute;
  bottom: 50%;
  right: 10px;
  width: 32px;
  height: 32px;
  font-size: 16px;
}
</style>
