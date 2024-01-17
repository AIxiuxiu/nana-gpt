<template>
  <div class="kb">
    <!-- <div class="user" @click="goUser">
      <icon-ep-User></icon-ep-User>
    </div>
    <div class="setting" @click="goSetting">
      <icon-ep-Setting></icon-ep-Setting>
    </div> -->
    <div class="search">
      <el-input v-model="searchValue" class="search-input" placeholder="搜索" :prefix-icon="Search" @submit.prevent @keyup.enter="refresh()" />
      <div class="add" title="新增知识库" @click="addKB">
        <icon-ep-plus></icon-ep-plus>
      </div>
    </div>
    <div class="card-list">
      <div v-for="item in docList" :key="item.id" class="crad-box">
        <div class="card" @click="goDocument(item)">
          <el-tooltip effect="light" content="聊天" placement="top">
            <img class="openai" :src="$img('openai.svg')" alt="openAi" @click.stop="goChat(item)" />
          </el-tooltip>

          <div class="title">{{ item.kbName }}</div>
          <div class="info">
            {{ item.companyName }}
            <span v-if="item.companyCode">（{{ item.companyCode }}）</span>
          </div>
          <div v-if="item.tags">
            <el-tag v-for="tag in item.tags.split(',')" :key="tag" style="margin: 4px" effect="light">
              {{ tag }}
            </el-tag>
          </div>
          <div circle class="more" @click.stop="">
            <el-dropdown placement="bottom" @command="handClick(item, $event)">
              <el-button class="btn" :icon="Setting"> </el-button>
              <template #dropdown>
                <el-dropdown-item v-for="(item1, index) in ['编辑', '删除']" :key="index" :command="item1">
                  <div>{{ item1 }}</div>
                </el-dropdown-item>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>
    <el-dialog v-model="showAdd" :title="currentKbId ? '编辑知识库' : '添加知识库'" width="520px" center>
      <el-form :model="kbForm" size="large" :label-width="100">
        <el-form-item label="知识库名称">
          <el-input v-model="kbForm.kbName" autocomplete="off" style="width: 300px" />
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input v-model="kbForm.companyName" autocomplete="off" style="width: 300px" />
        </el-form-item>
        <el-form-item label="公司代码">
          <el-input v-model="kbForm.companyCode" autocomplete="off" style="width: 300px" />
        </el-form-item>
        <el-form-item label="分类标签">
          <el-select v-model="kbForm.tags" multiple filterable allow-create default-first-option :reserve-keyword="false" placeholder="选择或新建标签" style="width: 300px">
            <el-option v-for="item in tagsOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAdd = false">取消</el-button>
          <el-button type="primary" @click="submitKb()"> 提交 </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
/**
 * 首页
 */
import KbApi from '@/apis/kbApi';
import { useAsyncApi } from '@/hooks/useAsyncApi';
import { Search, Setting } from '@element-plus/icons-vue';

const searchValue = ref('');

const {
  isLoading,
  data: docList,
  refresh
} = useAsyncApi(
  () => {
    return KbApi.list(searchValue.value);
  },
  {
    initialData: []
  }
);

const showAdd = ref(false);
const currentKbId = ref();
const kbForm = reactive({ kbName: '', companyName: '', companyCode: '', tags: [] });
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

function addKB() {
  currentKbId.value = null;
  kbForm.kbName = '';
  kbForm.companyName = '';
  kbForm.companyCode = '';
  kbForm.tags = [];
  showAdd.value = true;
}

function submitKb() {
  const params: any = { kbName: kbForm.kbName, companyName: kbForm.companyName, companyCode: kbForm.companyCode, tags: kbForm.tags.join(',') };
  if (currentKbId.value) {
    params.id = currentKbId.value;
  }
  KbApi.addOrUpdate(params).then(() => {
    if (currentKbId.value) {
      ElMessage.success('编辑成功！');
    } else {
      ElMessage.success('创建成功！');
    }
    refresh();
    showAdd.value = false;
  });
}

