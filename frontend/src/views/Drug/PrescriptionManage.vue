<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Check, Close } from '@element-plus/icons-vue'
import {
  pagePrescriptions, createPrescription, updatePrescription,
  pageDrugDicts, pageDrugRecords, listPending, createDrugRecord,
} from '@/api/drug'
import { pageResidents } from '@/api/resident'

const loading = ref(false)
const prescriptions = ref([])
const pendingList = ref([])
const residents = ref([])
const drugs = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const activeTab = ref('pending')

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, residentId: null, drugId: null, dosage: '', frequency: '', dosesPerDay: 1, startDate: '', endDate: '', notes: '' })

const rules = {
  residentId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
}

const fetchPrescriptions = async () => {
  loading.value = true
  try {
    const res = await pagePrescriptions({ page: pagination.page, size: pagination.size })
    prescriptions.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const fetchPending = async () => {
  pendingList.value = await listPending()
}

const showDialog = () => {
  Object.assign(form, { id: null, residentId: null, drugId: null, dosage: '', frequency: '', dosesPerDay: 1, startDate: '', endDate: '', notes: '' })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await createPrescription(form)
    ElMessage.success('处方创建成功')
    dialogVisible.value = false
    fetchPrescriptions()
    fetchPending()
  } catch { /* 错误已由拦截器统一提示 */ }
}

const handleStop = async row => {
  await ElMessageBox.confirm('确定停用该处方吗？', '确认停用', { type: 'warning' })
  try {
    await updatePrescription(row.id, { ...row, status: 'STOPPED' })
    ElMessage.success('已停用')
    fetchPrescriptions()
    fetchPending()
  } catch { /* 错误已由拦截器统一提示 */ }
}

const handleTakeDrug = async item => {
  await ElMessageBox.confirm(
    `确认 ${item.residentName} 已服用「${item.drugName}」${item.dosage || ''}？`,
    '确认服药',
    { type: 'info', confirmButtonText: '确认服药', cancelButtonText: '取消' },
  )
  await createDrugRecord({ prescriptionId: item.prescriptionId, status: 'TAKEN' })
  ElMessage.success('已记录服药')
  fetchPending()
}

const handleMissDrug = async item => {
  await ElMessageBox.confirm(
    `确认 ${item.residentName} 漏服「${item.drugName}」？`,
    '确认漏服',
    { type: 'warning', confirmButtonText: '确认漏服', cancelButtonText: '取消' },
  )
  try {
    await createDrugRecord({ prescriptionId: item.prescriptionId, status: 'MISSED' })
    ElMessage.success('已记录漏服')
    fetchPending()
  } catch { /* 错误已由拦截器统一提示 */ }
}

const handleRefuseDrug = async item => {
  await ElMessageBox.confirm(
    `确认 ${item.residentName} 拒服「${item.drugName}」？`,
    '确认拒服',
    { type: 'warning', confirmButtonText: '确认拒服', cancelButtonText: '取消' },
  )
  try {
    await createDrugRecord({ prescriptionId: item.prescriptionId, status: 'REFUSED' })
    ElMessage.success('已记录拒服')
    fetchPending()
  } catch { /* 错误已由拦截器统一提示 */ }
}

const statusMap = { ACTIVE: '用药中', STOPPED: '已停用' }

onMounted(async () => {
  const [rRes, dRes] = await Promise.all([
    pageResidents({ size: 999 }),
    pageDrugDicts({ size: 999 }),
  ])
  residents.value = rRes.records || []
  drugs.value = dRes.records || []
  fetchPrescriptions()
  fetchPending()
})
</script>

<template>
  <div class="prescription-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="今日待服药" name="pending">
        <div class="table-card">
          <el-table :data="pendingList" border stripe>
            <template #empty><el-empty description="今日暂无待服药任务" /></template>
            <el-table-column prop="residentName" label="老人" width="80" />
            <el-table-column prop="drugName" label="药品" width="120" />
            <el-table-column prop="dosage" label="用法用量" min-width="120" />
            <el-table-column label="进度" width="120">
              <template #default="{ row }">
                <span :style="{ color: row.pendingCount > 0 ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
                  已服 {{ row.takenCount }} / {{ row.dosesPerDay }} 次
                </span>
                <el-tag v-if="row.pendingCount === 0" type="success" size="small" style="margin-left: 6px">已完成</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.pendingCount > 0" size="small" :icon="Check" type="success" @click="handleTakeDrug(row)">服药</el-button>
                <el-button v-if="row.pendingCount > 0" size="small" :icon="Close" type="warning" plain @click="handleMissDrug(row)">漏服</el-button>
                <el-button v-if="row.pendingCount > 0" size="small" type="danger" plain @click="handleRefuseDrug(row)">拒服</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="处方列表" name="prescriptions">
        <div class="search-bar">
          <el-button type="primary" :icon="Plus" @click="showDialog">新增处方</el-button>
        </div>
        <div class="table-card" style="margin-top: 16px">
          <el-table :data="prescriptions" v-loading="loading" border stripe>
            <template #empty><el-empty description="暂无处方" /></template>
            <el-table-column prop="residentName" label="老人" width="80" />
            <el-table-column prop="drugName" label="药品" width="120" />
            <el-table-column prop="dosage" label="用法用量" width="100" />
            <el-table-column prop="frequency" label="频次" width="120" />
            <el-table-column prop="dosesPerDay" label="次/天" width="70" />
            <el-table-column prop="startDate" label="开始" width="100" />
            <el-table-column prop="endDate" label="结束" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ statusMap[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'ACTIVE'" size="small" link type="warning" @click="handleStop(row)">停用</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="pagination.page" v-model:page-size="pagination.size"
              :total="pagination.total" :page-sizes="[10, 20, 50]"
              @current-change="fetchPrescriptions" @size-change="fetchPrescriptions"
              layout="total, sizes, prev, pager, next" background
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增处方抽屉 -->
    <el-drawer v-model="dialogVisible" title="新增处方" size="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="老人" prop="residentId">
          <el-select v-model="form.residentId" placeholder="请选择老人" filterable style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="form.drugId" placeholder="请选择药品" filterable style="width: 100%">
            <el-option v-for="d in drugs" :key="d.id" :label="d.drugName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用法用量">
          <el-input v-model="form.dosage" placeholder="如：1片/次" />
        </el-form-item>
        <el-form-item label="频次说明">
          <el-input v-model="form.frequency" placeholder="如：一日3次 饭后" />
        </el-form-item>
        <el-form-item label="每日次数">
          <el-input-number v-model="form.dosesPerDay" :min="1" :max="10" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.endDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.prescription-page { animation: fadeIn 0.35s ease; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.search-bar { background: #fff; padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5; }
.table-card { background: #fff; padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; margin-top: 24px; border-top: 1px solid #ebeef5; }
</style>
