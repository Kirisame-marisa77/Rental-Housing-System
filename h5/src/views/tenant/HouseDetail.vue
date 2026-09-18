<template>
  <AppPage title="房源详情" show-back :loading="loading">

    <!-- 实景图：左大图 + 下缩略图；无图时给空状态，不要留白 -->
    <el-card class="block">
      <template #header><span class="card-title">房源实景</span></template>
      <template v-if="reality.length">
        <el-image
          :src="reality[activeIndex]"
          :preview-src-list="reality"
          :initial-index="activeIndex"
          :preview-teleported="true"
          fit="cover"
          class="main-img"
        />
        <div v-if="reality.length > 1" class="thumbs">
          <el-image
            v-for="(url, index) in reality"
            :key="url"
            :src="url"
            fit="cover"
            :class="['thumb', { active: index === activeIndex }]"
            @click="activeIndex = index"
          />
        </div>
      </template>
      <el-empty v-else description="房东暂未上传实景图" :image-size="80" />
    </el-card>

    <el-card class="block">
      <template #header><span class="card-title">房源信息</span></template>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="小区">{{ d.communityName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ address }}</el-descriptions-item>
        <el-descriptions-item label="户型">{{ d.layout || '-' }}</el-descriptions-item>
        <el-descriptions-item label="建筑面积">{{ d.squareArea != null ? d.squareArea + ' ㎡' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="朝向">{{ d.orientation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ floorText }}</el-descriptions-item>
        <el-descriptions-item label="装修">{{ d.decoration || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="d.status === 1 ? 'success' : 'info'">{{ statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="月租金">
          <span class="rent">¥{{ d.monthlyRent }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="押金">¥{{ d.deposit ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ d.paymentMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="配套设施" :span="2">
          <template v-if="facilities.length">
            <el-tag v-for="f in facilities" :key="f" class="facility">{{ f }}</el-tag>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="房源描述" :span="2">{{ d.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 户型图单独一段：和实景图混在一起看，租客分不清哪张是照片哪张是图纸 -->
      <template v-if="layoutImgs.length">
        <el-divider content-position="left">户型图</el-divider>
        <div class="layout-imgs">
          <el-image
            v-for="url in layoutImgs"
            :key="url"
            :src="url"
            :preview-src-list="layoutImgs"
            :preview-teleported="true"
            fit="contain"
            class="layout-img"
          />
        </div>
      </template>
    </el-card>

    <el-card class="block">
      <template #header><span class="card-title">房东信息</span></template>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="房东">{{ d.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">
          <el-link v-if="d.ownerPhone" type="primary" :href="'tel:' + d.ownerPhone">{{ d.ownerPhone }}</el-link>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 操作条固定在底部（tab 栏之上），长页面滑到底也不用回头找按钮 -->
    <template #footer>
      <el-button type="warning" plain @click="toggleFavorite">
        {{ favored ? '已收藏' : '收藏' }}
      </el-button>
      <el-button
        type="success"
        :disabled="!rentable"
        class="detail-footer__grow"
        @click="openAppointment"
      >
        预约看房
      </el-button>
      <el-button
        type="primary"
        :disabled="!rentable"
        class="detail-footer__grow"
        @click="openApply"
      >
        申请租房
      </el-button>
    </template>

    <!-- 与找房列表里那两个弹窗字段保持一致 -->
    <el-dialog v-model="applyVisible" title="提交租房申请" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="房源">
          <span>{{ d.communityName }} {{ d.buildingNo }}/{{ d.roomNo }}</span>
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
          <span>{{ d.communityName }} {{ d.buildingNo }}/{{ d.roomNo }}</span>
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
          <el-time-select v-model="appointForm.startTime" start="08:00" step="01:00" end="20:00" placeholder="请选择开始时间" class="u-w-full" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select v-model="appointForm.endTime" start="08:00" step="01:00" end="20:00" placeholder="请选择结束时间" class="u-w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAppointment">提交预约</el-button>
        <el-button @click="appointVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </AppPage>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  addTenantFavorite,
  cancelTenantFavorite,
  createTenantAppointment,
  createTenantApply,
  getTenantFavoriteHouseIds,
  getTenantHouseDetail
} from '../../api'
import { resolveImageUrls } from '../../utils/image'
import AppPage from '../../components/AppPage.vue'
import { DEFAULT_PAYMENT_METHOD, calcFirstPayment, payMonthsOf } from '../../utils/payment'

const route = useRoute()
const router = useRouter()


const loading = ref(false)
const d = ref({})
const reality = ref([])
const layoutImgs = ref([])
const activeIndex = ref(0)
const favored = ref(false)

// route.params.id 是字符串；收藏 ID 集合里是数字，比对前必须转
const houseId = computed(() => Number(route.params.id))

const facilities = computed(() => {
  try {
    const parsed = JSON.parse(d.value.facilities || '[]')
    return Array.isArray(parsed) ? parsed : []
  } catch {
    // facilities 是手填的 JSON 字符串，脏数据不能让整个页面白屏
    return []
  }
})

const address = computed(() =>
  [d.value.area, d.value.communityName, d.value.buildingNo, d.value.roomNo]
    .filter(Boolean)
    .join(' ')
)

const floorText = computed(() => {
  if (d.value.floor == null) {
    return '-'
  }
  return d.value.totalFloor ? `${d.value.floor}/${d.value.totalFloor} 层` : `${d.value.floor} 层`
})

const statusLabel = computed(
  () => ({ 0: '已下架', 1: '可租', 2: '签约中', 3: '已出租' }[d.value.status] ?? '-')
)

// 只有「上架」才能申请/预约；2-签约中、3-已出租 都不行
const rentable = computed(() => d.value.status === 1)

const load = async () => {
  loading.value = true
  try {
    const data = await getTenantHouseDetail(houseId.value)
    d.value = data || {}
    reality.value = resolveImageUrls(data?.realityImages?.map((i) => i.imageUrl))
    layoutImgs.value = resolveImageUrls(data?.layoutImages?.map((i) => i.imageUrl))
    activeIndex.value = 0
    // 收藏态：复用「拉全量 ID 再本地比对」，与找房列表保持一致
    const ids = await getTenantFavoriteHouseIds()
    favored.value = new Set(ids || []).has(houseId.value)
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (favored.value) {
    await cancelTenantFavorite(houseId.value)
  } else {
    await addTenantFavorite(houseId.value)
  }
  favored.value = !favored.value
  ElMessage.success(favored.value ? '收藏成功' : '已取消收藏')
}

// ===== 预约看房 =====
const appointVisible = ref(false)
const appointForm = reactive({ houseId: undefined, appointmentDate: '', startTime: '', endTime: '' })

const disabledDate = (date) => date.getTime() < new Date(new Date().toDateString()).getTime()

const openAppointment = () => {
  appointForm.houseId = houseId.value
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

// ===== 租房申请 =====
const applyVisible = ref(false)
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

const openApply = () => {
  form.houseId = houseId.value
  form.moveInDate = ''
  form.leaseTerm = 12
  // 租金、押金、付款方式三项一律用房源上的值，页面上也是只读的
  form.paymentMethod = d.value.paymentMethod || DEFAULT_PAYMENT_METHOD
  form.monthlyRent = d.value.monthlyRent
  form.depositAmount = d.value.deposit
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

onMounted(load)
</script>

<style scoped>
.page-header {
  margin-bottom: 12px;
}
.block {
  margin-bottom: var(--app-gap);
}

/* 底部操作条里两个主操作等宽，收藏按钮保持自适应宽度 */
.detail-footer__grow {
  flex: 1;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.main-img {
  width: 100%;
  /* 固定高度在窄屏比例失真，改成按 4:3 自适应 */
  aspect-ratio: 4 / 3;
  height: auto;
  border-radius: var(--app-radius-sm);
  cursor: pointer;
}
.thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.thumb {
  width: 88px;
  height: 66px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  box-sizing: border-box;
}
.thumb.active {
  border-color: var(--el-color-primary);
}
.layout-imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.layout-img {
  width: 100%;
  max-width: 260px;
  flex: 1 1 220px;
  aspect-ratio: 4 / 3;
  height: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  cursor: pointer;
}
.rent {
  color: var(--el-color-danger);
  font-size: 16px;
  font-weight: 600;
}
.facility {
  margin: 0 6px 6px 0;
}
.tip {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.w-full {
  width: 100%;
}
</style>
