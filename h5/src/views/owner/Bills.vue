<template>
  <el-card>
    <div class="toolbar">
      <span class="title">我的账单</span>
      <span class="hint">名下房源产生的账单，仅供查看；缴费由租客或管理员操作</span>
    </div>

    <el-tabs v-model="tab" @tab-change="onTabChange">
      <el-tab-pane label="租金/物业费账单" name="rent" />
      <el-tab-pane label="水电费账单" name="utility" />
    </el-tabs>

    <!-- 租金 / 物业费 -->
    <el-table v-if="tab === 'rent'" :data="rentList" v-loading="loading" size="small">
      <el-table-column prop="billNo" label="账单编号" width="200" show-overflow-tooltip />
      <el-table-column label="房源" min-width="160" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="90">
        <template #default="s">{{ s.row.billType === 0 ? '首期账单' : '周期账单' }}</template>
      </el-table-column>
      <el-table-column prop="rentAmount" label="租金" width="90" />
      <el-table-column prop="depositAmount" label="押金" width="90" />
      <el-table-column prop="propertyFeeAmount" label="物业费" width="90" />
      <el-table-column label="应缴总额" width="100">
        <template #default="s"><span class="amount">¥{{ s.row.totalAmount }}</span></template>
      </el-table-column>
      <el-table-column prop="dueDate" label="缴费截止" width="110" />
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="payType(s.row.payStatus)">{{ payLabel(s.row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 水电费 -->
    <el-table v-else :data="utilityList" v-loading="loading" size="small">
      <el-table-column prop="billNo" label="账单编号" width="200" show-overflow-tooltip />
      <el-table-column label="房源" min-width="160" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column label="费用类型" width="90">
        <template #default="s">{{ feeLabel(s.row.feeType) }}</template>
      </el-table-column>
      <el-table-column prop="waterAmount" label="水费" width="90" />
      <el-table-column prop="electricityAmount" label="电费" width="90" />
      <el-table-column label="应缴总额" width="100">
        <template #default="s"><span class="amount">¥{{ s.row.totalAmount }}</span></template>
      </el-table-column>
      <el-table-column prop="dueDate" label="缴费截止" width="110" />
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="payType(s.row.payStatus)">{{ payLabel(s.row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !currentList.length" description="暂无账单" :image-size="60" />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOwnerRentBills, getOwnerUtilityBills } from '../../api'

const tab = ref('rent')
const loading = ref(false)
const rentList = ref([])
const utilityList = ref([])

const payMap = [
  { value: 0, label: '待缴费' },
  { value: 1, label: '已缴费' },
  { value: 2, label: '已逾期' }
]
const payLabel = (s) => payMap.find((i) => i.value === s)?.label || '-'
const payType = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'warning')

const feeMap = [
  { value: 0, label: '水费' },
  { value: 1, label: '电费' },
  { value: 2, label: '水+电' }
]
const feeLabel = (s) => feeMap.find((i) => i.value === s)?.label || '-'

// 后端不做地址兜底，拼不出来时显示 '-'，不回退到裸的 houseId
const houseText = (row) => {
  const addr = [row?.communityName, row?.buildingNo && `${row.buildingNo}栋`, row?.roomNo && `${row.roomNo}室`]
    .filter(Boolean)
    .join(' ')
  return addr || '-'
}

const currentList = computed(() => (tab.value === 'rent' ? rentList.value : utilityList.value))

// 只加载当前 tab，避免每次进页面都打两个接口
const load = async () => {
  loading.value = true
  try {
    // pageNo / pageSize 有 @NotNull，必须显式传，否则 400
    const params = { pageNo: 1, pageSize: 100 }
    if (tab.value === 'rent') {
      const data = await getOwnerRentBills(params)
      rentList.value = data.list || []
    } else {
      const data = await getOwnerUtilityBills(params)
      utilityList.value = data.list || []
    }
  } finally {
    loading.value = false
  }
}

const onTabChange = () => load()

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 4px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.hint {
  font-size: 12px;
  color: #999;
}
.amount {
  color: #f56c6c;
  font-weight: 600;
}
</style>
