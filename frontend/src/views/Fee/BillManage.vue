<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Money, WarningFilled } from '@element-plus/icons-vue'
import {
  pageFeeConfigs, createFeeConfig, updateFeeConfig, deleteFeeConfig,
  pageBills, generateBills, getBillDetail, payBill, cancelBill, listArrears,
} from '@/api/fee'
import { pageResidents } from '@/api/resident'

const loading = ref(false)
const activeTab = ref('bills')

// 费用配置
const configs = ref([])
const configDialog = ref(false)
const configFormRef = ref(null)
const configForm = reactive({ id: null, feeName: '', defaultAmount: null, chargeUnit: 'MONTHLY', description: '', sortOrder: 0 })
const isConfigEdit = ref(false)

const configRules = {
  feeName: [{ required: true, message: '请输入费用名称', trigger: 'blur' }],
  defaultAmount: [{ required: true, message: '请输入默认金额', trigger: 'blur' }],
  chargeUnit: [{ required: true, message: '请选择计费单位', trigger: 'change' }],
}

// 账单
const bills = ref([])
const residents = ref([])
const billPagination = reactive({ page: 1, size: 10, total: 0 })
const billQuery = reactive({ residentId: null, billPeriod: '', status: '' })
const generateForm = reactive({ billPeriod: '', residentId: null })

// 缴费
const payDialog = ref(false)
const payForm = reactive({ billId: null, amount: null, paymentMethod: 'CASH', remark: '' })

// 详情
const detailDialog = ref(false)
const detail = ref(null)

// 欠费
const arrearsList = ref([])
const arrearsDialog = ref(false)

const chargeUnitMap = { DAILY: '按天', MONTHLY: '按月', ONE_TIME: '一次性' }
const statusMap = { UNPAID: '未支付', PARTIAL: '部分支付', PAID: '已付清', CANCELLED: '已作废' }
const statusTagType = s => (s === 'PAID' ? 'success' : s === 'CANCELLED' ? 'info' : s === 'PARTIAL' ? 'warning' : 'danger')
const payMethodMap = { CASH: '现金', WECHAT: '微信', ALIPAY: '支付宝', BANK_TRANSFER: '银行转账' }

const fetchConfigs = async () => {
  const res = await pageFeeConfigs({ page: 1, size: 50 })
  configs.value = res.records || []
}

const fetchBills = async () => {
  loading.value = true
  try {
    const params = { page: billPagination.page, size: billPagination.size }
    for (const [k, v] of Object.entries(billQuery)) {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    }
    const res = await pageBills(params)
    bills.value = res.records || []
    billPagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

// 费用配置操作
const showConfigDialog = (row = null) => {
  isConfigEdit.value = !!row
  Object.assign(configForm, row || { id: null, feeName: '', defaultAmount: null, chargeUnit: 'MONTHLY', description: '', sortOrder: 0 })
  configDialog.value = true
}

const handleConfigSave = async () => {
  const valid = await configFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isConfigEdit.value) {
      await updateFeeConfig(configForm.id, configForm)
      ElMessage.success('编辑成功')
    } else {
      await createFeeConfig(configForm)
      ElMessage.success('新增成功')
    }
    configDialog.value = false
    fetchConfigs()
  } catch {
    // 错误已由拦截器统一提示
  }
}

const handleConfigDelete = async row => {
  await ElMessageBox.confirm('确定删除该费用配置吗？', '确认删除', { type: 'warning' })
  await deleteFeeConfig(row.id)
  ElMessage.success('已删除')
  fetchConfigs()
}

// 账单生成
const handleGenerate = async () => {
  if (!generateForm.billPeriod) {
    ElMessage.warning('请选择账单月份')
    return
  }
  await ElMessageBox.confirm(`确认生成 ${generateForm.billPeriod} 的账单吗？`, '确认生成', { type: 'info' })
  const count = await generateBills(generateForm)
  ElMessage.success(`成功生成 ${count} 条账单`)
  fetchBills()
}

