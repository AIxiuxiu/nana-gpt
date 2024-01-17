<template>
  <div class="pagination">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      background
      :layout="layout"
      :page-sizes="pageSizes"
      :total="totalCount"
      :hide-on-single-page="true"
      @current-change="onCurrentChange"
      @size-change="onSizeChange"
    />
  </div>
</template>

<script setup lang="ts">
import { InjectTabkeKey } from '@/types/symbols';
import { inject, PropType } from 'vue';

const props = defineProps({
  // id用于区分多个时使用，和useAsyncTableApi的id参数对应
  id: {
    type: String,
    default: ''
  },
  pageSizes: {
    type: Array as PropType<Array<number>>,
    default: () => [10, 20, 50, 100]
  },
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  }
});

const { currentPage, pageSize, totalCount, onCurrentChange, onSizeChange } = inject(InjectTabkeKey(props.id));
</script>

<style lang="scss" scoped>
.pagination {
  display: flex;
  justify-content: end;
  padding-top: 20px;
  background-color: $white-color;
  text-align: center;
}
</style>
