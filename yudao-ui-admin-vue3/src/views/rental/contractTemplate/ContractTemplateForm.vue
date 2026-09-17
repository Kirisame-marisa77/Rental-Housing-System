<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="1000px" top="5vh">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="90px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="模板名称" prop="templateName">
            <el-input v-model="formData.templateName" placeholder="如：标准租赁合同（2026版）" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="模板类型" prop="templateType">
            <el-select v-model="formData.templateType" class="!w-full" placeholder="请选择模板类型">
              <el-option label="标准租赁合同" :value="0" />
              <el-option label="短期租赁合同" :value="1" />
              <el-option label="商业租赁合同" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="0">启用</el-radio>
              <el-radio :value="1">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="可用变量">
            <!-- 点击即把 ${key} 追加到正文末尾；正文框是富文本、外部无法精确插入光标位置，
                 追加到末尾再用编辑器自行拖拽，比假装能精确插入更诚实 -->
            <div class="vars">
              <el-tag
                v-for="v in variables"
                :key="v.key"
                class="var-tag"
                type="info"
                effect="plain"
                @click="insertVariable(v.key)"
              >
                {{ v.label }}
                <span class="var-key">${{ v.key }}</span>
              </el-tag>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="合同正文" prop="content">
            <Editor
              v-model="formData.content"
              editor-id="contract-template-editor"
              :height="420"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" placeholder="选填" />
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
import * as ContractTemplateApi from '@/api/rental/contractTemplate'
import { Editor } from '@/components/Editor'

defineOptions({ name: 'RentalContractTemplateForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const variables = ref<ContractTemplateApi.TemplateVariableVO[]>([])
const formData = ref<ContractTemplateApi.ContractTemplateVO>({
  id: undefined,
  templateName: '',
  templateType: 0,
  content: '',
  status: 0,
  remark: ''
})
const formRules = reactive({
  templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
  templateType: [{ required: true, message: '模板类型不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '合同正文不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 变量清单是常量，只在第一次打开时拉一次
  if (variables.value.length === 0) {
    variables.value = (await ContractTemplateApi.getTemplateVariables()) || []
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ContractTemplateApi.getContractTemplate(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const insertVariable = (key: string) => {
  // 用 $ 拼串而不是模板字符串：正文里的 ${key} 在 JS 模板字符串里会被当插值
  formData.value.content = (formData.value.content || '') + '${' + key + '}'
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
      await ContractTemplateApi.createContractTemplate(data)
      message.success(t('common.createSuccess'))
    } else {
      await ContractTemplateApi.updateContractTemplate(data)
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
    templateName: '',
    templateType: 0,
    content: '',
    status: 0,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>

<style scoped>
.vars {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.var-tag {
  cursor: pointer;
}
.var-key {
  margin-left: 4px;
  opacity: 0.6;
  font-family: monospace;
}
</style>
