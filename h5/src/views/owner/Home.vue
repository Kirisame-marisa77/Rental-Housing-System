<template>
  <el-card>
    <h3>欢迎，{{ ownerName }}</h3>
    <p class="desc">这里是业主端，您可以完成以下操作：</p>
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
const ownerName = getUserName('owner') || '业主'

const menus = [
  { path: '/owner/houses', title: '上传房源', desc: '上传房源信息，设置水电计价（水费梯度/电费峰谷）' },
  { path: '/owner/applies', title: '租房申请', desc: '查看租客申请，同意后自动生成合同与首期账单' },
  { path: '/owner/appointments', title: '看房预约', desc: '确认租客的看房预约，完成后填写反馈' },
  { path: '/owner/meters', title: '上传水电', desc: '上传水电读数 + 表盘截图，等待管理员审核' },
  { path: '/owner/repairs', title: '处理维修', desc: '查看租客报修，处理并上传处理证据' },
  { path: '/owner/contracts', title: '合同', desc: '查看合同详情并确认签约' },
  { path: '/owner/bills', title: '我的账单', desc: '查看名下房源的租金/物业费与水电费账单（只读）' },
  { path: '/owner/profile', title: '个人信息', desc: '维护个人资料' }
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
