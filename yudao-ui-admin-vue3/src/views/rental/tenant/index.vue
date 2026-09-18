<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="租客姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          placeholder="请输入租客姓名"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          class="!w-240px"
          clearable
          placeholder="请输入手机号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="认证状态" prop="authStatus">
        <el-select v-model="queryParams.authStatus" class="!w-240px" clearable placeholder="请选择认证状态">
          <el-option v-for="item in authStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-button v-hasPermi="['rental:tenant:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="ID" prop="id" width="80" />
      <el-table-column align="center" label="租客姓名" prop="name" show-overflow-tooltip />
      <el-table-column align="center" label="手机号" prop="phone" width="130" />
      <el-table-column align="center" label="性别" width="80">
        <template #default="scope">{{ genderLabel(scope.row.gender) }}</template>
      </el-table-column>
      <el-table-column align="center" label="身份证号" prop="idCard" width="180" show-overflow-tooltip />
      <el-table-column align="center" label="工作单位" prop="workUnit" show-overflow-tooltip />
      <el-table-column align="center" label="认证状态" width="100">
        <template #default="scope">
          <el-tag :type="authStatusTagType(scope.row.authStatus)">{{ authStatusLabel(scope.row.authStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column align="center" label="操作" width="140">
        <template #default="scope">
          <el-button v-hasPermi="['rental:tenant:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:tenant:delete']" link type="danger" @click="handleDelete(scope.row.id)">
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
  <TenantForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import * as TenantApi from '@/api/rental/tenant'
import TenantForm from './TenantForm.vue'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'RentalTenant' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  phone: '',
  authStatus: undefined
})
const queryFormRef = ref()

const genderOptions = [
  { value: 0, label: '未知' },
  { value: 1, label: '男' },
  { value: 2, label: '女' }
]
const genderLabel = (gender?: number) => genderOptions.find((i) => i.value === gender)?.label || '-'

const authStatusOptions = [
  { value: 0, label: '未认证' },
  { value: 1, label: '认证中' },
  { value: 2, label: '已认证' },
  { value: 3, label: '认证失败' },
  { value: 4, label: '已锁定' }
]
const authStatusLabel = (status?: number) => authStatusOptions.find((i) => i.value === status)?.label || '-'
const authStatusTagType = (status?: number) => {
  if (status === 2) return 'success'
  if (status === 1) return 'warning'
  if (status === 3) return 'danger'
  return 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await TenantApi.getTenantInfoPage(queryParams)
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
    await TenantApi.deleteTenantInfo(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
