<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="合同编号" prop="contractNo">
        <el-input
          v-model="queryParams.contractNo"
          class="!w-240px"
          clearable
          placeholder="请输入合同编号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="合同状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-240px" clearable placeholder="请选择合同状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button plain type="warning" @click="handleExpiring">
          <Icon class="mr-5px" icon="ep:alarm-clock" />
          即将到期
        </el-button>
        <el-button v-hasPermi="['rental:contract:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="合同编号" prop="contractNo" width="170" show-overflow-tooltip />
      <el-table-column align="center" label="房源" min-width="200" show-overflow-tooltip>
        <template #default="scope">{{ houseText(scope.row) }}</template>
      </el-table-column>
      <el-table-column align="center" label="租客" width="150" show-overflow-tooltip>
        <template #default="scope">{{ tenantText(scope.row) }}</template>
      </el-table-column>
      <el-table-column align="center" label="房东" width="130" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.ownerName || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="租期开始" prop="rentStartDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="租期结束" prop="rentEndDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="月租金(元)" prop="monthlyRent" width="110" />
      <el-table-column align="center" label="押金(元)" prop="depositAmount" width="100" />
      <el-table-column align="center" label="付款方式" prop="paymentMethod" width="110" />
      <el-table-column align="center" label="状态" width="110">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column align="center" label="操作" width="320">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 2 || scope.row.status === 3" v-hasPermi="['rental:contract:update']" link type="success" @click="openRenew(scope.row)">
            续签
          </el-button>
          <el-button v-if="scope.row.status !== 7 && scope.row.status !== 6" v-hasPermi="['rental:contract:update']" link @click="handleCancel(scope.row.id)">
            取消
          </el-button>
          <el-button v-hasPermi="['rental:contract:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:contract:delete']" link type="danger" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <ContractForm ref="formRef" @success="getList" />

  <!-- 续签弹窗 -->
  <Dialog v-model="renewVisible" title="租约续签">
    <el-form label-width="110px">
      <el-form-item label="合同编号">
        <el-input :model-value="renewData.contractNo" disabled />
      </el-form-item>
      <el-form-item label="新的到期日期">
        <el-date-picker
          v-model="renewData.newEndDate"
          type="date"
          value-format="YYYY-MM-DD"
          class="!w-full"
          placeholder="请选择新的到期日期"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitRenew">确 定</el-button>
      <el-button @click="renewVisible = false">取 消</el-button>
    </template>
  </Dialog>

  <!-- 合同详情弹窗 -->
  <Dialog v-model="detailVisible" title="合同详情" width="860px" top="5vh">
    <el-descriptions v-loading="detailLoading" :column="2" border>
      <el-descriptions-item label="合同编号">{{ detail.contractNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="合同状态">
        <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="房源">{{ houseText(detail) }}</el-descriptions-item>
      <el-descriptions-item label="户型 / 面积">
        {{ detail.layout || '-' }} / {{ detail.squareArea ? detail.squareArea + ' ㎡' : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="房东">
        {{ detail.ownerName || '-' }} {{ detail.ownerPhone ? '（' + detail.ownerPhone + '）' : '' }}
      </el-descriptions-item>
      <el-descriptions-item label="租客">
        {{ detail.tenantName || '-' }} {{ detail.tenantPhone ? '（' + detail.tenantPhone + '）' : '' }}
      </el-descriptions-item>
      <el-descriptions-item label="租期">
        {{ detail.rentStartDate || '-' }} ~ {{ detail.rentEndDate || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="付款方式">{{ detail.paymentMethod || '-' }}</el-descriptions-item>
      <el-descriptions-item label="月租金">{{ detail.monthlyRent ?? '-' }} 元</el-descriptions-item>
      <el-descriptions-item label="押金">{{ detail.depositAmount ?? '-' }} 元</el-descriptions-item>
      <el-descriptions-item label="物业费单价">
        {{ detail.propertyFeeUnit ?? '-' }} 元/㎡/月
      </el-descriptions-item>
      <el-descriptions-item label="首期账单">
        <span v-if="detail.firstBillNo">
          {{ detail.firstBillNo }}
          <el-tag class="ml-5px" :type="detail.firstBillPaid ? 'success' : 'warning'">
            {{ detail.firstBillPaid ? '已缴' : '待缴' }}
          </el-tag>
        </span>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="租客签署">
        {{ detail.signTime ? formatDateTime(detail.signTime) : '未签署' }}
      </el-descriptions-item>
      <el-descriptions-item label="业主签署">
        {{ detail.ownerSignTime ? formatDateTime(detail.ownerSignTime) : '未签署' }}
      </el-descriptions-item>
    </el-descriptions>

    <div class="content-title">合同正文</div>
    <!-- v-html 在本仓库是安全的：VueDOMPurifyHTML 已在 main.ts 全局注册 -->
    <div v-if="detail.content" class="contract-content" v-html="detail.content"></div>
    <el-empty v-else description="该合同没有正文（未套用模板，或模板正文为空）" :image-size="60" />

    <template #footer>
      <el-button @click="printDetail">打 印</el-button>
      <el-button @click="detailVisible = false">关 闭</el-button>
    </template>
  </Dialog>

  <!-- 即将到期弹窗 -->
  <Dialog v-model="expiringVisible" :title="`即将到期合同（未来 ${expiringDays} 天）`" width="900px">
    <el-table v-loading="expiringLoading" :data="expiringList" max-height="420">
      <el-table-column align="center" label="合同编号" prop="contractNo" width="170" show-overflow-tooltip />
      <el-table-column align="center" label="房源" min-width="180" show-overflow-tooltip>
        <template #default="scope">{{ houseText(scope.row) }}</template>
      </el-table-column>
      <el-table-column align="center" label="租客" width="120" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.tenantName || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="租期结束" prop="rentEndDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="月租金(元)" prop="monthlyRent" width="110" />
      <el-table-column align="center" label="状态" width="110">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="expiringVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as ContractApi from '@/api/rental/contract'
import ContractForm from './ContractForm.vue'
import { dateFormatter, dateFormatter2, formatDate } from '@/utils/formatTime'

defineOptions({ name: 'RentalContract' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  contractNo: '',
  status: undefined
})
const queryFormRef = ref()

const statusOptions = [
  { value: 0, label: '待签署' },
  { value: 1, label: '待缴费' },
  { value: 2, label: '生效中' },
  { value: 3, label: '即将到期' },
  { value: 4, label: '退租处理中' },
  { value: 5, label: '已到期' },
  { value: 6, label: '已退租' },
  { value: 7, label: '已取消' }
]
const statusLabel = (status?: number) => statusOptions.find((i) => i.value === status)?.label || '-'
const statusTagType = (status?: number) => {
  if (status === 2) return 'success'
  if (status === 0 || status === 1 || status === 3) return 'warning'
  if (status === 7) return 'danger'
  return 'info'
}

// 房源地址：优先用后端补全的小区/楼栋/房号，缺失时退回房源 ID（例如房源已被删）
const houseText = (row: any) => {
  const addr = [row.communityName, row.buildingNo && `${row.buildingNo}栋`, row.roomNo && `${row.roomNo}室`]
    .filter(Boolean)
    .join(' ')
  return addr || (row.houseId ? `房源 ${row.houseId}` : '-')
}

const tenantText = (row: any) => {
  if (!row.tenantName) return row.tenantUserId ? `租客 ${row.tenantUserId}` : '-'
  return row.tenantPhone ? `${row.tenantName}（${row.tenantPhone}）` : row.tenantName
}

const formatDateTime = (value: any) => formatDate(value, 'YYYY-MM-DD HH:mm:ss')

const getList = async () => {
  loading.value = true
  try {
    const data = await ContractApi.getContractPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 合同详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>({})
const openDetail = async (row: any) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = {}
  try {
    detail.value = await ContractApi.getContract(row.id)
  } finally {
    detailLoading.value = false
  }
}

const printDetail = () => {
  // 直接 window.print 会把整个后台界面一起打出来，所以另开一个只装正文的窗口
  const html = detail.value.content || '<p>该合同没有正文</p>'
  const win = window.open('', '_blank', 'width=900,height=700')
  if (!win) {
    message.warning('浏览器拦截了打印窗口，请允许本站弹出窗口')
    return
  }
  win.document.write(
    `<html><head><title>${detail.value.contractNo || '合同'}</title>` +
      '<meta charset="utf-8"></head><body>' +
      html +
      '</body></html>'
  )
  win.document.close()
  win.focus()
  win.print()
}

// 即将到期
const expiringVisible = ref(false)
const expiringLoading = ref(false)
const expiringDays = 60
const expiringList = ref<any[]>([])
const handleExpiring = async () => {
  expiringVisible.value = true
  expiringLoading.value = true
  try {
    expiringList.value = (await ContractApi.getExpiringContractList(expiringDays)) || []
  } finally {
    expiringLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ContractApi.deleteContract(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

// 续签
const renewVisible = ref(false)
const renewData = reactive({ id: undefined, contractNo: '', newEndDate: '' })
const openRenew = (row: any) => {
  renewData.id = row.id
  renewData.contractNo = row.contractNo
  renewData.newEndDate = ''
  renewVisible.value = true
}
const submitRenew = async () => {
  if (!renewData.newEndDate) {
    message.warning('请选择新的到期日期')
    return
  }
  await ContractApi.renewContract(renewData.id!, renewData.newEndDate)
  message.success('续签成功')
  renewVisible.value = false
  await getList()
}

const handleCancel = async (id: number) => {
  try {
    await message.confirm('确认取消该合同吗？')
    await ContractApi.cancelContract(id)
    message.success('已取消')
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.content-title {
  margin: 16px 0 8px;
  font-weight: 600;
}
.contract-content {
  max-height: 45vh;
  overflow: auto;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}
</style>