function handClick(item, event) {
  if (event == '编辑') {
    currentKbId.value = item.id;
    kbForm.kbName = item.kbName;
    kbForm.companyName = item.companyName;
    kbForm.companyCode = item.companyCode;
    kbForm.tags = item.tags.split(',');
    showAdd.value = true;
  } else if (event == '删除') {
    ElMessageBox.confirm('确认删除当前知识库吗').then(() => {
      KbApi.remove(item.id).then((data) => {
        if (data) {
          ElMessage.success('删除成功！');
        }
        refresh();
      });
    });
  }
}

const router = useRouter();
function goDocument(item) {
  router.push({ path: '/document', query: { kbId: item.id } });
}

function goChat(item) {
  router.push({ path: '/chat', query: { kbId: item.id } });
}

function goUser() {
  router.push({ path: '/user' });
}

function goSetting() {
  router.push({ path: '/setting' });
}
</script>

<style lang="scss" scoped>
.kb {
  width: 100%;
  height: 100%;
  .user {
    position: absolute;
    top: 24px;
    right: 10%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    width: 36px;
    height: 36px;
    border-radius: 18px;
    background-color: rgb(39, 140, 240, 0.8);
    cursor: pointer;
    &:hover {
      opacity: 0.8;
    }
  }

  .setting {
    position: absolute;
    top: 24px;
    left: 10%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    width: 36px;
    height: 36px;
    border-radius: 18px;
    background-color: rgb(39, 140, 240, 0.8);
    cursor: pointer;
    &:hover {
      opacity: 0.8;
    }
  }

  .search {
    margin: 62px 0;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    .search-input {
      width: 380px;
      font-size: 18px;
      --el-input-text-color: #333;
      --el-input-icon-color: #666;
      --el-input-height: 48px;
      --el-input-bg-color: rgba(255, 255, 255, 0.6);
      --el-input-border: none;
      --el-input-border-color: transparent;
      :deep(.el-input__wrapper) {
        background: rgba(255, 255, 255, 0.55);
        box-shadow: 0 8px 32px 0 rgba(240, 240, 247, 0.37);
        backdrop-filter: blur(4px);
        -webkit-backdrop-filter: blur(4px);
        border-radius: 10px;
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
      margin-left: 16px;
      height: 48px;
      width: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      color: #fff;
      border-radius: 16px;
      background: #278cf0;
      background-image: linear-gradient(45deg, #4ce2a7 0%, #278cf0 100%);
      border-radius: 50% 50% 50% 50% / 34% 34% 66% 66%;
      box-shadow: -2px 2px #ffffff12;
      opacity: 0.9;
      box-shadow: 2.5px 2.5px 5.3px rgba(0, 0, 0, 0.013), 8.1px 8.1px 17.9px rgba(0, 0, 0, 0.017), 45px 45px 80px rgba(0, 0, 0, 0.03);
      transition: all 0.3s;
      cursor: pointer;
      &:hover {
        opacity: 0.7;
      }
    }
  }

  .card-list {
    margin: 0 auto;
    width: 1136px;
    padding: 10px 0;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    background-color: transparent;
  }

  .crad-box {
    margin: 18px;
    &:last-child {
      margin-right: auto;
    }
  }
  .card {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    padding: 18px;
    width: 248px;
    height: 180px;
    cursor: pointer;
    background: rgba(255, 255, 255, 0.5);
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
    backdrop-filter: blur(5px);
    -webkit-backdrop-filter: blur(5px);
    border-radius: 10px;
    border: 1px solid rgba(255, 255, 255, 0.18);

    .title {
      font-size: 22px;
      color: $text-color-primary;
      margin-bottom: 10px;
    }
    .info {
      margin-bottom: 10px;
      font-size: 14px;
      color: #666;
    }
    .more {
      position: absolute;
      bottom: 2px;
      right: 3px;
      .btn {
        background: transparent;
        border: none;
      }
    }
    .openai {
      position: absolute;
      top: 5px;
      right: 5px;
      height: 30px;
      width: 30px;
      cursor: pointer;
    }
  }
}
</style>
