<template>
  <AppPage title="我的申请">
    <CardList :data="list" :loading="loading" empty-text="还没有提交过租房申请">
      <template #item="{ row }">
        <InfoCard
          :title="houseText(row, { style: 'slash', fallback: 'id' })"
          :subtitle="row.applyNo"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button
              v-if="row.contractId"
              link
              type="primary"
              @click="router.push('/tenant/contracts')"
            >
              查看合同
            </el-button>
          </template>
        </InfoCard>
      </template>
    </CardList>
  </AppPage>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getTenantApplies } from '../../api'
import { applyStatusLabel, applyStatusType } from '../../utils/dict'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const router = useRouter()
const loading = ref(false)
const list = ref([])

const tagsOf = (row) => [{ text: applyStatusLabel(row.status), type: applyStatusType(row.status) }]

const fieldsOf = (row) => [
  { label: '月租金', value: `¥${row.monthlyRent ?? 0}`, type: 'amount' },
  { label: '付款方式', value: row.paymentMethod },
  { label: '期望入住', value: row.moveInDate },
  { label: '租期', value: row.leaseTerm != null ? `${row.leaseTerm} 个月` : '-' },
  { label: '房东', value: row.ownerName },
  { label: '房东电话', value: row.ownerPhone },
  // 驳回原因与失效原因是两个字段，二选一显示
  {
    label: '驳回/失效原因',
    value: row.rejectReason || row.timeoutReason,
    span: 2,
    clamp: 2
  }
]

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantApplies({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

onMounted(getList)
</script>
