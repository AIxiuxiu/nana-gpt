<template>
  <div class="content-list">
    <div v-loading="isLoading" class="list">
      <div v-for="item in dataList" :key="item.id" class="item" @click="edit(item)">
        <div class="content">{{ item.content }}</div>
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

    <el-dialog v-model="showEdit" title="编辑分段内容" width="880px" top="10vh" append-to-body center>
      <el-input v-if="currentEdit.summary" v-model="currentEdit.summary" class="marginB10" :autosize="{ minRows: 2, maxRows: 6 }" type="textarea" placeholder="请输入" />
      <el-input v-model="currentEdit.content" :autosize="{ minRows: 8, maxRows: 20 }" type="textarea" placeholder="请输入" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEdit = false">取消</el-button>
          <el-button type="primary" @click="submitEdit()"> 提交 </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 分段内容
 */
import DocContentsApi from '@/apis/docContentsApi';
import { useAsyncTableApi } from '@/hooks/useAsyncApi';

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
    return DocContentsApi.list({ ...params, docId: props.docId });
  },
  {
    immediate: false
  }
);

const showEdit = ref(false);
const currentEdit = reactive({
  id: '',
  content: '',
  summary: '',
  embedStatus: 0
});

function edit(item) {
  currentEdit.id = item.id;
  currentEdit.content = item.content;
  currentEdit.summary = item.summary;
  currentEdit.embedStatus = item.embedStatus;
  showEdit.value = true;
}

function submitEdit() {
  DocContentsApi.addOrUpdate({ id: currentEdit.id, content: currentEdit.content }).then((res) => {
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
    DocContentsApi.remove(item.id).then((data) => {
      if (data) {
        ElMessage.success('删除成功！');
      }
      refresh();
    });
  });
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
    overflow: hidden;
    -webkit-line-clamp: 6;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
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
</style>
