<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="模板名称" prop="templateName">
        <el-input
          v-model="queryParams.templateName"
          class="!w-240px"
          clearable
          placeholder="请输入模板名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模板类型" prop="templateType">
        <el-select v-model="queryParams.templateType" class="!w-240px" clearable placeholder="请选择模板类型">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-240px" clearable placeholder="请选择状态">
          <el-option label="启用" :value="0" />
          <el-option label="禁用" :value="1" />
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
        <el-button v-hasPermi="['rental:contract-template:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="模板名称" prop="templateName" min-width="200" show-overflow-tooltip />
      <el-table-column align="center" label="模板类型" width="130">
        <template #default="scope">{{ typeLabel(scope.row.templateType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="版本" prop="version" width="80" />
      <el-table-column align="center" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">
            {{ scope.row.status === 0 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="备注" prop="remark" min-width="150" show-overflow-tooltip />
      <el-table-column align="center" label="创建时间" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column align="center" label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openPreview(scope.row)">预览</el-button>
          <el-button v-hasPermi="['rental:contract-template:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:contract-template:delete']" link type="danger" @click="handleDelete(scope.row.id)">
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
  <ContractTemplateForm ref="formRef" @success="getList" />

  <!-- 预览弹窗：按原样展示模板，不做变量替换（这里没有具体合同可以代入） -->
  <Dialog v-model="previewVisible" :title="`模板预览 - ${previewData.templateName || ''}`" width="800px" top="5vh">
    <div class="preview-tip">
      正文中的 <code>${变量名}</code>（形如 <code>${tenantName}</code>）会在生成合同时替换为真实数据。
    </div>
    <div class="preview-body" v-html="previewData.content"></div>
    <template #footer>
      <el-button @click="previewVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as ContractTemplateApi from '@/api/rental/contractTemplate'
import ContractTemplateForm from './ContractTemplateForm.vue'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'RentalContractTemplate' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<ContractTemplateApi.ContractTemplateVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  templateName: '',
  templateType: undefined,
  status: undefined
})
const queryFormRef = ref()

const typeOptions = [
  { value: 0, label: '标准租赁合同' },
  { value: 1, label: '短期租赁合同' },
  { value: 2, label: '商业租赁合同' }
]
const typeLabel = (type?: number) => typeOptions.find((i) => i.value === type)?.label || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await ContractTemplateApi.getContractTemplatePage(queryParams)
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
    await ContractTemplateApi.deleteContractTemplate(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

// 预览：列表接口不带 content，需要单独拉一次详情
const previewVisible = ref(false)
const previewData = ref<ContractTemplateApi.ContractTemplateVO>({})
const openPreview = async (row: ContractTemplateApi.ContractTemplateVO) => {
  previewData.value = await ContractTemplateApi.getContractTemplate(row.id!)
  previewVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.preview-tip {
  margin-bottom: 12px;
  color: #909399;
  font-size: 13px;
}
.preview-body {
  max-height: 60vh;
  overflow: auto;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}
</style>
