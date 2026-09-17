<template>
  <el-card>
    <h3>欢迎，{{ tenantName }}</h3>
    <p class="desc">这里是租客端，您可以：</p>
    <el-row :gutter="12">
      <el-col :span="8" v-for="item in menus" :key="item.path">
        <el-card shadow="hover" class="entry" @click="go(item.path)">
          <div class="entry-title">{{ item.title }}</div>
          <div class="entry-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { getUserName } from '../../auth'

const router = useRouter()
const tenantName = getUserName('tenant') || '租客'

const menus = [
  { path: '/tenant/houses', title: '找房', desc: '浏览上架房源，收藏 / 预约看房 / 提交申请' },
  { path: '/tenant/favorites', title: '我的收藏', desc: '收藏的房源，随时回看并申请' },
  { path: '/tenant/appointments', title: '我的预约', desc: '查看看房预约及房东确认进度' },
  { path: '/tenant/applies', title: '我的申请', desc: '查看租房申请及审批进度' },
  { path: '/tenant/contracts', title: '我的合同', desc: '查看合同、确认签约' },
  { path: '/tenant/bills', title: '我的账单', desc: '缴纳首期账单（押金 + 首月租金）' },
  { path: '/tenant/announcements', title: '公告', desc: '查看社区公告与通知' },
  { path: '/tenant/repairs', title: '报修', desc: '提交报修工单并跟踪处理进度' },
  { path: '/tenant/profile', title: '个人信息', desc: '维护个人资料、改密码' }
]

const go = (path) => router.push(path)
</script>

<style scoped>
.desc {
  color: #999;
  margin: 8px 0 16px;
}
.entry {
  cursor: pointer;
  text-align: center;
}
.entry-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
}
.entry-desc {
  font-size: 12px;
  color: #999;
  line-height: 1.6;
}
</style>
