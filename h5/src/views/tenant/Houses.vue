<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">找房（上架房源）</span>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="communityName" label="小区" show-overflow-tooltip />
        <el-table-column label="楼栋/房号" width="110">
          <template #default="s">{{ s.row.buildingNo }}/{{ s.row.roomNo }}</template>
        </el-table-column>
        <el-table-column prop="layout" label="户型" width="130" show-overflow-tooltip />
        <el-table-column prop="squareArea" label="面积(㎡)" width="90" />
        <el-table-column prop="monthlyRent" label="月租金(元)" width="110" />
        <el-table-column prop="decoration" label="装修" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="s">
            <el-button link :type="isFavorite(s.row.id) ? 'warning' : 'info'" @click="toggleFavorite(s.row)">
              {{ isFavorite(s.row.id) ? '已收藏' : '收藏' }}
            </el-button>
            <el-button link type="success" @click="openAppointment(s.row)">预约看房</el-button>
            <el-button link type="primary" @click="openApply(s.row)">申请</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="applyVisible" title="提交租房申请" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="房源">
          <span>{{ currentHouse?.communityName }} {{ currentHouse?.buildingNo }}/{{ currentHouse?.roomNo }}</span>
        </el-form-item>
        <el-form-item label="期望入住日期">
          <el-date-picker v-model="form.moveInDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
        <el-form-item label="租期(月)">
          <el-input-number v-model="form.leaseTerm" :min="1" :max="120" class="w-full" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="form.paymentMethod" class="w-full">
            <el-option v-for="p in ['押一付一', '押一付三', '押二付一', '押二付三', '自定义']" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="月租金(元)">
          <el-input-number v-model="form.monthlyRent" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="押金(元)">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.tenantRemark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
        <el-button @click="applyVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="appointVisible" title="预约看房" width="480px">
      <el-form :model="appointForm" label-width="110px">
        <el-form-item label="房源">
          <span>{{ currentHouse?.communityName }} {{ currentHouse?.buildingNo }}/{{ currentHouse?.roomNo }}</span>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="appointForm.appointmentDate"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-select
            v-model="appointForm.startTime"
            start="08:00"
            step="01:00"
            end="20:00"
            placeholder="请选择开始时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select
            v-model="appointForm.endTime"
            start="08:00"
            step="01:00"
            end="20:00"
            placeholder="请选择结束时间"
            class="w-full"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAppointment">提交预约</el-button>
        <el-button @click="appointVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTenantHouses,
  createTenantApply,
  getTenantFavoriteHouseIds,
  addTenantFavorite,
  cancelTenantFavorite,
  createTenantAppointment
} from '../../api'

const loading = ref(false)
const list = ref([])

// 已收藏房源 ID 集合。一次性拉全量、本地比对，避免逐行请求；
// 收藏量上万时这个数组会变大，届时改为「按当前页 houseIds 批量查」。
const favoriteIds = ref(new Set())
const isFavorite = (houseId) => favoriteIds.value.has(houseId)

const getList = async () => {
  loading.value = true
  try {
    const [data, ids] = await Promise.all([
      getTenantHouses({ pageNo: 1, pageSize: 100 }),
      getTenantFavoriteHouseIds()
    ])
    list.value = data.list || []
    favoriteIds.value = new Set(ids || [])
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async (house) => {
  const favored = isFavorite(house.id)
  if (favored) {
    await cancelTenantFavorite(house.id)
  } else {
    await addTenantFavorite(house.id)
  }
  // Set 不是响应式的：必须赋新实例，直接 add/delete 界面不会翻转
  const next = new Set(favoriteIds.value)
  favored ? next.delete(house.id) : next.add(house.id)
  favoriteIds.value = next
  ElMessage.success(favored ? '已取消收藏' : '收藏成功')
}

// ===== 预约看房 =====
const appointVisible = ref(false)
const appointForm = reactive({
  houseId: undefined,
  appointmentDate: '',
  startTime: '',
  endTime: ''
})

// 不允许选过去的日期（今天可以）
const disabledDate = (date) => date.getTime() < new Date(new Date().toDateString()).getTime()

const openAppointment = (house) => {
  currentHouse.value = house
  appointForm.houseId = house.id
  appointForm.appointmentDate = ''
  appointForm.startTime = ''
  appointForm.endTime = ''
  appointVisible.value = true
}

const submitAppointment = async () => {
  if (!appointForm.appointmentDate || !appointForm.startTime || !appointForm.endTime) {
    ElMessage.warning('请选择预约日期与时间段')
    return
  }
  if (appointForm.startTime >= appointForm.endTime) {
    ElMessage.warning('开始时间必须早于结束时间')
    return
  }
  await createTenantAppointment(appointForm)
  ElMessage.success('预约已提交，等待房东确认')
  appointVisible.value = false
}

const applyVisible = ref(false)
const currentHouse = ref(null)
const form = reactive({
  houseId: undefined,
  moveInDate: '',
  leaseTerm: 12,
  paymentMethod: '押一付三',
  monthlyRent: undefined,
  depositAmount: undefined,
  tenantRemark: ''
})

const openApply = (house) => {
  currentHouse.value = house
  form.houseId = house.id
  form.moveInDate = ''
  form.leaseTerm = 12
  form.paymentMethod = '押一付三'
  form.monthlyRent = house.monthlyRent
  form.depositAmount = house.deposit
  form.tenantRemark = ''
  applyVisible.value = true
}

const submitApply = async () => {
  if (!form.moveInDate || !form.leaseTerm || !form.paymentMethod || form.monthlyRent == null) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await createTenantApply(form)
  ElMessage.success('申请已提交，等待房东审批')
  applyVisible.value = false
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
.w-full {
  width: 100%;
}
</style>
