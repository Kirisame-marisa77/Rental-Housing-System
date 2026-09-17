<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="90px">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="公告标题" prop="title">
            <el-input v-model="formData.title" placeholder="请输入公告标题" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公告分类" prop="category">
            <el-select v-model="formData.category" class="!w-full" placeholder="请选择公告分类">
              <el-option label="缴费通知" value="缴费通知" />
              <el-option label="维修通知" value="维修通知" />
              <el-option label="社区公告" value="社区公告" />
              <el-option label="紧急通知" value="紧急通知" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="是否置顶" prop="isTop">
            <el-radio-group v-model="formData.isTop">
              <el-radio :value="0">否</el-radio>
              <el-radio :value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="0">草稿</el-radio>
              <el-radio :value="1">已发布</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="公告内容" prop="content">
            <el-input v-model="formData.content" type="textarea" :rows="8" placeholder="请输入公告内容（支持文字、图片链接）" />
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
import * as AnnouncementApi from '@/api/rental/announcement'

defineOptions({ name: 'RentalAnnouncementForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<AnnouncementApi.AnnouncementVO>({
  id: undefined,
  title: '',
  category: '社区公告',
  isTop: 0,
  status: 0,
  content: ''
})
const formRules = reactive({
  title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
  category: [{ required: true, message: '公告分类不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }]
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
      formData.value = await AnnouncementApi.getAnnouncement(id)
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
      await AnnouncementApi.createAnnouncement(data)
      message.success(t('common.createSuccess'))
    } else {
      await AnnouncementApi.updateAnnouncement(data)
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
    title: '',
    category: '社区公告',
    isTop: 0,
    status: 0,
    content: ''
  }
  formRef.value?.resetFields()
}
</script>
