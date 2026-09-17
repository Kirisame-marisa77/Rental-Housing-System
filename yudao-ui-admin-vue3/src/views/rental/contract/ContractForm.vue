<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="1000px" top="5vh">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="合同编号" prop="contractNo">
            <el-input v-model="formData.contractNo" placeholder="请输入合同编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房源 ID" prop="houseId">
            <el-input-number v-model="formData.houseId" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="租客 ID" prop="tenantUserId">
            <el-input-number v-model="formData.tenantUserId" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同状态" prop="status">
            <el-select v-model="formData.status" class="!w-full" clearable placeholder="请选择合同状态">
              <el-option label="待签署" :value="0" />
              <el-option label="待缴费" :value="1" />
              <el-option label="生效中" :value="2" />
              <el-option label="即将到期" :value="3" />
              <el-option label="退租处理中" :value="4" />
              <el-option label="已到期" :value="5" />
              <el-option label="已退租" :value="6" />
              <el-option label="已取消" :value="7" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="租期开始日期" prop="rentStartDate">
            <el-date-picker
              v-model="formData.rentStartDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
              placeholder="请选择租期开始日期"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="租期结束日期" prop="rentEndDate">
            <el-date-picker
              v-model="formData.rentEndDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
              placeholder="请选择租期结束日期"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="月租金(元)" prop="monthlyRent">
            <el-input-number v-model="formData.monthlyRent" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="押金(元)" prop="depositAmount">
            <el-input-number v-model="formData.depositAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="付款方式" prop="paymentMethod">
            <el-input v-model="formData.paymentMethod" placeholder="如：押一付三" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="物业费单价" prop="propertyFeeUnit">
            <el-input-number v-model="formData.propertyFeeUnit" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="合同模板" prop="templateId">
            <div class="template-row">
              <el-select v-model="formData.templateId" clearable class="!w-320px" placeholder="选择模板后可一键套用正文">
                <el-option
                  v-for="tpl in templates"
                  :key="tpl.id"
                  :label="`${tpl.templateName}（${typeLabel(tpl.templateType)} v${tpl.version}）`"
                  :value="tpl.id!"
                />
              </el-select>
              <el-button :disabled="!formData.templateId" @click="applyTemplate">套用模板正文</el-button>
              <span class="template-tip">
                套用会用模板覆盖下面的正文；正文里的 ${变量名} 由后端在保存时替换为真实数据
              </span>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="合同正文" prop="content">
            <Editor v-model="formData.content" editor-id="contract-editor" :height="360" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as ContractApi from '@/api/rental/contract'
import * as ContractTemplateApi from '@/api/rental/contractTemplate'
import { Editor } from '@/components/Editor'

defineOptions({ name: 'RentalContractForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const templates = ref<ContractTemplateApi.ContractTemplateVO[]>([])

const TYPE_LABELS = ['标准租赁合同', '短期租赁合同', '商业租赁合同']
const typeLabel = (type?: number) => (type == null ? '-' : TYPE_LABELS[type] || '-')

const formData = ref<ContractApi.ContractVO>({
  id: undefined,
  contractNo: '',
  houseId: undefined,
  tenantUserId: undefined,
  rentStartDate: undefined,
  rentEndDate: undefined,
  monthlyRent: undefined,
  depositAmount: undefined,
  paymentMethod: '',
  propertyFeeUnit: undefined,
  status: 0,
  templateId: undefined,
  content: ''
})
const formRules = reactive({
  houseId: [{ required: true, message: '房源不能为空', trigger: 'blur' }],
  tenantUserId: [{ required: true, message: '租客不能为空', trigger: 'blur' }],
  rentStartDate: [{ required: true, message: '租期开始日期不能为空', trigger: 'blur' }],
  rentEndDate: [{ required: true, message: '租期结束日期不能为空', trigger: 'blur' }],
  monthlyRent: [{ required: true, message: '月租金不能为空', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '付款方式不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 下拉是精简列表（不含正文），只在第一次打开时拉一次
  if (templates.value.length === 0) {
    templates.value = (await ContractTemplateApi.getEnabledTemplateList()) || []
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ContractApi.getContract(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

// 套用模板：把模板正文拉到富文本里。此时正文里还是 ${变量名} 占位符，
// 真正的替换在后端保存时做 —— 前端拿不到房源/租客的详细信息
const applyTemplate = async () => {
  if (!formData.value.templateId) return
  const tpl = await ContractTemplateApi.getContractTemplate(formData.value.templateId)
  formData.value.content = tpl.content || ''
  message.success('已套用模板正文，保存时自动替换变量')
}

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value
    if (formType.value === 'create') {
      await ContractApi.createContract(data)
      message.success(t('common.createSuccess'))
    } else {
      await ContractApi.updateContract(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    contractNo: '',
    houseId: undefined,
    tenantUserId: undefined,
    rentStartDate: undefined,
    rentEndDate: undefined,
    monthlyRent: undefined,
    depositAmount: undefined,
    paymentMethod: '',
    propertyFeeUnit: undefined,
    status: 0,
    templateId: undefined,
    content: ''
  }
  formRef.value?.resetFields()
}
</script>

<style scoped>
.template-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.template-tip {
  color: #909399;
  font-size: 12px;
}
</style>
