<template>
  <div class="setting">
    <el-tabs v-model="activeName" @tab-change="handleTabChange">
      <el-tab-pane label="系统提示词" name="0"> </el-tab-pane>
      <el-tab-pane label="用户使用提示词" name="1"> </el-tab-pane>
    </el-tabs>

    <div v-loading="isLoading" class="list">
      <template v-for="item in dataList" :key="item.id">
        <div class="item" @click="edit(item)">
          <div class="question">{{ item.topic }}</div>
          <div class="content">
            <div class="answer">{{ item.content }}</div>
          </div>

          <el-icon class="icon" :size="18" color="#666" @click.stop="remove(item)">
            <icon-ep-delete></icon-ep-delete>
          </el-icon>
        </div>
      </template>
    </div>

    <el-button v-if="activeName == '1'" class="add-prompt" size="small" type="primary" round @click="addPrompt">新增提示词</el-button>

    <el-dialog v-model="showEdit" title="编辑" width="880px" top="10vh" append-to-body center>
      <el-input v-model="currentEdit.content" :autosize="{ minRows: 6, maxRows: 12 }" type="textarea" placeholder="请输入prompt" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEdit = false">取消</el-button>
          <el-button type="primary" @click="submitEdit()"> 提交 </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showAdd" title="新增" width="880px" top="10vh" append-to-body center>
      <div>简单描述</div>
      <el-input v-model="addForm.topic" :autosize="{ minRows: 1, maxRows: 2 }" type="textarea" placeholder="请输入描述" />
      <div class="marginT10">具体提示词</div>
      <el-input v-model="addForm.content" :autosize="{ minRows: 6, maxRows: 12 }" type="textarea" placeholder="请输入prompt" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAdd = false">取消</el-button>
          <el-button type="primary" @click="submitAdd()"> 提交 </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import PromptApi from '@/apis/promptApi';
import { useAsyncApi } from '@/hooks/useAsyncApi';

const activeName = ref('1');

/**
 * 设置
 */
const {
  isLoading,
  data: dataList,
  refresh
} = useAsyncApi(
  () => {
    return PromptApi.list(activeName.value);
  },
  {
    initialData: []
  }
);

const showEdit = ref(false);
const currentEdit = reactive({
  id: '',
  content: ''
});

function edit(item) {
  currentEdit.id = item.id;
  currentEdit.content = item.content;
  showEdit.value = true;
}

function submitEdit() {
  PromptApi.update({ id: currentEdit.id, content: currentEdit.content }).then((res) => {
    if (res) {
      refresh();
      showEdit.value = false;
    }
  });
}

function handleTabChange() {
  refresh();
}

const showAdd = ref(false);
const addForm = reactive({
  content: '',
  topic: ''
});
function addPrompt() {
  addForm.content = '';
  addForm.topic = '';
  showAdd.value = true;
}

function submitAdd() {
  PromptApi.add({ ...addForm }).then((res) => {
    if (res) {
      refresh();
      showAdd.value = false;
    }
  });
}

function remove(item) {
  ElMessageBox.confirm('确认删除当前提示词吗？').then(() => {
    PromptApi.remove(item.id).then((data) => {
      if (data) {
        ElMessage.success('删除成功！');
      }
      refresh();
    });
  });
}
</script>

<style lang="scss" scoped>
.setting {
  position: relative;
  margin: 0 auto;
  max-width: 900px;
  width: 100%;
  height: 100%;
}

.list {
  width: 100%;
  height: calc(100vh - 94px);
  padding: 16px;
  overflow: scroll;
}

.item {
  position: relative;
  margin-bottom: 16px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  position: relative;
  border-radius: 12px;
  background-color: #f0f0f0;
  background-image: linear-gradient(145deg, #fff, #f0f0f0);
  border: 1px solid #f0f0f0;
  box-shadow: 2px 2px 1px rgb(0 0 0 / 2.9%), 4px 4px 4px rgb(0 0 0 / 3.9%);
  transition: all 0.3s ease-in-out;
  cursor: pointer;
  .question {
    font-size: 15px;
    margin-bottom: 6px;
  }

  .content {
    font-size: 14px;
    color: #333;

    .answer {
      color: #666;
      overflow: hidden;
      -webkit-line-clamp: 4;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-box-orient: vertical;
    }
  }

  .icon {
    position: absolute;
    top: 8px;
    right: 14px;
    cursor: pointer;
    transition: opacity 0.3s;
    opacity: 0;
    &:hover {
      color: $error-color;
    }
  }

  &:hover {
    border: 1px solid #e2e2e2;
    box-shadow: 4px 4px 3px rgb(0 0 0 / 3.2%), 7px 7px 7px rgb(0 0 0 / 4.9%);
    .icon {
      opacity: 1;
    }
  }
}

.add-prompt {
  position: absolute;
  top: 2px;
  right: 10px;
  cursor: pointer;
}
</style>
