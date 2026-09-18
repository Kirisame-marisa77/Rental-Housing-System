<template>
  <div>
    <AppPage title="退租处理">
      <template #actions>
        <el-button :icon="Refresh" circle @click="getList" />
      </template>

      <CardList :data="list" :loading="loading" empty-text="暂无退租申请">
        <template #item="{ row }">
          <InfoCard
            :title="houseText(row, { style: 'unit', fallback: 'idOrDash' })"
            :subtitle="row.applyNo"
            :tags="tagsOf(row)"
            :fields="fieldsOf(row)"
          >
            <template #actions>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <template v-if="row.status === 0">
                <el-button link type="success" @click="openConfirm(row)">处理</el-button>
                <el-button link type="danger" @click="openReject(row)">驳回</el-button>
              </template>
            </template>
          </InfoCard>
        </template>
      </CardList>
    </AppPage>

    <!-- 处理退租：房屋验收 + 费用结算 -->
    <el-dialog v-model="confirmVisible" title="处理退租（房屋验收 + 费用结算）" width="620px" top="5vh">
      <el-alert type="info" :closable="false" class="mb">
        合同 {{ current?.contractNo || current?.contractId }} ·
        月租 ¥{{ current?.monthlyRent ?? '-' }} · 押金 ¥{{ current?.depositAmount ?? '-' }} ·
        {{ current?.moveOutType === 1 ? '提前退租（押金不退，违约金）' : '到期退租（押金扣欠费后退还）' }}
      </el-alert>
      <el-form :model="cf" label-width="130px">
        <el-form-item label="房屋验收结果">
          <el-select v-model="cf.inspectionResult" class="u-w-full">
            <el-option :value="0" label="验收通过" />
            <el-option :value="1" label="轻微损坏" />
            <el-option :value="2" label="严重损坏" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修费用(元)">
          <el-input-number v-model="cf.repairFee" :min="0" :precision="2" class="u-w-full" />
        </el-form-item>
        <el-form-item label="维修明细">
          <el-input v-model="cf.repairFeeDesc" placeholder="如：墙面修补 200 元" />
        </el-form-item>
        <el-form-item label="物业费欠费(元)">
          <el-input-number v-model="cf.propertyFeeArrears" :min="0" :precision="2" class="u-w-full" />
        </el-form-item>
        <el-form-item label="水电费欠费(元)">
          <el-input-number v-model="cf.utilityFeeArrears" :min="0" :precision="2" class="u-w-full" />
        </el-form-item>
        <el-form-item v-if="cf.moveOutType === 1" label="剩余租金(元)">
          <el-input-number v-model="cf.remainingRent" :min="0" :precision="2" class="u-w-full" />
          <div class="tip">提前退租时已预缴但未使用的租金，抵扣欠费后多退少补</div>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="cf.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <!-- 预演算：与后端结算规则逐字一致，避免填完提交才发现方向反了 -->
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="押金处理">{{ depositHandle }}</el-descriptions-item>
        <el-descriptions-item label="欠费抵扣合计">¥{{ deductionAmount }}</el-descriptions-item>
        <el-descriptions-item label="结算结果">
          <span :class="refundOrPay >= 0 ? 'refund' : 'pay'">
            {{ refundOrPay >= 0 ? '应退还租客' : '租客应补缴' }} ¥{{ Math.abs(refundOrPay) }}
          </span>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button type="primary" :loading="submitting" @click="submitConfirm">确认处理并生成结算单</el-button>
        <el-button @click="confirmVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 驳回 -->
    <el-dialog v-model="rejectVisible" title="驳回退租申请" width="460px">
      <el-form label-width="90px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="必填，会展示给租客" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" :loading="submitting" @click="submitReject">确认驳回</el-button>
        <el-button @click="rejectVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="退租详情" width="600px">
      <el-descriptions v-if="detail" :column="1" border size="small">
        <el-descriptions-item label="申请编号">{{ detail.applyNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房源">{{ houseText(detail) }}</el-descriptions-item>
        <el-descriptions-item label="租客">
          {{ detail.tenantName || '-' }} {{ detail.tenantPhone || '' }}
        </el-descriptions-item>
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
        <el-descriptions-item label="处理备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { confirmOwnerMoveOut, getOwnerContract, getOwnerMoveOuts, rejectOwnerMoveOut } from '../../api'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])

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
  { label: '租客', value: row.tenantName },
  { label: '联系电话', value: row.tenantPhone },
  { label: '退租类型', value: row.moveOutType === 1 ? '提前退租' : '到期退租' },
  { label: '预计退租日', value: row.expectedMoveOutDate },
  { label: '退租原因', value: row.moveOutReason, span: 2, clamp: 2 }
]

const getList = async () => {
  loading.value = true
  try {
    const data = await getOwnerMoveOuts({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

// ===== 处理退租 =====
const confirmVisible = ref(false)
const current = ref(null)
const cf = reactive({
  id: undefined,
  moveOutType: 0,
  inspectionResult: 0,
  repairFee: 0,
  repairFeeDesc: '',
  propertyFeeArrears: 0,
  utilityFeeArrears: 0,
  remainingRent: 0,
  remark: ''
})

const round = (n) => Math.round((Number(n) || 0) * 100) / 100

// 下面三个 computed 逐字复刻后端 doConfirm 的结算规则，改一处必须改两处
const depositHandle = computed(() => (cf.moveOutType === 1 ? '不退（违约金）' : '扣除欠费后退还'))
const deductionAmount = computed(() =>
  round(round(cf.propertyFeeArrears) + round(cf.utilityFeeArrears) + round(cf.repairFee))
)
const refundOrPay = computed(() =>
  cf.moveOutType === 1
    ? round(cf.remainingRent - deductionAmount.value)
    : round((current.value?.depositAmount ?? 0) - deductionAmount.value)
)

const openConfirm = async (row) => {
  current.value = row
  Object.assign(cf, {
    id: row.id,
    moveOutType: row.moveOutType,
    inspectionResult: 0,
    repairFee: 0,
    repairFeeDesc: '',
    propertyFeeArrears: 0,
    utilityFeeArrears: 0,
    remainingRent: 0,
    remark: ''
  })
  // 押金金额不在退租申请上，得从合同取；失败不阻塞（弹窗里用 ?. 兜底显示 '-'）
  try {
    current.value = { ...row, ...(await getOwnerContract(row.contractId)) }
  } catch {
    /* 忽略：填数不受影响 */
  }
  confirmVisible.value = true
}

const submitConfirm = async () => {
  submitting.value = true
  try {
    await confirmOwnerMoveOut({
      id: cf.id,
      inspectionResult: cf.inspectionResult,
      repairFee: cf.repairFee,
      repairFeeDesc: cf.repairFeeDesc,
      propertyFeeArrears: cf.propertyFeeArrears,
      utilityFeeArrears: cf.utilityFeeArrears,
      remainingRent: cf.remainingRent,
      remark: cf.remark
    })
    ElMessage.success('退租已处理，结算单已生成，房源已下架')
    confirmVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

// ===== 驳回 =====
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectId = ref(undefined)

const openReject = (row) => {
  rejectId.value = row.id
  rejectReason.value = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    await rejectOwnerMoveOut(rejectId.value, rejectReason.value.trim())
    ElMessage.success('已驳回该退租申请')
    rejectVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

// ===== 详情 =====
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
.sub {
  color: #909399;
  font-size: 12px;
}
.w-full {
  width: 100%;
}
.mb {
  margin-bottom: 12px;
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
