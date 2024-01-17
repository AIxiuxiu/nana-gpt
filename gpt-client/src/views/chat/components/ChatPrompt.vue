<template>
  <div class="chat-line-prompt">
    <div class="chat-line-inner">
      <div class="chat-icon">
        <img :src="$img(icon)" alt="User" />
      </div>
      <div class="chat-content">
        <div class="content" v-html="showContent"></div>
        <div v-if="createTime !== ''" class="bar">
          <span class="bar-item">
            <el-icon><icon-ep-clock /></el-icon> {{ createTime }}
          </span>
          <span class="bar-item">tokens: {{ tokens }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: 'user-icon.png'
  },
  createTime: {
    type: String,
    default: ''
  },
  tokens: {
    type: Number,
    default: 0
  }
});

const showContent = computed(() => {
  return renderInputText(props.content ?? '');
});

function renderInputText(text) {
  const replaceRegex = /(\n\r|\r\n|\r|\n)/g;
  text = text || '';
  return text.replace(replaceRegex, '<br/>');
}
</script>

<style lang="scss" scoped>
.chat-line-prompt {
  justify-content: center;
  width: 100%;
  padding-bottom: 16px;
  padding-top: 16px;

  .chat-line-inner {
    display: flex;
    width: 100%;
    max-width: 900px;
    align-items: flex-start;
    justify-content: flex-end;

    .chat-icon {
      order: 3;

      img {
        width: 38px;
        height: 38px;
        border-radius: 12px;
        padding: 1px;
      }
    }

    .chat-content {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      order: 2;
      max-width: calc(100% - 80px);
      position: relative;
      padding: 2px 5px 0 0;

      .content {
        display: flex;
        flex-direction: column;
        position: relative;
        overflow-wrap: break-word;
        word-break: break-word;
        padding: 10px 12px;
        color: #374151;
        font-size: 14px;
        overflow: auto;
        background-color: rgb(214, 232, 255, 0.8);
        border-radius: 8px 2px 8px 8px;
        box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);

        p {
          line-height: 1.5;
        }

        p:last-child {
          margin-bottom: 0;
        }

        p:first-child {
          margin-top: 0;
        }
      }

      .bar {
        padding: 10px 0;
        font-size: 14px;

        .bar-item {
          background-color: rgba(242, 242, 242, 0.8);
          color: #888;
          padding: 4px 5px;
          margin-right: 10px;
          border-radius: 5px;
          &:last-child {
            margin-right: 0px;
          }

          .el-icon {
            position: relative;
            top: 2px;
          }
        }
      }
    }
  }
}
</style>
