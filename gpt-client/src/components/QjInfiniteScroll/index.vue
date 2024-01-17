<template>
  <div @scroll="handleScroll">
    <div ref="scrollRef">
      <slot></slot>
    </div>

    <div v-if="isLoading" style="text-align: center">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { InjectTabkeKey } from '@/types/symbols';
import { inject, ref } from 'vue';
import { debounce } from '../../utils';

const props = defineProps({
  // id用于区分多个时使用，和useAsyncTableApi的id参数对应
  id: {
    type: String,
    default: ''
  }
});

const { isLoading, currentPage, pageSize, totalCount, onCurrentChange } = inject(InjectTabkeKey(props.id));

const scrollRef = ref();

const getScrollEventTarget = (currentNode) => {
  while (currentNode && currentNode.tagName !== 'HTML' && currentNode.tagName !== 'BODY' && currentNode.nodeType === 1) {
    let overflowY = document.defaultView.getComputedStyle(currentNode).overflowY;
    if (overflowY === 'scroll' || overflowY === 'auto') {
      return currentNode;
    }
    currentNode = currentNode.parentNode;
  }
  return window;
};

let scrollEventTarget;
onMounted(() => {
  scrollEventTarget = getScrollEventTarget(scrollRef.value);
});

const handleScroll = debounce(scrollChange);
function scrollChange() {
  console.error('scrollChange' + isLoading.value + currentPage.value + '   ' + totalCount.value);

  let element = scrollRef.value;
  if (!isLoading.value && element && scrollEventTarget && element.getBoundingClientRect().bottom < scrollEventTarget.getBoundingClientRect().bottom) {
    if (currentPage.value * pageSize.value < totalCount.value) {
      currentPage.value = currentPage.value + 1;
      onCurrentChange(currentPage.value, true);
    }
  }
}
</script>

<style lang="scss" scoped>
.spinner {
  display: inline-block; /* Setting display to inline-block prevents the custom event from being fired multiple times at once */
  margin: 5px auto;
  height: 2rem;
  width: 2rem;
  border-radius: 50%;
  border: 2px dashed black;
  position: relative;
  animation: spin 2s ease infinite;
}
.spinner::before {
  position: absolute;
  content: '';
  top: 0;
  left: -7px;
  width: 40%;
  height: 70%;
  background: white;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
