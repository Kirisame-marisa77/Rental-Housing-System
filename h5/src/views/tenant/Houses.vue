<template>
  <div>
    <AppPage title="找房" subtitle="上架房源">
      <CardList :data="list" :loading="loading" empty-text="当前没有可租的房源">
        <template #item="{ row }">
          <!-- 整卡可点进详情，取代原来「小区」列上的 el-link -->
          <InfoCard
            :title="houseTitle(row)"
            :subtitle="row.description"
            :tags="tagsOf(row)"
            :fields="fieldsOf(row)"
            clickable
            @click="goDetail(row)"
          >
            <template #actions>
              <el-button link type="primary" @click="goDetail(row)">详情</el-button>
              <el-button
                link
                :type="isFavorite(row.id) ? 'warning' : 'info'"
                @click="toggleFavorite(row)"
              >
                {{ isFavorite(row.id) ? '已收藏' : '收藏' }}
              </el-button>
              <el-button link type="success" @click="openAppointment(row)">预约看房</el-button>
              <el-button link type="primary" @click="openApply(row)">申请</el-button>
            </template>
          </InfoCard>
        </template>
      </CardList>
    </AppPage>

    <el-dialog v-model="applyVisible" title="提交租房申请" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="房源">
          <span>{{ currentHouse?.communityName }} {{ currentHouse?.buildingNo }}/{{ currentHouse?.roomNo }}</span>
        </el-form-item>
        <el-form-item label="期望入住日期">
          <el-date-picker v-model="form.moveInDate" type="date" value-format="YYYY-MM-DD" class="u-w-full" />
        </el-form-item>
        <el-form-item label="租期(月)">
          <el-input-number v-model="form.leaseTerm" :min="1" :max="120" class="u-w-full" />
        </el-form-item>
        <!-- 付款方式是房东出租房时定下的条件，和租金押金一样属于房源属性，租客不可更改 -->
        <el-form-item label="付款方式">
          <el-input :model-value="form.paymentMethod" disabled class="u-w-full" />
        </el-form-item>
        <!-- 租金与押金是房源属性，由房东设定，租客只能看不能改（后端也会用房源的值覆盖） -->
        <el-form-item label="月租金(元)">
          <el-input :model-value="form.monthlyRent" disabled class="u-w-full" />
        </el-form-item>
        <el-form-item label="押金(元)">
          <el-input :model-value="form.depositAmount" disabled class="u-w-full" />
        </el-form-item>
        <el-form-item label="首期应缴(元)">
          <el-input :model-value="firstPayment.toFixed(2)" disabled class="u-w-full" />
          <div class="tip">
            押金 {{ Number(form.depositAmount || 0).toFixed(2) }}
            + {{ payMonthsOf(form.paymentMethod) }} 个月租金，租期从入住日开始算
          </div>
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
            class="u-w-full"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-select
            v-model="appointForm.startTime"
            start="08:00"
            step="01:00"
            end="20:00"
            placeholder="请选择开始时间"
            class="u-w-full"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select
            v-model="appointForm.endTime"
            start="08:00"
            step="01:00"
            end="20:00"
            placeholder="请选择结束时间"
            class="u-w-full"
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
import { computed, ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getTenantHouses,
  createTenantApply,
  getTenantFavoriteHouseIds,
  addTenantFavorite,
  cancelTenantFavorite,
  createTenantAppointment
} from '../../api'
import { DEFAULT_PAYMENT_METHOD, calcFirstPayment, payMonthsOf } from '../../utils/payment'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const router = useRouter()

const loading = ref(false)
const list = ref([])

const goDetail = (house) => router.push(`/tenant/houses/${house.id}`)

// 卡片标题 = 「小区 楼栋号楼 房号室」，与详情页保持一致
const houseTitle = (row) =>
  [row.communityName, row.buildingNo && `${row.buildingNo}号楼`, row.roomNo && `${row.roomNo}室`]
    .filter(Boolean)
    .join(' ')

// 列表接口只返回上架房源，但保留状态标签让租客一眼确认可租
const tagsOf = (row) => [
  { text: '可租', type: 'success' },
  ...(row.decoration ? [{ text: row.decoration, type: 'info' }] : [])
]

const fieldsOf = (row) => [
  { label: '户型', value: row.layout },
  { label: '面积', value: row.squareArea != null ? `${row.squareArea}㎡` : '-' },
  { label: '月租金', value: `¥${row.monthlyRent ?? 0}`, type: 'amount' },
  { label: '押金', value: `¥${row.deposit ?? 0}` },
  { label: '付款方式', value: row.paymentMethod },
  { label: '朝向', value: row.orientation }
]

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
  paymentMethod: DEFAULT_PAYMENT_METHOD,
  monthlyRent: undefined,
  depositAmount: undefined,
  tenantRemark: ''
})

// 首期应缴 = 押金 + 付数 × 月租金，全部取自房源，租客改不了
const firstPayment = computed(() =>
  calcFirstPayment(form.monthlyRent, form.depositAmount, form.paymentMethod)
)

const openApply = (house) => {
  currentHouse.value = house
  form.houseId = house.id
  form.moveInDate = ''
  form.leaseTerm = 12
  // 租金、押金、付款方式三项一律用房源上的值，页面上也是只读的
  form.paymentMethod = house.paymentMethod || DEFAULT_PAYMENT_METHOD
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
