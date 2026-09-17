<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2>社区房屋租赁</h2>
      <!-- 角色 tab：业主与租客是两张独立的表、手机号可能同时存在（两条独立自增序列），
           所以必须由用户明确选身份，不能靠"先试租客再试业主"去猜 -->
      <el-tabs v-model="role" class="roles" @tab-change="onRoleChange">
        <el-tab-pane label="业主登录" name="owner" />
        <el-tab-pane label="租客登录" name="tenant" />
      </el-tabs>
      <el-form :model="form" label-width="90px">
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="w-full" :loading="loading" @click="submit">登 录</el-button>
        <el-button class="w-full reg" text type="primary" @click="goRegister">没有账号？去注册</el-button>
      </el-form>
      <p class="tip">说明：使用{{ ROLE_LABEL[role] }}账号登录（手机号 + 密码）。</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ownerLogin, tenantLogin } from '../api'
import { ROLE_LABEL, ROLE_HOME, isRole, setSession, getLastRole } from '../auth'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const form = reactive({ phone: '', password: '' })

const role = ref(isRole(route.query.role) ? route.query.role : getLastRole() || 'owner')

// 角色同步到 URL：可分享、刷新不丢
watch(role, (r) => router.replace({ query: { ...route.query, role: r } }))

const onRoleChange = () => {
  // 切换身份时清掉密码。手机号保留 —— 同一个人可能在两张表里都有账号，
  // 而这正是需要角色 tab 的原因
  form.password = ''
}

const goRegister = () => router.push({ path: '/register', query: { role: role.value } })

const submit = async () => {
  if (!form.phone || !form.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }
  loading.value = true
  try {
    const data = role.value === 'owner'
      ? await ownerLogin(form.phone, form.password)
      : await tenantLogin(form.phone, form.password)
    setSession(role.value, { ...data, name: data.name || form.phone })
    ElMessage.success('登录成功')
    router.replace(resolveTarget())
  } catch (e) {
    // 接口错误已由拦截器提示；setSession 的异常要显式暴露出来
    if (e?.message && !e.response) ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

/**
 * 回跳目标必须是本角色前缀内的路径。
 * 不校验的话，被篡改或过期的 ?redirect=/tenant/bills 会把刚登录的业主送进租客树，
 * 触发二次登录，看起来像 bug
 */
const resolveTarget = () => {
  const raw = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  return raw.startsWith(`/${role.value}/`) ? raw : ROLE_HOME[role.value]
}
</script>

<style scoped>
.login-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
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
.tip {
  margin-top: 12px;
  font-size: 12px;
  color: #999;
}
</style>
