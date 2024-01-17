<template>
  <div class="user">
    <div v-loading="loadingSubscription" class="subscription">
      <div>
        账号：
        <span>{{ subscription?.account_name }}</span>
      </div>
      <div>
        余额：
        <span>{{ subscription?.hard_limit_usd || 0 }}</span>
        美元
      </div>
      <div>
        到期时间：
        <span v-if="subscription">{{ dayjs.unix(subscription?.access_until).format('YYYY-MM-DD HH:mm') }}</span>
      </div>
    </div>
    <div class="billingUsage">
      <div class="hedaer">
        <div>账单：</div>
        <el-date-picker
          v-model="dateRange"
          style="max-width: 360px"
          type="daterange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="shortcuts"
          @change="getBillingUsage()"
        />
        <div class="info">
          使用 <i> {{ total_usage }}</i> 美分
        </div>
      </div>
      <el-table v-loading="loadingBillingUsage" :data="daily_costs" style="width: 100%">
        <el-table-column prop="timestamp" label="时间" width="120">
          <template #default="scope">
            <span>{{ dayjs.unix(scope.row.timestamp).format('YYYY-MM-DD') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="line_items" label="详情" width="780">
          <template #default="scope">
            <div class="d-flex ai-center jc-between">
              <span v-for="model in scope.row.line_items.filter((v) => v.name != 'Image models' && v.name != 'Audio models')" :key="model.name">
                <el-tag type="info">
                  {{ model.name }}
                </el-tag>
                <el-tag>
                  {{ model.cost }}
                </el-tag>
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 会员中心
 */
import ChatApi from '@/apis/chatApi';
import dayjs from 'dayjs';
import { useDateRange } from '../../hooks/useDateRange';

/**
 * dateRange 相关重复代码
 * rerurn { shortcuts:快捷选项 }
 */

const { shortcuts } = useDateRange();

const subscription = ref<any>();
const loadingSubscription = ref(true);
ChatApi.subscription()
  .then((res) => {
    if (res) {
      subscription.value = res;
    }
  })
  .finally(() => {
    loadingSubscription.value = false;
  });

const dateRange = ref([dayjs().subtract(30, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]);

const daily_costs = ref([]);
const total_usage = ref(0);

const loadingBillingUsage = ref(false);
function getBillingUsage() {
  let startDate;
  let endDate;
  if (dateRange.value[0]) {
    startDate = dateRange.value[0];
  }
  if (dateRange.value[1]) {
    endDate = dateRange.value[1];
  }
  loadingBillingUsage.value = true;
  ChatApi.billingUsage(startDate, endDate)
    .then((res) => {
      if (res) {
        daily_costs.value = res.daily_costs.sort(function (a, b) {
          return b.timestamp - a.timestamp;
        });
        total_usage.value = res.total_usage;
      }
    })
    .finally(() => {
      loadingBillingUsage.value = false;
    });
}

getBillingUsage();
</script>

<style lang="scss" scoped>
.user {
  margin: 0 auto;
  padding: 30px 0;
  position: relative;
  width: $content-min-width;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.subscription {
  background-color: rgba(254, 254, 254, 0.8);
  width: 932px;
  height: 110px;
  display: flex;
  flex-direction: column;
  font-size: 16px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
  div {
    line-height: 1.6;
    color: #666;
    span {
      color: #333;
    }
  }
}
.billingUsage {
  min-height: 200px;
  background-color: #fefefe;
  display: flex;
  flex-direction: column;
  font-size: 16px;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
}
.hedaer {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  .info {
    margin-left: 16px;
    font-size: 16px;
    i {
      color: $primary-color;
    }
  }
}
</style>
