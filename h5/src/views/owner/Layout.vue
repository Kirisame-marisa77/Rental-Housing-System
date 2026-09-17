<template>
  <div class="layout">
    <header>
      <span class="title">社区房屋租赁 · 业主端</span>
      <span class="spacer" />
      <span class="name">{{ ownerName }}</span>
      <el-button link type="danger" @click="logout">退出</el-button>
    </header>
    <el-menu mode="horizontal" :default-active="route.path" router class="nav">
      <el-menu-item index="/owner/home">首页</el-menu-item>
      <el-menu-item index="/owner/houses">我的房源</el-menu-item>
      <el-menu-item index="/owner/applies">租房申请</el-menu-item>
      <el-menu-item index="/owner/appointments">看房预约</el-menu-item>
      <el-menu-item index="/owner/meters">水电抄表</el-menu-item>
      <el-menu-item index="/owner/repairs">维修工单</el-menu-item>
      <el-menu-item index="/owner/contracts">合同</el-menu-item>
      <el-menu-item index="/owner/bills">我的账单</el-menu-item>
      <el-menu-item index="/owner/profile">个人信息</el-menu-item>
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
const ownerName = getUserName('owner') || '业主'

const logout = () => {
  // 只清除业主会话，租客会话不受影响
  clearSession('owner')
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
