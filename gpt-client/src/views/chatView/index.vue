<template>
  <div class="chat-wrapper">
    <el-tooltip effect="light" content="清空记录" placement="bottom">
      <el-icon class="clean-icon" :size="30" @click="cleanHistory()">
        <icon-qj-qingli></icon-qj-qingli>
      </el-icon>
    </el-tooltip>

    <div class="setting">
      <Prompt v-model="rolePrompt" />
      <div class="search-setting">
        <div class="label">联网搜索</div>
        <el-select v-model="search.searchType" size="small" placeholder="联网搜索">
          <el-option v-for="item in searchOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-row class="marginT5" align="middle">
          <el-col :span="16">
            <el-input-number v-model="search.searchCount" style="width: 90px" size="small" :min="1" :max="10" />
          </el-col>
          <el-col :span="8">
            <el-switch v-model="search.searchDepth" inline-prompt size="small" active-value="full" active-text="详情" inactive-value="quick" inactive-text="快照" />
          </el-col>
        </el-row>
      </div>
      <el-button class="btn" type="primary" size="small" @click="showSeting = true">
        <span style="margin-right: 2px">设置</span>
        <el-icon :size="16">
          <icon-ep-setting></icon-ep-setting>
        </el-icon>
      </el-button>
    </div>
    <div class="content">
      <el-scrollbar ref="scrollbarRef" class="chat-box" style="flex: 1">
        <div ref="innerRef" v-loading="loading">
          <div v-for="item in chatData" :key="item.id">
            <ChatPrompt v-if="item.role === 'USER'" :create-time="item.createTime" :tokens="item['tokens']" :content="item.content" />
            <ChatReply v-else-if="item.role === 'SYSTEM'" :icon="item.icon" :create-time="item.createTime" :tokens="item['tokens']" :content="item.content" :prompt="item.prompt" />
          </div>
        </div>
      </el-scrollbar>
      <footer class="chat-footer">
        <div class="re-generate">
          <div class="btn-box">
            <div v-if="showWaitTip" class="tip">等待回复中，请稍后...</div>
            <el-button v-if="false" type="info" plain>
              <el-icon><icon-ep-VideoPause /></el-icon>
              停止生成
            </el-button>
            <el-button v-if="false" type="primary" plain>
              <el-icon><icon-ep-RefreshRight /></el-icon>
              重新生成
            </el-button>
          </div>
        </div>
        <div class="input-box">
          <div class="input-container">
            <el-input
              ref="text-input"
              v-model="prompt"
              autofocus
              type="textarea"
              :rows="4"
              :maxlength="5000"
              show-word-limit
              resize="none"
              placeholder="按 Enter 键发送消息，使用 Ctrl + Enter 换行"
              @keydown="inputKeyDown"
            />
            <span class="send-btn">
              <el-button @click="sendMessage">
                <el-icon><icon-ep-Promotion /></el-icon>
              </el-button>
            </span>
          </div>
        </div>
      </footer>
    </div>

    <QjWebScoket />

    <UserSetting v-model="showSeting" />
  </div>
</template>

<script setup lang="ts">
/**
 * 聊天界面
 */

import ChatApi from '@/apis/chatApi';
import useTypewriter from '@/hooks/useTypewriter';
import dayjs from 'dayjs';
import { ElScrollbar } from 'element-plus';
import { nextTick, ref } from 'vue';
import { useWSStore } from '../../store';
import ChatPrompt from '../chat/components/ChatPrompt.vue';
import ChatReply from '../chat/components/ChatReply.vue';
import Prompt from './Prompt.vue';
import UserSetting from './UserSetting.vue';

const kbId = '';
const rolePrompt = ref('');

const loading = ref(false);
const chatData = ref<any>([]);

function getHistory() {
  loading.value = true;
  // 加载会话
  ChatApi.history(kbId)
    .then((data) => {
      if (data) {
        chatData.value = data;
      }
    })
    .finally(() => {
      loading.value = false;
      setScrollBottom();
    });
}

getHistory();

const prompt = ref('');
const canSend = ref(true);
const showWaitTip = ref(false);
const scrollbarRef = ref<InstanceType<typeof ElScrollbar>>();
const innerRef = ref<HTMLDivElement>();

// 输入框输入事件处理
const inputKeyDown = function (e) {
  if (e.keyCode === 13) {
    if (e.ctrlKey) {
      // Ctrl + Enter 换行
      prompt.value += '\n';
      return;
    }
    e.preventDefault();
    sendMessage();
  }
};

// 发送消息
const sendMessage = function () {
  if (canSend.value === false) {
    ElMessage.warning('AI 正在作答中，请稍后...');
    return;
  }
  if (prompt.value.trim().length === 0) {
    return false;
  }

  // 追加消息
  chatData.value.push({
    role: 'USER',
    content: prompt.value,
    createTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
  });

  setScrollBottom();

  canSend.value = false;
  showWaitTip.value = true;
  //DOTO:
  wsStore.sendMessage({
    type: 'chat',
    flag: true,
    role: 'user',
    kbId,
    message: prompt.value,
    replyId: '',
    status: '',
    prompt: rolePrompt.value,
    search: search.searchType,
    searchCount: search.searchCount,
    searchDepth: search.searchDepth
  });

  prompt.value = '';
  return true;
};

function setScrollBottom() {
  if (!scrollbarRef.value || !innerRef.value) {
    return;
  }
  nextTick(() => {
    scrollbarRef.value!.setScrollTop(innerRef.value!.clientHeight);
  });
}

