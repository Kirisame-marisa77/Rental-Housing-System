<template>
  <el-card>
    <div class="toolbar">
      <span class="title">我的收藏</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="房源" min-width="180" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column prop="layout" label="户型" width="120" show-overflow-tooltip />
      <el-table-column prop="squareArea" label="面积(㎡)" width="90" />
      <el-table-column prop="monthlyRent" label="月租金(元)" width="110" />
      <el-table-column prop="ownerName" label="房东" width="90" />
      <el-table-column prop="ownerPhone" label="房东电话" width="120" />
      <el-table-column label="房源状态" width="90">
        <template #default="s">
          <el-tag :type="houseType(s.row.houseStatus)">{{ houseLabel(s.row.houseStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="收藏时间" width="170" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="s">
          <el-button v-if="s.row.houseStatus === 1" link type="primary" @click="router.push('/tenant/houses')">
            去申请
          </el-button>
          <el-button link type="danger" @click="handleCancel(s.row)">取消收藏</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTenantFavorites, cancelTenantFavorite } from '../../api'

const router = useRouter()
const loading = ref(false)
const list = ref([])

const houseMap = [
  { value: 0, label: '下架' },
  { value: 1, label: '上架' },
  { value: 2, label: '已锁定' },
  { value: 3, label: '已出租' }
]
const houseLabel = (s) => houseMap.find((i) => i.value === s)?.label || '-'
const houseType = (s) => (s === 1 ? 'success' : s === 3 ? 'info' : s === 2 ? 'warning' : 'danger')

const houseText = (row) =>
  [row.communityName, row.buildingNo && `${row.buildingNo}/${row.roomNo}`].filter(Boolean).join(' ') || `房源 ${row.houseId}`

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantFavorites({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确认取消收藏「${houseText(row)}」吗？`, '取消收藏', { type: 'warning' })
    await cancelTenantFavorite(row.houseId)
    ElMessage.success('已取消收藏')
    getList()
  } catch {}
}

onMounted(getList)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
</style>
