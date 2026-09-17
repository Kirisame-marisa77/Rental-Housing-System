<template>
  <el-card>
    <div class="toolbar">
      <span class="title">我的合同</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="contractNo" label="合同编号" width="180" show-overflow-tooltip />
      <el-table-column label="房源" min-width="180" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column prop="ownerName" label="房东" width="90" />
      <el-table-column prop="ownerPhone" label="房东电话" width="120" />
      <el-table-column prop="rentStartDate" label="租期开始" width="110" />
      <el-table-column prop="rentEndDate" label="租期结束" width="110" />
      <el-table-column prop="monthlyRent" label="月租金" width="90" />
      <el-table-column prop="paymentMethod" label="付款方式" width="100" />
      <el-table-column label="首期账单" width="100">
        <template #default="s">
          <el-tag v-if="s.row.status === 7" type="info">-</el-tag>
          <el-tag v-else :type="s.row.firstBillPaid ? 'success' : 'warning'">
            {{ s.row.firstBillPaid ? '已缴' : '待缴' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="statusType(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="s">
          <el-button link type="primary" @click="openDetail(s.row)">详情</el-button>
          <template v-if="s.row.status === 0">
            <el-button v-if="!s.row.firstBillPaid" link type="primary" @click="router.push('/tenant/bills')">去缴费</el-button>
            <el-button v-else link type="success" @click="handleSign(s.row)">确认签约</el-button>
          </template>
          <!-- 退租只对生效中/即将到期的合同开放，后端也会再校验一次 -->
          <el-button
            v-else-if="s.row.status === 2 || s.row.status === 3"
            link
            type="warning"
            @click="router.push('/tenant/move-outs')"
          >
            申请退租
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="合同详情" width="720px" top="5vh">
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="合同编号">{{ detail?.contractNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail?.status)">{{ statusLabel(detail?.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="房源" :span="2">{{ houseText(detail) }}</el-descriptions-item>
          <el-descriptions-item label="户型 / 面积">
            {{ detail?.layout || '-' }} / {{ detail?.squareArea ? detail.squareArea + ' ㎡' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="房东">
            {{ detail?.ownerName || '-' }}{{ detail?.ownerPhone ? ' ' + detail.ownerPhone : '' }}
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
          <el-descriptions-item label="租期进度" :span="2">
            <el-progress :percentage="progressPercent" :status="progressStatus" />
            <span class="progress-text">{{ progressText }}</span>
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
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTenantContracts, getTenantContract, signTenantContract } from '../../api'

const router = useRouter()
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

const houseText = (row) => {
  if (!row) return '-'
  const addr = [row.communityName, row.buildingNo && `${row.buildingNo}栋`, row.roomNo && `${row.roomNo}室`]
    .filter(Boolean)
    .join(' ')
  return addr || (row.houseId ? `房源 ${row.houseId}` : '-')
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantContracts({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
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
    detail.value = await getTenantContract(row.id)
  } finally {
    detailLoading.value = false
  }
}

// 租期进度：只对生效中/即将到期的合同有意义，其余状态直接显示 0/100 会误导
const progressPercent = computed(() => {
  const d = detail.value
  if (!d?.rentStartDate || !d?.rentEndDate) return 0
  const start = new Date(d.rentStartDate).getTime()
  const end = new Date(d.rentEndDate).getTime()
  const now = Date.now()
  if (now <= start) return 0
  if (now >= end) return 100
  return Math.round(((now - start) / (end - start)) * 100)
})
const progressStatus = computed(() => (progressPercent.value >= 90 ? 'exception' : 'success'))
const progressText = computed(() => {
  const d = detail.value
  if (!d?.rentStartDate || !d?.rentEndDate) return '租期信息不完整'
  const end = new Date(d.rentEndDate).getTime()
  const days = Math.ceil((end - Date.now()) / 86400000)
  if (days < 0) return `已到期 ${-days} 天`
  if (days === 0) return '今天到期'
  return `距到期还有 ${days} 天`
})

const handleSign = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认签署该合同吗？双方签署且首期账单已缴清后合同即生效，房源先到先得。',
      '确认签约',
      { type: 'warning' }
    )
    await signTenantContract(row.id)
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
.progress-text {
  color: #909399;
  font-size: 12px;
}
</style>
