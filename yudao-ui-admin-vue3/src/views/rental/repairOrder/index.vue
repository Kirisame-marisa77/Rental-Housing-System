<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="工单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" class="!w-200px" clearable placeholder="请输入工单编号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="报修类型" prop="repairType">
        <el-select v-model="queryParams.repairType" class="!w-160px" clearable placeholder="请选择报修类型">
          <el-option v-for="item in repairTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="工单状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-160px" clearable placeholder="请选择状态">
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
        <el-button v-hasPermi="['rental:repair:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="工单编号" prop="orderNo" width="170" show-overflow-tooltip />
      <el-table-column align="center" label="租客 ID" prop="tenantUserId" width="90" />
      <el-table-column align="center" label="房源 ID" prop="houseId" width="90" />
      <el-table-column align="center" label="业主 ID" prop="ownerId" width="90" />
      <el-table-column align="center" label="报修类型" prop="repairType" width="100" />
      <el-table-column align="center" label="优先级" width="80">
        <template #default="scope">
          <el-tag :type="priorityTagType(scope.row.priority)">{{ priorityLabel(scope.row.priority) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="维修人员 ID" prop="repairerId" width="100" />
      <el-table-column align="center" label="创建时间" prop="createTime" :formatter="dateFormatter" width="170" />
      <el-table-column align="center" label="操作" width="320">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" v-hasPermi="['rental:repair:update']" link type="primary" @click="openAssign(scope.row.id)">
            分配
          </el-button>
          <el-button v-if="scope.row.status === 1" v-hasPermi="['rental:repair:update']" link type="success" @click="openComplete(scope.row.id)">
            维修完成
          </el-button>
          <el-button v-if="scope.row.status === 2" v-hasPermi="['rental:repair:update']" link type="success" @click="handleReviewPass(scope.row.id)">
            判定合格
          </el-button>
          <el-button v-if="scope.row.status === 2" v-hasPermi="['rental:repair:update']" link type="danger" @click="openReject(scope.row.id)">
            判定不合格
          </el-button>
          <el-button v-if="scope.row.status === 2" v-hasPermi="['rental:repair:update']" link type="primary" @click="handleConfirm(scope.row.id)">
            租客确认完成
          </el-button>
          <el-button v-if="scope.row.status === 4" v-hasPermi="['rental:repair:update']" link @click="openStatus(scope.row.id)">
            更新状态
          </el-button>
          <el-button v-hasPermi="['rental:repair:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:repair:delete']" link type="danger" @click="handleDelete(scope.row.id)">
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

  <!-- 表单弹窗 -->
  <RepairOrderForm ref="formRef" @success="getList" />

  <!-- 分配弹窗 -->
  <Dialog v-model="assignVisible" title="分配维修人员">
    <el-form label-width="110px">
      <el-form-item label="维修人员 ID">
        <el-input-number v-model="assignData.repairerId" :min="1" class="!w-full" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitAssign">确 定</el-button>
      <el-button @click="assignVisible = false">取 消</el-button>
    </template>
  </Dialog>

  <!-- 更新状态弹窗 -->
  <Dialog v-model="statusVisible" title="更新工单状态">
    <el-form label-width="110px">
      <el-form-item label="目标状态">
        <el-select v-model="statusData.status" class="!w-full">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitStatus">确 定</el-button>
      <el-button @click="statusVisible = false">取 消</el-button>
    </template>
  </Dialog>

  <!-- 维修完成弹窗 -->
  <Dialog v-model="completeVisible" title="维修完成">
    <el-form label-width="110px">
      <el-form-item label="维修说明">
        <el-input v-model="completeData.repairDescription" type="textarea" placeholder="处理过程和结果" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitComplete">确 定</el-button>
      <el-button @click="completeVisible = false">取 消</el-button>
    </template>
  </Dialog>

  <!-- 判定不合格弹窗 -->
  <Dialog v-model="rejectVisible" title="判定不合格（退回重做）">
    <el-form label-width="90px">
      <el-form-item label="不合格原因">
        <el-input v-model="rejectData.reason" type="textarea" placeholder="请输入不合格原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitReject">确 定</el-button>
      <el-button @click="rejectVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as RepairOrderApi from '@/api/rental/repairOrder'
import RepairOrderForm from './RepairOrderForm.vue'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'RentalRepairOrder' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  orderNo: '',
  repairType: undefined,
  status: undefined
})
const queryFormRef = ref()

