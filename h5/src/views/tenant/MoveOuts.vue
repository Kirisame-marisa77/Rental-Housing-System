<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">退租申请</span>
        <el-button type="primary" @click="openApply">申请退租</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="applyNo" label="申请编号" width="180" show-overflow-tooltip />
        <el-table-column label="房源" min-width="180" show-overflow-tooltip>
          <template #default="s">{{ houseText(s.row) }}</template>
        </el-table-column>
        <el-table-column label="退租类型" width="100">
          <template #default="s">{{ s.row.moveOutType === 1 ? '提前退租' : '到期退租' }}</template>
        </el-table-column>
        <el-table-column prop="expectedMoveOutDate" label="预计退租日" width="120" />
        <el-table-column prop="moveOutReason" label="原因" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="s">
            <el-tag :type="s.row.status === 1 ? 'success' : 'warning'">
              {{ s.row.status === 1 ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结算结果" width="140">
          <template #default="s">
            <span v-if="s.row.status !== 1">-</span>
            <span v-else :class="s.row.refundOrPay >= 0 ? 'refund' : 'pay'">
              {{ s.row.refundOrPay >= 0 ? '退还' : '补缴' }} ¥{{ Math.abs(s.row.refundOrPay ?? 0) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="s">
            <el-button link type="primary" @click="openDetail(s.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 申请退租 -->
    <el-dialog v-model="applyVisible" title="申请退租" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="合同">
          <el-select v-model="form.contractId" class="w-full" placeholder="请选择要退租的合同">
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
          <el-date-picker v-model="form.expectedMoveOutDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
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
        <el-descriptions-item label="状态">
          {{ detail.status === 1 ? '已处理' : '待处理' }}
        </el-descriptions-item>
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
          <el-descriptions-item label="管理员备注">{{ detail.remark || '-' }}</el-descriptions-item>
        </template>
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

const loading = ref(false)
const list = ref([])
const contracts = ref([])

const inspectionMap = [
  { value: 0, label: '通过' },
  { value: 1, label: '轻微损坏' },
  { value: 2, label: '严重损坏' }
]
const inspectionLabel = (v) => (v == null ? '-' : inspectionMap.find((i) => i.value === v)?.label || '-')

const houseText = (row) => {
  if (!row) return '-'
  const addr = [row.communityName, row.buildingNo && `${row.buildingNo}栋`, row.roomNo && `${row.roomNo}室`]
    .filter(Boolean)
    .join(' ')
  return addr || (row.houseId ? `房源 ${row.houseId}` : '-')
}

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
  ElMessage.success('退租申请已提交，等待管理员验收结算')
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
