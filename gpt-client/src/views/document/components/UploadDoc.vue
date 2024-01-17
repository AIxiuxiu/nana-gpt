<template>
  <div v-loading="loading">
    <el-upload
      style="width: 100%"
      action=""
      :http-request="uploadFile"
      :before-upload="beforeUpload"
      :on-exceed="handleExceed"
      :accept="accept"
      multiple
      drag
      :limit="limit"
      :show-file-list="false"
    >
      <el-icon size="38">
        <icon-ep-upload-filled></icon-ep-upload-filled>
      </el-icon>
      <div class="el-upload__text" style="font-size: 16px; margin: 10px">拖放文件至此处或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip" style="font-size: 14px">只支持pdf、word、md、text、json、txt，文件需小于20MB</div>
      </template>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
/**
 * 上传附件
 */
import DocApi from '@/apis/docApi';
import { AxiosRequestConfig } from 'axios';
import { UploadRequestOptions } from 'element-plus';
import { PropType } from 'vue';

const props = defineProps({
  kbId: {
    type: String,
    default: ''
  },
  // 支持的文件类型
  accept: {
    type: String,
    default: '.pdf,.doc,.docx,.xlsx,.csv,.md,.json,.txt,.html'
  },
  limit: {
    type: [Number, undefined],
    default: 1
  },
  // 是否限制大小
  limitFileSize: {
    type: Boolean,
    default: true
  },
  // 限制文件大小 mb
  fileSize: {
    type: Number,
    default: 20
  },
  fileSizeUnit: {
    type: String as PropType<'kb' | 'KB' | 'mb' | 'MB'>,
    default: 'mb'
  }
});

function beforeUpload(file) {
  if (props.limitFileSize) {
    let fileSize = file.size;
    if (props.fileSizeUnit.toLowerCase() == 'kb') {
      fileSize = file.size / 1024;
    } else {
      fileSize = file.size / 1024 / 1024;
    }
    const isLtSize = fileSize < props.fileSize;
    if (!isLtSize) {
      ElMessage.error(`文件大小不能超过 ${props.fileSize}${props.fileSizeUnit}!`);
      return false;
    }
  }
}

function handleExceed(files, fileList) {
  ElMessage.warning(`当前限制选择 ${props.limit} 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
}

const emits = defineEmits(['success']);

const loading = ref(false);
const uploadFile = (param: UploadRequestOptions) => {
  return new Promise((resolve, reject) => {
    if (!param.file) {
      reject('文件不能为空！');
      return;
    }

    const formData = new FormData();
    formData.append('kbId', props.kbId);
    formData.append('docName', param.file.name);
    formData.append('files', param.file);

    const config: AxiosRequestConfig = {
      onUploadProgress: function (progressEvent) {
        if (progressEvent.upload) {
          const percent = Number((progressEvent.progress * 100).toFixed(0));
          param.onProgress({ percent: percent } as any);
        }
      }
    };

    loading.value = true;
    DocApi.uploadDoc(formData, config)
      .then((data) => {
        if (data) {
          emits('success', data);
          resolve(data);
        } else {
          reject('上传文件失败');
        }
      })
      .catch((err) => {
        console.error('上传文件失败！' + err);
        reject('上传文件失败！');
      });
  }).finally(() => {
    loading.value = false;
  });
};
</script>
