<template>
  <div class="chat-line-reply">
    <div class="chat-line-inner">
      <div class="chat-icon">
        <img :src="$img(icon)" alt="ChatGPT" />
      </div>
      <div class="chat-content">
        <div class="content markdown-body" v-html="showContent"></div>
        <div v-if="createTime !== ''" class="bar">
          <span class="bar-item">
            <el-icon><icon-ep-Clock /></el-icon> {{ createTime }}
          </span>
          <span class="bar-item">tokens: {{ tokens }}</span>
          <el-tooltip class="box-item" effect="light" content="复制回答" placement="top">
            <el-button @click="doCopy">
              <el-icon>
                <icon-ep-DocumentCopy />
              </el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip class="box-item" effect="light" content="提示词" placement="top">
            <el-button v-if="prompt" @click="showPrompt = true">
              <el-icon>
                <icon-ep-ChatLineSquare />
              </el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>
    <el-dialog v-model="showPrompt" title="提示词" width="860px">
      <p style="white-space: pre-line">{{ prompt }}</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import mdKatex from '@traptitech/markdown-it-katex';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark-reasonable.css';
import MarkdownIt from 'markdown-it';
import mila from 'markdown-it-link-attributes';
import { computed } from 'vue';
import { copyText } from 'vue3-clipboard';

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  prompt: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: 'openai.svg'
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

const mdi = new MarkdownIt({
  linkify: true,
  highlight(code, language) {
    const validLang = !!(language && hljs.getLanguage(language));
    if (validLang) {
      const lang = language ?? '';
      return highlightBlock(hljs.highlight(code, { language: lang }).value, lang);
    }
    return highlightBlock(hljs.highlightAuto(code).value, '');
  }
});

mdi.use(mila, { attrs: { target: '_blank', rel: 'noopener' } });
// 数学公式
mdi.use(mdKatex, {
  blockClass: 'katexmath-block padding10',
  errorColor: ' #F53F3F'
});

// 代码高亮
function highlightBlock(str: string, lang?: string) {
  return `<pre class="code-block-wrapper">
            <div class="code-block-header">
              <span class="code-block-header__lang">${lang}</span>
            </div>
            <code class="hljs code-block-body ${lang}">${str}</code>
          </pre>`;
}

const showContent = computed(() => {
  let value = removeTrailingNewLines(props.content ?? '');
  return mdi.render(value);
});

const removeTrailingNewLines = (text: string) => {
  try {
    return text?.replace(/\n+$/g, '');
  } catch (error) {
    console.log(text);
    return text;
  }
};

const doCopy = () => {
  copyText(props.content, undefined, (error, event) => {
    if (error) {
      ElMessage.success('复制失败');
    } else {
      ElMessage.success('复制成功');
    }
  });
};

const showPrompt = ref(false);
</script>

<style lang="scss">
.chat-line-reply {
  justify-content: center;
  width: 100%;
  padding-bottom: 16px;
  padding-top: 16px;

  .chat-line-inner {
    display: flex;
    align-items: flex-start;
    justify-content: flex-start;
    width: 100%;
    max-width: 900px;

    .chat-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      height: 38px;
      width: 38px;
      border-radius: 12px;
      background-color: #f9f9f9;
      img {
        width: 30px;
        height: 30px;
      }
    }

    .chat-content {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      max-width: calc(100% - 80px);
      position: relative;
      padding: 2px 0 0 5px;

      .content {
        min-height: 20px;
        display: flex;
        flex-direction: column;
        position: relative;
        overflow-wrap: break-word;
        word-break: break-word;
        padding: 10px 12px;
        color: #374151;
        font-size: 14px;
        overflow: auto;
        background-color: rgba(246, 246, 246, 0.8);
        border-radius: 2px 8px 8px 8px;
        box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);

        p {
          line-height: 1.5;

          code {
            color: #f1f1f1;
            background-color: #202121;
            padding: 0 3px;
            border-radius: 5px;
          }
        }

        p:last-child {
          margin-bottom: 0;
        }

        p:first-child {
          margin-top: 0;
        }
      }

      .bar {
        display: flex;
        align-items: center;
        padding: 10px;
        font-size: 13px;

        .bar-item {
          background-color: rgba(242, 242, 242, 0.8);
          color: #888;
          padding: 4px 5px;
          margin-right: 10px;
          border-radius: 5px;

          .el-icon {
            position: relative;
            top: 2px;
          }
        }

        .el-button {
          height: 22px;
          min-height: 22px;
          padding: 5px 2px;
        }
      }
    }

    .tool-box {
      font-size: 16px;

      .el-button {
        height: 20px padding 5px 2px;
      }
    }
  }
}
</style>