const wsStore = useWSStore();
wsStore.connectWs();
const lineBuffer = ref(''); // 输出缓冲行

const typewriter = useTypewriter((str: string, tokens: number) => {
  lineBuffer.value += str || '';
  nextTick(() => {
    if (lineBuffer.value != '') {
      const reply = chatData.value[chatData.value.length - 1];
      reply['content'] = lineBuffer.value;
      if (tokens) {
        reply['tokens'] = tokens;
        lineBuffer.value = '';
        ChatApi.saveChat({ kbId, role: reply.role, content: reply.content, tokens: reply.tokens, prompt: reply.prompt }).then(() => {
          console.log('回复消息保存成功！');
        });
      }
    }
  });
});

const onChat = (data) => {
  //回答
  if (data.kbId == kbId) {
    showWaitTip.value = false;
    if (data.status === 'send') {
      const sendData = chatData.value[chatData.value.length - 1];
      if ((sendData['content'] = data.message)) {
        sendData.tokens = data.tokens;
      }
    } else if (data.status === 'start') {
      lineBuffer.value = ''; // 清空缓冲
      chatData.value.push({
        role: data.role,
        id: data.replyId,
        content: '',
        prompt: data.message,
        createTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
      });
      typewriter.start();
    } else if (data.status === 'end') {
      // 消息接收完毕
      canSend.value = true;
      typewriter.done();
    } else if (data.status === 'failure') {
      // 消息接收失败
      canSend.value = true;
      typewriter.done();
      ElMessage.error('消息接收失败！');
    } else if (data.status === 'empty') {
      chatData.value.push({
        role: data.role,
        id: data.replyId,
        content: data.message,
        createTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
      });
      // 消息接收失败
      canSend.value = true;
    } else {
      typewriter.add(data.message);
    }
    // 将聊天框的滚动条滑动到最底部
    setScrollBottom();
  }
};

wsStore.emitter.on('chat', onChat);

function cleanHistory() {
  ChatApi.cleanHistory(kbId).then((data) => {
    if (data) {
      getHistory();
      ElMessage.success('清理成功');
    }
  });
}

const showSeting = ref(false);

const search = reactive({
  searchType: '',
  searchCount: 3,
  searchDepth: 'quick'
});

const searchOptions = ref([
  { label: '禁止搜索', value: '' },
  { label: '谷歌搜索', value: 'google' },
  { label: '必应搜索', value: 'bing' },
  { label: '必应中国搜索', value: 'cnBing' },
  { label: 'duckduckgo搜索', value: 'duckduckgo' }
]);

onUnmounted(() => {
  wsStore.emitter.off('chat', onChat);
});
</script>

<style lang="scss" scoped>
.chat-wrapper {
  position: relative;
  overflow: hidden;
  padding: 12px 38px 16px;
  width: 1200px;
  height: calc(100vh - 40px);
  margin: 0 auto;
  display: flex;
  flex-direction: row;
  justify-content: center;
  .content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .chat-box {
    background-color: rgb(254, 254, 254, 0.6);
    font-size: 16px;
    width: 100%;
    padding: 10px 10px 30px 10px;
    margin-bottom: 24px;
    border-radius: 6px;
    box-shadow: rgba(0, 0, 0, 0.1) 0px 2px 10px;
  }
}

.re-generate {
  position: relative;
  display: flex;
  justify-content: center;

  .btn-box {
    position: absolute;
    bottom: 6px;
    .tip {
      font-size: 14px;
      color: #999;
    }

    .el-button {
      .el-icon {
        margin-right: 5px;
      }
    }
  }
}

.input-box {
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: rgba(0, 0, 0, 0.1) 0px 2px 10px;
  border-radius: 6px;
  overflow: hidden;

  .input-container {
    width: 100%;
    margin: 0;
    border: none;
    display: flex;
    justify-content: center;
    position: relative;

    :deep(.el-textarea) {
      .el-textarea__inner {
        box-shadow: none;
      }
      .el-textarea__inner::-webkit-scrollbar {
        width: 0;
        height: 0;
      }
    }

    .send-btn {
      position: absolute;
      right: 12px;
      top: 20px;

      .el-button {
        height: 32px;
        padding: 8px 5px;
        border-radius: 6px;
        background: rgb(25, 195, 125);
        color: #ffffff;
        font-size: 20px;
      }
    }
  }
  :deep(.el-textarea__inner) {
    background-color: rgb(254, 254, 254, 0.6);
  }
  :deep(.el-input__count) {
    background-color: transparent;
  }
}

.clean-icon {
  position: absolute;
  right: 0px;
  top: 8px;
  cursor: pointer;
  &:hover {
    opacity: 0.8;
  }
}

.setting {
  margin-right: 12px;
  height: 100%;
  width: 200px;
  flex-shrink: 0;
  background-color: rgba(254, 254, 278, 0.6);
  padding: 10px;
  border-radius: 6px;
  box-shadow: rgba(0, 0, 0, 0.1) 0px 2px 10px;

  .btn {
    margin-top: 10px;
    width: 100%;
  }

  .search-setting {
    display: flex;
    flex-direction: column;
    margin: 0 -10px;
    padding: 8px 10px;
    background-color: rgba(39, 140, 240, 0.1);
    .label {
      color: #333;
      margin-bottom: 4px;
    }
  }

  :deep(.el-input__wrapper) {
    background-color: rgb(254, 254, 254, 0.8);
  }
  :deep(.el-textarea__inner) {
    background-color: rgb(254, 254, 254, 0.8);
    height: 100%;
  }
}
</style>