const repairTypeOptions = ['水电维修', '家电维修', '管道疏通', '门窗维修', '墙面地面', '其他']

const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '待验收' },
  { value: 3, label: '已处理' },
  { value: 4, label: '已驳回' }
]
const statusLabel = (status?: number) => statusOptions.find((i) => i.value === status)?.label || '-'
const statusTagType = (status?: number) => {
  if (status === 3) return 'success'
  if (status === 4) return 'danger'
  if (status === 2) return 'warning'
  if (status === 0) return 'info'
  return 'primary'
}

const priorityOptions = [
  { value: 0, label: '普通' },
  { value: 1, label: '紧急' },
  { value: 2, label: '特急' }
]
const priorityLabel = (p?: number) => priorityOptions.find((i) => i.value === p)?.label || '-'
const priorityTagType = (p?: number) => {
  if (p === 2) return 'danger'
  if (p === 1) return 'warning'
  return 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await RepairOrderApi.getRepairOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
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
    await RepairOrderApi.deleteRepairOrder(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

// 分配
const assignVisible = ref(false)
const assignData = reactive<{ id?: number; repairerId?: number }>({})
const openAssign = (id: number) => {
  assignData.id = id
  assignData.repairerId = undefined
  assignVisible.value = true
}
const submitAssign = async () => {
  if (!assignData.repairerId) {
    message.warning('请输入维修人员 ID')
    return
  }
  await RepairOrderApi.assignRepairOrder(assignData.id!, assignData.repairerId)
  message.success('分配成功')
  assignVisible.value = false
  await getList()
}

// 更新状态
const statusVisible = ref(false)
const statusData = reactive<{ id?: number; status?: number }>({})
const openStatus = (id: number) => {
  statusData.id = id
  statusData.status = undefined
  statusVisible.value = true
}
const submitStatus = async () => {
  if (statusData.status === undefined) {
    message.warning('请选择目标状态')
    return
  }
  await RepairOrderApi.updateRepairOrderStatus(statusData.id!, statusData.status)
  message.success('更新成功')
  statusVisible.value = false
  await getList()
}

// 维修完成
const completeVisible = ref(false)
const completeData = reactive<{ id?: number; repairDescription: string }>({ repairDescription: '' })
const openComplete = (id: number) => {
  completeData.id = id
  completeData.repairDescription = ''
  completeVisible.value = true
}
const submitComplete = async () => {
  await RepairOrderApi.completeRepairOrder(completeData.id!, completeData.repairDescription)
  message.success('维修完成')
  completeVisible.value = false
  await getList()
}

// 判定与确认
const handleReviewPass = async (id: number) => {
  try {
    await message.confirm('确认该维修工单合格吗？')
    await RepairOrderApi.reviewRepairOrder(id, true)
    message.success('判定合格')
    await getList()
  } catch {}
}
const rejectVisible = ref(false)
const rejectData = reactive<{ id?: number; reason: string }>({ reason: '' })
const openReject = (id: number) => {
  rejectData.id = id
  rejectData.reason = ''
  rejectVisible.value = true
}
const submitReject = async () => {
  if (!rejectData.reason) {
    message.warning('请填写不合格原因')
    return
  }
  await RepairOrderApi.reviewRepairOrder(rejectData.id!, false, rejectData.reason)
  message.success('已退回重做')
  rejectVisible.value = false
  await getList()
}
const handleConfirm = async (id: number) => {
  try {
    await message.confirm('确认租客已确认维修完成吗？')
    await RepairOrderApi.confirmRepairOrder(id)
    message.success('已处理')
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
