<template>
  <div>
    <AppPage title="我的房源">
      <template #actions>
        <el-button type="primary" @click="openUpload">上传房源</el-button>
      </template>

      <CardList :data="list" :loading="loading" empty-text="还没有上传过房源">
        <template #item="{ row }">
          <InfoCard
            :title="houseTitle(row)"
            :subtitle="row.houseNo"
            :tags="tagsOf(row)"
            :fields="fieldsOf(row)"
          >
            <template #actions>
              <!-- 签约中(2)/已出租(3) 由签约与退租流程驱动，手动改会和先到先得的抢占逻辑打架 -->
              <el-button
                link
                :type="row.status === 1 ? 'warning' : 'success'"
                :disabled="row.reviewStatus !== 1 || row.status === 2 || row.status === 3"
                @click="toggleOnline(row)"
              >
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
            </template>
          </InfoCard>
        </template>
      </CardList>
    </AppPage>

    <el-dialog v-model="uploadVisible" title="上传房源" width="640px" top="5vh">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="小区名称"><el-input v-model="form.communityName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所在区域"><el-input v-model="form.area" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="楼栋号"><el-input v-model="form.buildingNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房号"><el-input v-model="form.roomNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="户型"><el-input v-model="form.layout" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="面积(㎡)"><el-input-number v-model="form.squareArea" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="朝向"><el-input v-model="form.orientation" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="装修">
              <el-select v-model="form.decoration" class="u-w-full">
                <el-option v-for="d in ['精装', '简装', '毛坯']" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="月租金(元)"><el-input-number v-model="form.monthlyRent" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="付款方式">
              <el-select v-model="form.paymentMethod" class="u-w-full">
                <el-option v-for="p in PAYMENT_METHODS" :key="p.label" :label="p.label" :value="p.label" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 押金与首期应缴都不手填：由「押N付M」× 月租金自动算出，避免业主把两者填得不匹配 -->
            <el-form-item label="押金(元)">
              <el-input :model-value="depositAmount.toFixed(2)" disabled class="u-w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="首期应缴(元)">
              <el-input :model-value="firstPaymentAmount.toFixed(2)" disabled class="u-w-full" />
              <div class="tip">
                押金 {{ depositAmount.toFixed(2) }} + {{ selectedPayment.payMonths }} 个月租金
                {{ (firstPaymentAmount - depositAmount).toFixed(2) }}
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="房源描述"><el-input v-model="form.description" type="textarea" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">房源图片</el-divider>
        <el-form-item label="实景图">
          <el-upload
            list-type="picture-card"
            :file-list="realityFileList"
            :http-request="(option) => doUpload(option, 'realityImages')"
            :on-remove="(file) => removeImage(file, 'realityImages')"
            :limit="6"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="tip">第一张作为列表封面，最多 6 张</div>
        </el-form-item>
        <el-form-item label="户型图">
          <el-upload
            list-type="picture-card"
            :file-list="layoutFileList"
            :http-request="(option) => doUpload(option, 'layoutImages')"
            :on-remove="(file) => removeImage(file, 'layoutImages')"
            :limit="3"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="tip">最多 3 张</div>
        </el-form-item>

        <el-divider content-position="left">水电计价</el-divider>
        <el-form-item label="水费计价">
          <el-radio-group v-model="form.waterBillType">
            <el-radio :value="0">统一单价</el-radio>
            <el-radio :value="1">三档梯度</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.waterBillType === 0">
          <el-form-item label="水费单价(元/吨)"><el-input-number v-model="form.waterUnitPrice" :min="0" :precision="2" class="u-w-full" /></el-form-item>
        </template>
        <template v-else>
          <el-row :gutter="12">
            <el-col :span="12"><el-form-item label="档一上限(吨)"><el-input-number v-model="form.waterTier1Limit" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档一单价"><el-input-number v-model="form.waterTier1Price" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档二上限(吨)"><el-input-number v-model="form.waterTier2Limit" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档二单价"><el-input-number v-model="form.waterTier2Price" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档三单价"><el-input-number v-model="form.waterTier3Price" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
          </el-row>
        </template>
        <el-form-item label="电费计价">
          <el-radio-group v-model="form.electricityBillType">
            <el-radio :value="0">统一单价</el-radio>
            <el-radio :value="1">峰谷两价</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.electricityBillType === 0">
          <el-form-item label="电费单价(元/度)"><el-input-number v-model="form.electricityUnitPrice" :min="0" :precision="2" class="u-w-full" /></el-form-item>
        </template>
        <template v-else>
          <el-row :gutter="12">
            <el-col :span="12"><el-form-item label="电费峰价(元/度)"><el-input-number v-model="form.electricityPeakPrice" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="电费谷价(元/度)"><el-input-number v-model="form.electricityValleyPrice" :min="0" :precision="2" class="u-w-full" /></el-form-item></el-col>
          </el-row>
        </template>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submit">提交（待审核）</el-button>
        <el-button @click="uploadVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getOwnerHouses, createOwnerHouse, updateOwnerHouseStatus, uploadOwnerHouseImage } from '../../api'
import { PAYMENT_METHOD_OPTIONS, calcDeposit, calcFirstPayment } from '../../utils/payment'
import { houseStatusLabel, houseStatusType, reviewStatusLabel, reviewStatusType } from '../../utils/dict'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])

