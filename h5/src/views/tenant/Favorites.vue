<template>
  <!-- loading 交给 CardList：遮罩只盖列表区，标题栏仍然可见、可点 -->
  <AppPage title="我的收藏">
    <CardList :data="list" :loading="loading" empty-text="还没有收藏任何房源">
      <template #item="{ row }">
        <InfoCard
          :title="houseText(row, { style: 'slash', fallback: 'id' })"
          :subtitle="[row.layout, row.squareArea && `${row.squareArea}㎡`].filter(Boolean).join(' · ')"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button
              v-if="row.houseStatus === 1"
              link
              type="primary"
              @click="router.push('/tenant/houses')"
            >
              去申请
            </el-button>
            <el-button link type="danger" @click="handleCancel(row)">取消收藏</el-button>
          </template>
        </InfoCard>
      </template>
    </CardList>
  </AppPage>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelTenantFavorite, getTenantFavorites } from '../../api'
import { houseStatusLabel, houseStatusType } from '../../utils/dict'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const router = useRouter()
const loading = ref(false)
const list = ref([])

// 状态是枚举，进 tags；其余进 fields
const tagsOf = (row) => [
  { text: houseStatusLabel(row.houseStatus), type: houseStatusType(row.houseStatus) }
]

const fieldsOf = (row) => [
  { label: '月租金', value: `¥${row.monthlyRent ?? 0}`, type: 'amount' },
  { label: '房东', value: row.ownerName },
  { label: '房东电话', value: row.ownerPhone },
  { label: '收藏时间', value: row.createTime }
]

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
    const name = houseText(row, { style: 'slash', fallback: 'id' })
    await ElMessageBox.confirm(`确认取消收藏「${name}」吗？`, '取消收藏', { type: 'warning' })
    await cancelTenantFavorite(row.houseId)
    ElMessage.success('已取消收藏')
    getList()
  } catch {}
}

onMounted(getList)
</script>