// 缴费
const showPayDialog = row => {
  payForm.billId = row.id
  payForm.amount = row.unpaidAmount
  payForm.paymentMethod = 'CASH'
  payForm.remark = ''
  payDialog.value = true
}

const handlePay = async () => {
  if (!payForm.amount || payForm.amount <= 0) {
    ElMessage.warning('请输入有效支付金额')
    return
  }
  await ElMessageBox.confirm(
    `确认收款 ¥${payForm.amount}？`,
    '确认缴费',
    { type: 'info', confirmButtonText: '确认收款', cancelButtonText: '取消' },
  )
  try {
    await payBill(payForm.billId, { amount: payForm.amount, paymentMethod: payForm.paymentMethod, remark: payForm.remark })
    ElMessage.success('缴费成功')
    payDialog.value = false
    fetchBills()
  } catch { /* 错误已由拦截器统一提示 */ }
}

const handleCancel = async row => {
  await ElMessageBox.confirm('确定作废该账单吗？（仅未支付且无缴费记录的可作废）', '确认作废', { type: 'warning' })
  await cancelBill(row.id)
  ElMessage.success('已作废')
  fetchBills()
}

// 详情
const showDetail = async row => {
  detail.value = await getBillDetail(row.id)
  detailDialog.value = true
}

// 欠费
const showArrears = async () => {
  arrearsList.value = await listArrears()
  arrearsDialog.value = true
}

const now = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
})

onMounted(async () => {
  const res = await pageResidents({ size: 999 })
  residents.value = res.records || []
  fetchConfigs()
  fetchBills()
})
</script>