const houseTitle = (row) =>
  [row.communityName, row.buildingNo && `${row.buildingNo}号楼`, row.roomNo && `${row.roomNo}室`]
    .filter(Boolean)
    .join(' ')

// 状态与审核都是枚举，进 tags
const tagsOf = (row) => [
  { text: houseStatusLabel(row.status), type: houseStatusType(row.status) },
  { text: `审核${reviewStatusLabel(row.reviewStatus)}`, type: reviewStatusType(row.reviewStatus) }
]

const fieldsOf = (row) => [
  { label: '户型', value: row.layout },
  { label: '面积', value: row.squareArea != null ? `${row.squareArea}㎡` : '-' },
  { label: '月租金', value: `¥${row.monthlyRent ?? 0}`, type: 'amount' },
  { label: '押金', value: `¥${row.deposit ?? 0}` },
  { label: '付款方式', value: row.paymentMethod },
  // 审核驳回原因只有被驳回时才有，用 hidden 控制
  { label: '驳回原因', value: row.reviewReason, span: 2, clamp: 2, hidden: !row.reviewReason }
]

// 付款方式的 9 种组合与「押金 = 押数 × 月租金」的规则统一放在 utils/payment.js，
// 与租客端共用一份，避免两边各写一套算错
const PAYMENT_METHODS = PAYMENT_METHOD_OPTIONS

const getList = async () => {
  loading.value = true
  try {
    const data = await getOwnerHouses({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const form = reactive({
  communityName: '',
  area: '',
  buildingNo: '',
  roomNo: '',
  layout: '',
  squareArea: undefined,
  orientation: '',
  decoration: '精装',
  monthlyRent: undefined,
  // deposit 不在表单里手填，由下面的 depositAmount 计算后写回
  deposit: undefined,
  paymentMethod: '押一付一',
  facilities: '',
  description: '',
  waterBillType: 0,
  waterUnitPrice: undefined,
  waterTier1Limit: undefined,
  waterTier1Price: undefined,
  waterTier2Limit: undefined,
  waterTier2Price: undefined,
  waterTier3Price: undefined,
  electricityBillType: 1,
  electricityUnitPrice: undefined,
  electricityPeakPrice: undefined,
  electricityValleyPrice: undefined,
  // 图片在 rental_house_image 里一图一行，后端 createHouseByOwner 负责拆行写入
  realityImages: [],
  layoutImages: []
})

const uploadVisible = ref(false)

const selectedPayment = computed(
  () => PAYMENT_METHODS.find((p) => p.label === form.paymentMethod) ?? PAYMENT_METHODS[0]
)
const depositAmount = computed(() => calcDeposit(form.monthlyRent, form.paymentMethod))
// 首期要交的钱 = 押金 + 付数个月的租金，给业主一个直观的预览
const firstPaymentAmount = computed(
  () => calcFirstPayment(form.monthlyRent, depositAmount.value, form.paymentMethod)
)
// 月租金或付款方式一变就重算押金写回表单：后端只认 deposit，不认「押几」
watch(depositAmount, (value) => {
  form.deposit = value
}, { immediate: true })

// el-upload 需要 { name, url } 形态的列表；表单里只存 URL 字符串数组
const toFileList = (urls) => (urls || []).map((url, index) => ({ name: `图片${index + 1}`, url }))
const realityFileList = computed(() => toFileList(form.realityImages))
const layoutFileList = computed(() => toFileList(form.layoutImages))

// 必须用 http-request 覆盖 el-upload 的内建 XHR：那个 XHR 不带 Authorization，
// 打我们的上传端点会因为没有 token 而失败。
const doUpload = async (option, field) => {
  try {
    const url = await uploadOwnerHouseImage(option.file)
    form[field] = [...(form[field] || []), url]
    option.onSuccess(url)
  } catch (error) {
    option.onError(error)
  }
}

const removeImage = (file, field) => {
  form[field] = (form[field] || []).filter((url) => url !== file.url)
}

const toggleOnline = async (row) => {
  const online = row.status !== 1
  await updateOwnerHouseStatus(row.id, online)
  ElMessage.success(online ? '房源已上架' : '房源已下架')
  getList()
}

const openUpload = () => {
  Object.assign(form, {
    communityName: '',
    area: '',
    buildingNo: '',
    roomNo: '',
    layout: '',
    squareArea: undefined,
    orientation: '',
    decoration: '精装',
    monthlyRent: undefined,
    deposit: undefined,
    paymentMethod: '押一付一',
    facilities: '',
    description: '',
    waterBillType: 0,
    waterUnitPrice: undefined,
    waterTier1Limit: undefined,
    waterTier1Price: undefined,
    waterTier2Limit: undefined,
    waterTier2Price: undefined,
    waterTier3Price: undefined,
    electricityBillType: 1,
    electricityUnitPrice: undefined,
    electricityPeakPrice: undefined,
    electricityValleyPrice: undefined,
    realityImages: [],
    layoutImages: []
  })
  uploadVisible.value = true
}

const submit = async () => {
  if (!form.communityName || !form.area || !form.buildingNo || !form.roomNo || !form.layout || form.squareArea == null || form.monthlyRent == null) {
    ElMessage.warning('请填写完整的基本信息')
    return
  }
  await createOwnerHouse(form)
  ElMessage.success('提交成功，等待管理员审核')
  uploadVisible.value = false
  getList()
}

onMounted(getList)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.w-full {
  width: 100%;
}
.tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
</style>
