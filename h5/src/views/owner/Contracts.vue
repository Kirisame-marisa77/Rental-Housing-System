<template>
  <AppPage title="我的合同">
    <CardList :data="list" :loading="loading" empty-text="还没有合同">
      <template #item="{ row }">
        <InfoCard
          :title="houseText(row, { style: 'unit', fallback: 'idOrDash' })"
          :subtitle="row.contractNo"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 0">
              <el-button v-if="!row.firstBillPaid" link type="primary" disabled>待租客缴费</el-button>
              <el-button v-else link type="success" @click="handleSign(row)">确认签约</el-button>
            </template>
          </template>
        </InfoCard>
      </template>
    </CardList>

    <el-dialog v-model="detailVisible" title="合同详情" width="720px" top="5vh">
      <div v-loading="detailLoading">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="合同编号">{{ detail?.contractNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail?.status)">{{ statusLabel(detail?.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="房源" :span="2">{{ houseText(detail) }}</el-descriptions-item>
          <el-descriptions-item label="户型 / 面积">
            {{ detail?.layout || '-' }} / {{ detail?.squareArea ? detail.squareArea + ' ㎡' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="租客">
            {{ detail?.tenantName || '-' }}{{ detail?.tenantPhone ? ' ' + detail.tenantPhone : '' }}
          </el-descriptions-item>
          <el-descriptions-item label="租期" :span="2">
            {{ detail?.rentStartDate || '-' }} ~ {{ detail?.rentEndDate || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="月租金">{{ detail?.monthlyRent ?? '-' }} 元</el-descriptions-item>
          <el-descriptions-item label="押金">{{ detail?.depositAmount ?? '-' }} 元</el-descriptions-item>
          <el-descriptions-item label="付款方式">{{ detail?.paymentMethod || '-' }}</el-descriptions-item>
          <el-descriptions-item label="首期账单">
            <template v-if="detail?.firstBillNo">
              {{ detail.firstBillNo }}
              <el-tag :type="detail.firstBillPaid ? 'success' : 'warning'">
                {{ detail.firstBillPaid ? '已缴' : '待缴' }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="业主签署">
            {{ detail?.ownerSignTime ? detail.ownerSignTime.replace('T', ' ').slice(0, 19) : '未签署' }}
          </el-descriptions-item>
          <el-descriptions-item label="租客签署">
            {{ detail?.signTime ? detail.signTime.replace('T', ' ').slice(0, 19) : '未签署' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="section-title">合同正文</div>
        <div v-if="detail?.content" class="content" v-html="detail.content"></div>
        <el-empty v-else description="该合同暂无正文" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </AppPage>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOwnerContracts, getOwnerContract, signOwnerContract } from '../../api'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])

const statusMap = [
  { value: 0, label: '待签署' },
  { value: 1, label: '待缴费' },
  { value: 2, label: '生效中' },
  { value: 3, label: '即将到期' },
  { value: 4, label: '退租处理中' },
  { value: 5, label: '已到期' },
  { value: 6, label: '已退租' },
  { value: 7, label: '已取消' }
]
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 2 ? 'success' : s === 0 ? 'warning' : s === 7 ? 'danger' : 'info')

const tagsOf = (row) => [
  { text: statusLabel(row.status), type: statusType(row.status) },
  ...(row.status === 7
    ? []
    : [{ text: row.firstBillPaid ? '首期已缴' : '首期待缴', type: row.firstBillPaid ? 'success' : 'warning' }])
]

const fieldsOf = (row) => [
  { label: '月租金', value: `¥${row.monthlyRent ?? 0}`, type: 'amount' },
  { label: '付款方式', value: row.paymentMethod },
  { label: '租期开始', value: row.rentStartDate },
  { label: '租期结束', value: row.rentEndDate },
  { label: '租客', value: row.tenantName },
  { label: '联系电话', value: row.tenantPhone }
]

const getList = async () => {
  loading.value = true
  try {
    const data = await getOwnerContracts()
    list.value = data || []
  } finally {
    loading.value = false
  }
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getOwnerContract(row.id)
  } finally {
    detailLoading.value = false
  }
}

const handleSign = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认签署该合同吗？双方签署且首期账单已缴清后合同即生效，房源将被占用（先到先得）。',
      '确认签约',
      { type: 'warning' }
    )
    await signOwnerContract(row.id)
    ElMessage.success('签署成功')
    getList()
  } catch {}
}

onMounted(getList)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.section-title {
  margin: 14px 0 8px;
  font-weight: 600;
}
.content {
  line-height: 1.8;
  max-height: 50vh;
  overflow: auto;
}
</style>
