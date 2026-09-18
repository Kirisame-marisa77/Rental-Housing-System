<template>
  <div>
    <AppPage title="退租申请">
      <template #actions>
        <el-button type="primary" @click="openApply">申请退租</el-button>
      </template>

      <CardList :data="list" :loading="loading" empty-text="还没有退租申请">
        <template #item="{ row }">
          <InfoCard
            :title="houseText(row, { style: 'unit', fallback: 'idOrDash' })"
            :subtitle="row.applyNo"
            :tags="tagsOf(row)"
            :fields="fieldsOf(row)"
          >
            <template #actions>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            </template>
          </InfoCard>
        </template>
      </CardList>
    </AppPage>

    <!-- 申请退租 -->
    <el-dialog v-model="applyVisible" title="申请退租" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="合同">
          <el-select v-model="form.contractId" class="u-w-full" placeholder="请选择要退租的合同">
            <el-option v-for="c in contracts" :key="c.id" :label="contractLabel(c)" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="退租类型">
          <el-radio-group v-model="form.moveOutType">
            <el-radio :value="0">到期退租</el-radio>
            <el-radio :value="1">提前退租</el-radio>
          </el-radio-group>
          <div class="tip">
            提前退租押金不退（违约金），只退未使用的剩余租金；到期退租押金扣除欠费后退还。
          </div>
        </el-form-item>
        <el-form-item label="预计退租日期">
          <el-date-picker v-model="form.expectedMoveOutDate" type="date" value-format="YYYY-MM-DD" class="u-w-full" />
        </el-form-item>
        <el-form-item label="退租原因">
          <el-input v-model="form.moveOutReason" type="textarea" :rows="3" placeholder="请说明退租原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
        <el-button @click="applyVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 退租详情 -->
    <el-dialog v-model="detailVisible" title="退租详情" width="600px">
      <el-descriptions v-if="detail" :column="1" border size="small">
        <el-descriptions-item label="申请编号">{{ detail.applyNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房源">{{ houseText(detail) }}</el-descriptions-item>
        <el-descriptions-item label="退租类型">
          {{ detail.moveOutType === 1 ? '提前退租' : '到期退租' }}
        </el-descriptions-item>
        <el-descriptions-item label="预计退租日期">{{ detail.expectedMoveOutDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退租原因">{{ detail.moveOutReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <template v-if="detail.status === 1">
          <el-descriptions-item label="房屋验收">{{ inspectionLabel(detail.inspectionResult) }}</el-descriptions-item>
          <el-descriptions-item label="押金处理">{{ detail.depositHandle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="剩余租金">¥{{ detail.remainingRent ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="欠费抵扣">¥{{ detail.deductionAmount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="维修费">¥{{ detail.repairFee ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="结算结果">
            <span :class="detail.refundOrPay >= 0 ? 'refund' : 'pay'">
              {{ detail.refundOrPay >= 0 ? '应退还' : '应补缴' }} ¥{{ Math.abs(detail.refundOrPay ?? 0) }}
            </span>
          </el-descriptions-item>
        </template>
        <!-- 驳回时只有备注有内容，单独放在模板外，否则驳回单会显示一大片 '-' -->
        <el-descriptions-item v-if="detail.status === 2" label="驳回原因">
          {{ detail.remark || '-' }}
        </el-descriptions-item>
        <el-descriptions-item v-else label="处理备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTenantMoveOuts, createTenantMoveOut, getTenantContracts } from '../../api'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])
const contracts = ref([])

const statusMap = [
  { value: 0, label: '待处理' },
  { value: 1, label: '已处理' },
  { value: 2, label: '已驳回' }
]
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'warning')

const inspectionMap = [
  { value: 0, label: '通过' },
  { value: 1, label: '轻微损坏' },
  { value: 2, label: '严重损坏' }
]
const inspectionLabel = (v) => (v == null ? '-' : inspectionMap.find((i) => i.value === v)?.label || '-')

const tagsOf = (row) => [{ text: statusLabel(row.status), type: statusType(row.status) }]

const fieldsOf = (row) => [
  { label: '退租类型', value: row.moveOutType === 1 ? '提前退租' : '到期退租' },
  { label: '预计退租日', value: row.expectedMoveOutDate },
  { label: '退租原因', value: row.moveOutReason, span: 2, clamp: 2 },
  // 只有已处理的申请才有结算结果
  {
    label: '结算结果',
    value: (r) =>
      r.refundOrPay == null
        ? '-'
        : `${r.refundOrPay >= 0 ? '应退还' : '应补缴'} ¥${Math.abs(r.refundOrPay)}`,
    span: 2,
    type: 'amount',
    hidden: (r) => r.status !== 1
  },
  { label: '房屋验收', value: inspectionLabel(row.inspectionResult), hidden: row.status !== 1 },
  { label: '押金处理', value: row.depositHandle, hidden: row.status !== 1 },
  { label: '欠费抵扣', value: `¥${row.deductionAmount ?? 0}`, hidden: row.status !== 1 }
]

const contractLabel = (c) =>
  [c.contractNo, c.communityName, c.roomNo && `${c.roomNo}室`].filter(Boolean).join(' · ') ||
  `合同 ${c.id}`

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantMoveOuts({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const applyVisible = ref(false)
const form = reactive({
  contractId: undefined,
  moveOutType: 0,
  expectedMoveOutDate: '',
  moveOutReason: ''
})

const openApply = async () => {
  Object.assign(form, {
    contractId: undefined,
    moveOutType: 0,
    expectedMoveOutDate: '',
    moveOutReason: ''
  })
  // 只有生效中(2)/即将到期(3)的合同能退租，后端也会再校验一次
  const data = await getTenantContracts({ pageNo: 1, pageSize: 100 })
  contracts.value = (data.list || []).filter((c) => c.status === 2 || c.status === 3)
  if (contracts.value.length === 0) {
    ElMessage.warning('当前没有可退租的合同（仅生效中或即将到期的合同可申请退租）')
    return
  }
  applyVisible.value = true
}

const submitApply = async () => {
  if (!form.contractId || !form.expectedMoveOutDate) {
    ElMessage.warning('请选择合同和预计退租日期')
    return
  }
  // 只提交这 4 个字段：租客编号与房源编号由后端从合同反查，传了也会被忽略
  await createTenantMoveOut({
    contractId: form.contractId,
    moveOutType: form.moveOutType,
    expectedMoveOutDate: form.expectedMoveOutDate,
    moveOutReason: form.moveOutReason
  })
  ElMessage.success('退租申请已提交，等待房东验收结算')
  applyVisible.value = false
  getList()
}

const detailVisible = ref(false)
const detail = ref(null)
const openDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

onMounted(getList)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.w-full {
  width: 100%;
}
.tip {
  width: 100%;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
.refund {
  color: #67c23a;
  font-weight: 600;
}
.pay {
  color: #f56c6c;
  font-weight: 600;
}
</style>
