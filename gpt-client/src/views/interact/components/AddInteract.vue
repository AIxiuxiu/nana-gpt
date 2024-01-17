<template>
  <div v-loading="loading">
    <el-form class="marginT20" :model="interactForm" :label-width="100">
      <el-form-item label="选择公司">
        <el-select v-model="interactForm.company" value-key="oid" filterable remote reserve-keyword placeholder="请选择公司" :remote-method="remoteMethod" :loading="orgLoading">
          <el-option v-for="item in orgOptions" :key="item.oid" :label="item.name" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="interactForm.dataRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="shortcuts"
          clearable
          style="max-width: 280px"
          @change="dateChange"
        />
      </el-form-item>
      <el-form-item label="最大数量">
        <el-input-number v-model="interactForm.maxNum" :min="1" :max="100" :step="2" />
      </el-form-item>
      <el-form-item label="核心新闻">
        <el-switch v-model="interactForm.isCore" inline-prompt active-text="是" inactive-text="否" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="生成问题提示词">
        <el-input v-model="interactForm.qprompt" :autosize="{ minRows: 2, maxRows: 4 }" type="textarea" placeholder="请输入提示词" />
      </el-form-item>
      <el-form-item label="优化问题">
        <el-switch v-model="interactForm.qaOptimize" inline-prompt active-text="是" inactive-text="否" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="优化问题提示词">
        <el-input v-model="interactForm.optimizePrompt" :autosize="{ minRows: 2, maxRows: 4 }" type="textarea" placeholder="请输入提示词" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" plain @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import InteractApi from '@/apis/interactApi';
import OrgApi from '@/apis/orgApi';
import { useDateRange } from '@/hooks/useDateRange';
import dayjs from 'dayjs';

/**
 * 添加互动问答
 */

const props = defineProps({
  qprompt: {
    type: String,
    default: ''
  },
  optimizePrompt: {
    type: String,
    default: ''
  }
});
const { shortcuts } = useDateRange();

const loading = ref(false);

const interactForm = reactive<any>({
  company: {
    name: '',
    cpde: '',
    oid: ''
  },
  dataRange: [dayjs().subtract(90, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
  startDate: dayjs().subtract(90, 'day').format('YYYY-MM-DD'),
  endDate: dayjs().format('YYYY-MM-DD'),
  qprompt: props.qprompt,
  optimizePrompt: props.optimizePrompt,
  maxNum: 10,
  isCore: 1,
  qaOptimize: 1
});

const orgOptions = ref([]);
const orgLoading = ref(false);
const remoteMethod = (query: string) => {
  if (query) {
    orgLoading.value = true;
    OrgApi.searchListed({ searchValue: query, num: 10 }).then((data) => {
      orgLoading.value = false;
      orgOptions.value = data;
    });
  } else {
    orgOptions.value = [];
  }
};

function dateChange(value) {
  console.error(value);
  if (value == null) {
    interactForm.startDate = '';
    interactForm.endDate = '';
  } else {
    interactForm.startDate = value[0];
    interactForm.endDate = value[1];
  }
}

const emits = defineEmits(['success']);

function save() {
  InteractApi.add({
    orgId: interactForm.company.oid,
    startDate: interactForm.startDate,
    endDate: interactForm.endDate,
    maxNum: interactForm.maxNum,
    qprompt: interactForm.qprompt,
    isCore: interactForm.isCore,
    qaOptimize: interactForm.qaOptimize,
    optimizePrompt: interactForm.optimizePrompt
  }).then((data) => {
    emits('success', data);
  });
}
</script>
