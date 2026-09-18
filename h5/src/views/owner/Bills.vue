<template>
  <AppPage title="我的账单" subtitle="只读">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint"
      title="名下房源产生的账单，仅供查看；缴费由租客或管理员操作"
    />

    <el-tabs v-model="tab" @tab-change="onTabChange">
      <el-tab-pane label="租金/物业费" name="rent" />
      <el-tab-pane label="水电费" name="utility" />
    </el-tabs>

    <CardList
      :data="currentList"
      :loading="loading"
      empty-text="暂无账单"
    >
      <template #item="{ row }">
        <InfoCard
          :title="`¥${row.totalAmount ?? 0}`"
          :subtitle="row.billNo"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        />
      </template>
    </CardList>
  </AppPage>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOwnerRentBills, getOwnerUtilityBills } from '../../api'
import { payStatusLabel, payStatusType } from '../../utils/dict'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const tab = ref('rent')
const loading = ref(false)
const rentList = ref([])
const utilityList = ref([])

const feeMap = [
  { value: 0, label: '水费' },
  { value: 1, label: '电费' },
  { value: 2, label: '水+电' }
]
const feeLabel = (s) => feeMap.find((i) => i.value === s)?.label || '-'

const currentList = computed(() => (tab.value === 'rent' ? rentList.value : utilityList.value))

const tagsOf = (row) => [
  {
    text: tab.value === 'rent' ? (row.billType === 0 ? '首期账单' : '周期账单') : feeLabel(row.feeType),
    type: 'primary'
  },
  { text: payStatusLabel(row.payStatus), type: payStatusType(row.payStatus) }
]

// 两个 tab 的字段不同，按当前 tab 分别给出
// 房源地址拼不出来时显示 '-'，不回退到裸的 houseId
const fieldsOf = (row) =>
  tab.value === 'rent'
    ? [
        { label: '房源', value: houseText(row, { style: 'unit', fallback: 'dash' }) },
        { label: '租金', value: `¥${row.rentAmount ?? 0}` },
        { label: '押金', value: `¥${row.depositAmount ?? 0}` },
        { label: '物业费', value: `¥${row.propertyFeeAmount ?? 0}` },
        { label: '缴费截止', value: row.dueDate }
      ]
    : [
        { label: '房源', value: houseText(row, { style: 'unit', fallback: 'dash' }) },
        { label: '水费', value: `¥${row.waterAmount ?? 0}` },
        { label: '电费', value: `¥${row.electricityAmount ?? 0}` },
        { label: '缴费截止', value: row.dueDate }
      ]

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
/* 只读提示条与 tab 之间的间距 */
.hint {
  margin-bottom: 10px;
}
</style>
