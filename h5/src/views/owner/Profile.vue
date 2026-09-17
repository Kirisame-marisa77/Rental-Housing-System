<template>
  <el-card>
    <div class="toolbar">
      <span class="title">个人信息</span>
    </div>
    <el-form :model="form" label-width="100px" class="form">
      <el-form-item label="业主姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item>
      <el-form-item label="银行卡号"><el-input v-model="form.bankCard" /></el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="0">未知</el-radio>
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="紧急联系人"><el-input v-model="form.emergencyContact" /></el-form-item>
      <el-form-item label="紧急电话"><el-input v-model="form.emergencyPhone" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      <div class="btns">
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="openPassword">修改密码</el-button>
      </div>
    </el-form>

    <el-dialog v-model="pwdVisible" title="修改密码" width="400px">
      <el-form label-width="80px">
        <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPassword">确 定</el-button>
        <el-button @click="pwdVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOwnerInfo, updateOwnerInfo, changeOwnerPassword } from '../../api'
import { setUserName } from '../../auth'

const saving = ref(false)
const form = reactive({
  name: '',
  phone: '',
  idCard: '',
  bankCard: '',
  gender: 0,
  emergencyContact: '',
  emergencyPhone: '',
  remark: ''
})

const load = async () => {
  const data = await getOwnerInfo()
  if (data) {
    Object.assign(form, data)
  }
}

const save = async () => {
  if (!form.name || !form.phone) {
    ElMessage.warning('姓名和手机号不能为空')
    return
  }
  saving.value = true
  try {
    await updateOwnerInfo(form)
    setUserName('owner', form.name)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

// 修改密码
const pwdVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const openPassword = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdVisible.value = true
}
const submitPassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请输入旧密码和新密码')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await changeOwnerPassword(pwdForm.oldPassword, pwdForm.newPassword)
  ElMessage.success('密码修改成功')
  pwdVisible.value = false
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.form {
  max-width: 520px;
}
.btns {
  display: flex;
  gap: 12px;
}
</style>