<template>
  <div class="fee-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="账单管理" name="bills">
        <div class="search-bar">
          <el-form :model="generateForm" inline>
            <el-form-item label="生成月份">
              <el-date-picker v-model="generateForm.billPeriod" type="month" placeholder="选择月份" value-format="YYYY-MM" />
            </el-form-item>
            <el-form-item label="指定老人">
              <el-select v-model="generateForm.residentId" placeholder="全部" clearable filterable class="search-input">
                <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="success" :icon="Money" @click="handleGenerate">生成账单</el-button>
              <el-button type="danger" plain :icon="WarningFilled" @click="showArrears">欠费统计</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="search-bar" style="margin-top: 12px">
          <el-form :model="billQuery" inline>
            <el-form-item label="老人">
              <el-select v-model="billQuery.residentId" placeholder="全部" clearable filterable class="search-input">
                <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="billQuery.status" placeholder="全部" clearable class="search-select">
                <el-option label="未支付" value="UNPAID" />
                <el-option label="部分支付" value="PARTIAL" />
                <el-option label="已付清" value="PAID" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="fetchBills">查询</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card" style="margin-top: 16px">
          <el-table :data="bills" v-loading="loading" border stripe>
            <template #empty><el-empty description="暂无账单" /></template>
            <el-table-column prop="residentName" label="老人" min-width="90" />
            <el-table-column prop="billPeriod" label="账单周期" width="100" />
            <el-table-column label="总金额" width="100">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column label="已付" width="90">
              <template #default="{ row }">¥{{ row.paidAmount }}</template>
            </el-table-column>
            <el-table-column label="待付" width="100">
              <template #default="{ row }">
                <span :style="{ color: row.unpaidAmount > 0 ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
                  ¥{{ row.unpaidAmount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ statusMap[row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="showDetail(row)">详情</el-button>
                <el-button v-if="row.status !== 'PAID' && row.status !== 'CANCELLED'" size="small" link type="success" @click="showPayDialog(row)">缴费</el-button>
                <el-button v-if="row.status === 'UNPAID'" size="small" link type="danger" @click="handleCancel(row)">作废</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="billPagination.page" v-model:page-size="billPagination.size"
              :total="billPagination.total" :page-sizes="[10, 20, 50]"
              @current-change="fetchBills" @size-change="fetchBills"
              layout="total, sizes, prev, pager, next" background
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="费用配置" name="configs">
        <div class="search-bar">
          <el-button type="primary" :icon="Plus" @click="showConfigDialog()">新增费用</el-button>
        </div>
        <div class="table-card" style="margin-top: 16px">
          <el-table :data="configs" border stripe>
            <template #empty><el-empty description="暂无费用配置" /></template>
            <el-table-column prop="feeName" label="费用名称" min-width="120" />
            <el-table-column label="默认金额" width="100">
              <template #default="{ row }">¥{{ row.defaultAmount }}</template>
            </el-table-column>
            <el-table-column label="计费单位" width="90">
              <template #default="{ row }">{{ chargeUnitMap[row.chargeUnit] || row.chargeUnit }}</template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="150" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button size="small" :icon="Edit" link type="primary" @click="showConfigDialog(row)">编辑</el-button>
                <el-button size="small" :icon="Delete" link type="danger" @click="handleConfigDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 费用配置抽屉 -->
    <el-drawer v-model="configDialog" :title="isConfigEdit ? '编辑费用' : '新增费用'" size="450px" destroy-on-close>
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-width="90px">
        <el-form-item label="费用名称" prop="feeName">
          <el-input v-model="configForm.feeName" placeholder="如：床位费" />
        </el-form-item>
        <el-form-item label="默认金额" prop="defaultAmount">
          <el-input-number v-model="configForm.defaultAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计费单位" prop="chargeUnit">
          <el-select v-model="configForm.chargeUnit" style="width: 100%">
            <el-option label="按天 (DAILY)" value="DAILY" />
            <el-option label="按月 (MONTHLY)" value="MONTHLY" />
            <el-option label="一次性 (ONE_TIME)" value="ONE_TIME" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="configForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="configForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="configDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfigSave">保存</el-button>
      </div>
    </el-drawer>

    <!-- 缴费弹窗 -->
    <el-dialog v-model="payDialog" title="缴费" width="400px" destroy-on-close>
      <el-form :model="payForm" label-width="80px">
        <el-form-item label="支付金额">
          <el-input-number v-model="payForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.paymentMethod" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="payForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePay">确认缴费</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog" title="账单详情" width="650px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人">{{ detail.residentName }}</el-descriptions-item>
          <el-descriptions-item label="账单周期">{{ detail.billPeriod }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ detail.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="已付金额">¥{{ detail.paidAmount }}</el-descriptions-item>
          <el-descriptions-item label="待付金额">
            <span :style="{ color: '#f56c6c', fontWeight: 600 }">¥{{ detail.unpaidAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ statusMap[detail.status] }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">费用明细</h4>
        <el-table :data="detail.items" border size="small">
          <el-table-column prop="feeName" label="费用名称" />
          <el-table-column label="金额" width="100">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
        </el-table>
        <h4 style="margin: 16px 0 8px">缴费记录</h4>
        <el-table :data="detail.payments" border size="small">
          <template #empty><el-empty description="暂无缴费记录" :image-size="40" /></template>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column label="方式" width="80">
            <template #default="{ row }">{{ payMethodMap[row.paymentMethod] || row.paymentMethod }}</template>
          </el-table-column>
          <el-table-column prop="paidAt" label="时间" width="150" />
          <el-table-column prop="remark" label="备注" />
        </el-table>
      </template>
    </el-dialog>

    <!-- 欠费统计弹窗 -->
    <el-dialog v-model="arrearsDialog" title="欠费统计" width="550px" destroy-on-close>
      <el-table :data="arrearsList" border stripe>
        <template #empty><el-empty description="暂无欠费" /></template>
        <el-table-column prop="residentName" label="老人" width="90" />
        <el-table-column label="欠费总额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">¥{{ row.totalArrears }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unpaidBillCount" label="未付账单数" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.fee-page { animation: fadeIn 0.35s ease; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.search-bar { background: #fff; padding: 16px 20px 0; border-radius: 10px; border: 1px solid #ebeef5; .search-input { width: 150px; } .search-select { width: 110px; } }
.table-card { background: #fff; padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; margin-top: 24px; border-top: 1px solid #ebeef5; }
</style>
