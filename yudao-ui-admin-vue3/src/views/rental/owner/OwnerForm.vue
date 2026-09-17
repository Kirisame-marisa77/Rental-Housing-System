<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="90px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="业主姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入业主姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="登录密码" prop="password">
            <el-input v-model="formData.password" type="password" show-password placeholder="留空默认 123456" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银行卡号" prop="bankCard">
            <el-input v-model="formData.bankCard" placeholder="请输入银行卡号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio :value="0">未知</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="紧急联系人" prop="emergencyContact">
            <el-input v-model="formData.emergencyContact" placeholder="请输入紧急联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="紧急联系电话" prop="emergencyPhone">
            <el-input v-model="formData.emergencyPhone" placeholder="请输入紧急联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
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
import * as OwnerApi from '@/api/rental/owner'

defineOptions({ name: 'RentalOwnerForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<OwnerApi.OwnerInfoVO>({
  id: undefined,
  userId: undefined,
  name: '',
  idCard: '',
  bankCard: '',
  phone: '',
  password: '',
  gender: 0,
  emergencyContact: '',
  emergencyPhone: '',
  remark: ''
})
const formRules = reactive({
  name: [{ required: true, message: '业主姓名不能为空', trigger: 'blur' }],
  phone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await OwnerApi.getOwnerInfo(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value
    if (formType.value === 'create') {
      await OwnerApi.createOwnerInfo(data)
      message.success(t('common.createSuccess'))
    } else {
      await OwnerApi.updateOwnerInfo(data)
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
    userId: undefined,
    name: '',
    idCard: '',
    bankCard: '',
    phone: '',
    password: '',
    gender: 0,
    emergencyContact: '',
    emergencyPhone: '',
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
