<template>
  <el-image-viewer
    v-if="isShow"
    :url-list="showImgs"
    :z-index="options.zIndex"
    :initial-index="options.initialIndex"
    :infinite="options.infinite"
    :hide-on-click-modal="options.hideOnClickModal"
    @close="close"
  />
</template>

<script lang="ts" setup>
import { PropType } from 'vue';

const props = defineProps({
  urlList: {
    type: Array as PropType<string[]>,
    default: () => {
      return [];
    }
  },
  zIndex: {
    type: Number,
    default: 2800
  },
  initialIndex: {
    type: Number,
    default: 0
  },
  infinite: {
    type: Boolean,
    default: true
  },
  hideOnClickModal: {
    type: Boolean,
    default: false
  }
});

const isShow = ref(false);
const showImgs = ref(props.urlList);
const options = reactive({
  zIndex: props.zIndex,
  initialIndex: props.initialIndex,
  infinite: props.infinite,
  hideOnClickModal: props.hideOnClickModal
});

const open = (urlList: string | string[], opt) => {
  if (typeof urlList === 'string') {
    showImgs.value = [urlList];
  } else {
    showImgs.value = urlList;
  }
  Object.assign(options, { zIndex: props.zIndex, initialIndex: props.initialIndex, infinite: props.infinite, hideOnClickModal: props.hideOnClickModal }, opt);
  isShow.value = true;
};

const close = () => {
  isShow.value = false;
  showImgs.value = [];
};

defineExpose({ open, close });
</script>

<style lang="css" scoped></style>
