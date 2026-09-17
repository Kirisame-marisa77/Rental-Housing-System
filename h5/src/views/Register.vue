<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2>社区房屋租赁</h2>
      <el-tabs v-model="role" class="roles" @tab-change="onRoleChange">
        <el-tab-pane label="业主注册" name="owner" />
        <el-tab-pane label="租客注册" name="tenant" />
      </el-tabs>
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="form.name" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.idCard" placeholder="请输入身份证号" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="请输入密码" /></el-form-item>
        <!-- 业主必填银行卡号，租客必填工作单位（租客端工作单位选填，与后端 DTO 一致） -->
        <el-form-item v-if="role === 'owner'" label="银行卡号">
          <el-input v-model="form.bankCard" placeholder="请输入银行卡号" />
        </el-form-item>
        <el-form-item v-if="role === 'tenant'" label="工作单位">
          <el-input v-model="form.workUnit" placeholder="请输入工作单位" />
        </el-form-item>
        <el-form-item label="紧急联系人"><el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" /></el-form-item>
        <el-form-item label="紧急电话"><el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-button type="primary" class="w-full" :loading="loading" @click="submit">注 册</el-button>
        <el-button class="w-full reg" text type="primary" @click="goLogin">已有账号？去登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ownerRegister, tenantRegister } from '../api'
import { ROLE_HOME, isRole, setSession, getLastRole } from '../auth'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const role = ref(isRole(route.query.role) ? route.query.role : getLastRole() || 'owner')

watch(role, (r) => router.replace({ query: { ...route.query, role: r } }))

const blank = () => ({
  name: '',
  idCard: '',
  phone: '',
  password: '',
  bankCard: '', // 业主
  workUnit: '', // 租客
  emergencyContact: '',
  emergencyPhone: '',
  gender: 0
})
const form = reactive(blank())

const onRoleChange = () => {
  // 两种表单形状不同，整体重置比带着上一个角色的字段过去更不意外
  Object.assign(form, blank())
}

const goLogin = () => router.push({ path: '/login', query: { role: role.value } })

/**
 * 按角色显式白名单构造 payload，不要 {...form}
 * 后端是 8 个字段的 DTO：业主有 bankCard 无 workUnit，租客反之。
 * 白名单既避免依赖 Jackson 忽略未知属性，也保证切 tab 时旧值不会串味
 */
const buildPayload = () => {
  const base = {
    name: form.name,
    idCard: form.idCard,
    phone: form.phone,
    password: form.password,
    emergencyContact: form.emergencyContact,
    emergencyPhone: form.emergencyPhone,
    gender: form.gender
  }
  return role.value === 'owner'
    ? { ...base, bankCard: form.bankCard }
    : { ...base, workUnit: form.workUnit }
}

const REQUIRED = {
  owner: ['name', 'idCard', 'phone', 'password', 'bankCard', 'emergencyContact'],
  tenant: ['name', 'idCard', 'phone', 'password', 'emergencyContact']
}

const REQUIRED_LABEL = {
  owner: '姓名、身份证号、手机号、密码、银行卡号、紧急联系人',
  tenant: '姓名、身份证号、手机号、密码、紧急联系人'
}

const submit = async () => {
  if (REQUIRED[role.value].some((k) => !form[k])) {
    ElMessage.warning('请填写完整信息（' + REQUIRED_LABEL[role.value] + '）')
    return
  }
  loading.value = true
  try {
    const payload = buildPayload()
    const data = role.value === 'owner'
      ? await ownerRegister(payload)
      : await tenantRegister(payload)
    setSession(role.value, { ...data, name: data.name || form.name })
    ElMessage.success('注册成功')
    router.replace(ROLE_HOME[role.value])
  } catch (e) {
    // 接口错误已由拦截器提示；setSession 的异常要显式暴露出来
    if (e?.message && !e.response) ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 0;
}
.login-card {
  width: 420px;
  max-width: 92%;
}
h2 {
  text-align: center;
  margin-bottom: 8px;
}
.roles {
  margin-bottom: 8px;
}
.w-full {
  width: 100%;
}
.reg {
  margin-top: 8px;
}
</style>
