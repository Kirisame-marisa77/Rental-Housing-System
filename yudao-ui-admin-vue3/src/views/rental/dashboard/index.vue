<template>
  <!-- 综合指标卡片 -->
  <el-row :gutter="16" class="mb-16px">
    <el-col v-for="card in statCards" :key="card.label" :xs="12" :sm="12" :md="6" :lg="6" class="mb-16px">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
      </el-card>
    </el-col>
  </el-row>

  <el-row :gutter="16">
    <!-- 片区房源分布 -->
    <el-col :xs="24" :md="12">
      <el-card shadow="never" class="mb-16px">
        <template #header>片区房源分布</template>
        <div v-if="distribution.length === 0" class="empty-tip">暂无数据</div>
        <div v-for="item in distribution" :key="item.area" class="dist-row">
          <span class="dist-area">{{ item.area }}</span>
          <el-progress :percentage="calcPercent(item.count)" :stroke-width="14" class="dist-bar" />
          <span class="dist-count">{{ item.count }} 套</span>
        </div>
      </el-card>
    </el-col>

    <!-- 租约到期预警 -->
    <el-col :xs="24" :md="12">
      <el-card shadow="never" class="mb-16px">
        <template #header>未来 60 天到期租约</template>
        <el-table v-loading="expiringLoading" :data="expiringList" size="small">
          <el-table-column align="center" label="合同编号" prop="contractNo" width="170" show-overflow-tooltip />
          <el-table-column align="center" label="房源 ID" prop="houseId" width="90" />
          <el-table-column align="center" label="租客 ID" prop="tenantUserId" width="90" />
          <el-table-column align="center" label="到期日期" prop="rentEndDate" :formatter="dateFormatter2" />
        </el-table>
        <div v-if="expiringList.length === 0" class="empty-tip">暂无即将到期的租约</div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script lang="ts" setup>
import * as DashboardApi from '@/api/rental/dashboard'
import * as ContractApi from '@/api/rental/contract'
import { dateFormatter2 } from '@/utils/formatTime'

defineOptions({ name: 'RentalDashboard' })

const overview = ref<DashboardApi.DashboardOverviewVO>({})
const distribution = ref<DashboardApi.DashboardDistributionVO[]>([])
const expiringList = ref([])
const expiringLoading = ref(false)

const statCards = computed(() => [
  { label: '总房源数', value: overview.value.houseCount ?? 0, color: '#409EFF' },
  { label: '已出租', value: overview.value.rentedCount ?? 0, color: '#67C23A' },
  { label: '上架中', value: overview.value.onSaleCount ?? 0, color: '#E6A23C' },
  { label: '待房东审批申请', value: overview.value.pendingApplyCount ?? 0, color: '#F56C6C' },
  { label: '待处理工单', value: overview.value.pendingRepairCount ?? 0, color: '#909399' },
  { label: '待缴费账单', value: overview.value.pendingBillCount ?? 0, color: '#F56C6C' },
  { label: '下架房源', value: overview.value.offSaleCount ?? 0, color: '#909399' },
  { label: '即将到期', value: overview.value.expiringContractCount ?? 0, color: '#E6A23C' }
])

const calcPercent = (count?: number) => {
  if (!count) return 0
  const max = Math.max(...distribution.value.map((i) => i.count ?? 0), 1)
  return Math.round((count / max) * 100)
}

const loadData = async () => {
  overview.value = await DashboardApi.getOverview()
  distribution.value = await DashboardApi.getDistribution()
  expiringLoading.value = true
  try {
    expiringList.value = await ContractApi.getExpiringContractList(60)
  } finally {
    expiringLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.stat-card {
  text-align: center;
  .stat-label {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }
  .stat-value {
    margin-top: 8px;
    font-size: 28px;
    font-weight: 600;
  }
}

.dist-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  .dist-area {
    width: 90px;
    font-size: 13px;
    flex-shrink: 0;
  }
  .dist-bar {
    flex: 1;
    margin: 0 12px;
  }
  .dist-count {
    width: 50px;
    font-size: 13px;
    text-align: right;
    flex-shrink: 0;
  }
}

.empty-tip {
  padding: 20px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
}
</style>
