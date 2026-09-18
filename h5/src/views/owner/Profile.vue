<template>
  <AppPage title="个人信息">
    <el-form :model="form" label-width="100px" label-position="top" class="form">
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

    <el-divider />
    <div class="danger">
      <div class="danger-text">
        <div class="danger-title">注销账号</div>
        <div class="tip">
          需先结清全部合同（名下不能有出租中或签约中的房源）。
          注销时名下房源会全部下架，手机号可重新注册，历史合同与账单仍会保留。
        </div>
      </div>
      <el-button type="danger" plain @click="openDeregister">注销账号</el-button>
    </div>

    <el-dialog v-model="deregVisible" title="注销账号" width="460px">
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        class="mb"
        title="此操作不可撤销"
        description="注销时名下房源将全部下架；需用同一手机号重新注册，历史数据不会恢复到新账号。"
      />
      <el-form label-width="100px">
        <el-form-item label="登录密码">
          <el-input v-model="deregForm.password" type="password" show-password placeholder="请输入当前登录密码" />
        </el-form-item>
        <el-form-item label="确认注销">
          <el-input v-model="deregForm.confirmText" placeholder="请输入「注销」两个字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="danger"
          :loading="deregLoading"
          :disabled="deregForm.confirmText !== '注销'"
          @click="submitDeregister"
        >
          确认注销
        </el-button>
        <el-button @click="deregVisible = false">取消</el-button>
      </template>
    </el-dialog>

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
  </AppPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOwnerInfo, updateOwnerInfo, changeOwnerPassword, deregisterOwner } from '../../api'
import { setUserName, clearSession } from '../../auth'
import AppPage from '../../components/AppPage.vue'

const router = useRouter()

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

// 注销账号
const deregVisible = ref(false)
const deregLoading = ref(false)
const deregForm = reactive({ password: '', confirmText: '' })

const openDeregister = () => {
  deregForm.password = ''
  deregForm.confirmText = ''
  deregVisible.value = true
}

const submitDeregister = async () => {
  if (!deregForm.password) {
    ElMessage.warning('请输入登录密码')
    return
  }
  deregLoading.value = true
  try {
    await deregisterOwner(deregForm.password)
    // 后端已经 logoutAll 让 token 失效，这里只清本地会话（只清业主键，租客会话不受影响）
    clearSession('owner')
    ElMessage.success('账号已注销，感谢使用')
    router.push('/login')
  } finally {
    deregLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.form {
  max-width: 520px;
}
.btns {
  display: flex;
  gap: 12px;
}
.danger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  max-width: 520px;
  padding: 12px 16px;
  border: 1px solid var(--el-color-danger-light-5);
  border-radius: 4px;
  background: var(--el-color-danger-light-9);
}
.danger-title {
  font-weight: 600;
  color: var(--el-color-danger);
  margin-bottom: 4px;
}
.tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
.mb {
  margin-bottom: 12px;
}
</style>
