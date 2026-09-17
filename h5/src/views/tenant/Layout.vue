<template>
  <div class="layout">
    <header>
      <span class="title">社区房屋租赁 · 租客端</span>
      <span class="spacer" />
      <span class="name">{{ tenantName }}</span>
      <el-button link type="danger" @click="logout">退出</el-button>
    </header>
    <el-menu mode="horizontal" :default-active="route.path" router class="nav">
      <el-menu-item index="/tenant/home">首页</el-menu-item>
      <el-menu-item index="/tenant/houses">找房</el-menu-item>
      <el-menu-item index="/tenant/favorites">我的收藏</el-menu-item>
      <el-menu-item index="/tenant/appointments">我的预约</el-menu-item>
      <el-menu-item index="/tenant/applies">我的申请</el-menu-item>
      <el-menu-item index="/tenant/contracts">我的合同</el-menu-item>
      <el-menu-item index="/tenant/bills">我的账单</el-menu-item>
      <el-menu-item index="/tenant/move-outs">退租申请</el-menu-item>
      <el-menu-item index="/tenant/announcements">公告</el-menu-item>
      <el-menu-item index="/tenant/repairs">报修</el-menu-item>
      <el-menu-item index="/tenant/profile">个人信息</el-menu-item>
    </el-menu>
    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { getUserName, clearSession } from '../../auth'

const route = useRoute()
const router = useRouter()
const tenantName = getUserName('tenant') || '租客'

const logout = () => {
  // 只清除租客会话，业主会话不受影响
  clearSession('tenant')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
header {
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.spacer {
  flex: 1;
}
.name {
  margin-right: 12px;
  color: #666;
}
.nav {
  padding: 0 8px;
}
main {
  flex: 1;
  padding: 16px;
}
</style>
