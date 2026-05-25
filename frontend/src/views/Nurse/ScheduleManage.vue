<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { pageSchedules, createSchedule, updateSchedule, deleteSchedule, pageNurses, pageHandovers, createHandover } from '@/api/nurse'

const loading = ref(false)
const schedules = ref([])
const nurses = ref([])
const query = reactive({ page: 1, size: 50 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, nurseId: null, shiftDate: '', shiftType: '', notes: '' })

const handoverDialog = ref(false)
const handoverFormRef = ref(null)
const handoverForm = reactive({ scheduleId: null, toNurseId: null, shiftDate: '', keyNotes: '', residentNotes: '', materialCheck: '' })

const rules = {
  nurseId: [{ required: true, message: '请选择护工', trigger: 'change' }],
  shiftDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  shiftType: [{ required: true, message: '请选择班次', trigger: 'change' }],
}
const handoverRules = {
  toNurseId: [{ required: true, message: '请选择接班人', trigger: 'change' }],
}

const shiftTypeMap = { MORNING: '早班', AFTERNOON: '中班', NIGHT: '夜班' }
const shiftTypeTag = t => (t === 'MORNING' ? '' : t === 'AFTERNOON' ? 'warning' : '')
const statusMap = { SCHEDULED: '已排班', COMPLETED: '已完成', ABSENT: '缺勤' }
const statusTagType = s => (s === 'SCHEDULED' ? 'primary' : s === 'COMPLETED' ? 'success' : 'danger')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageSchedules(query)
    schedules.value = res.records || []
  } finally {
    loading.value = false
  }
}

const showDialog = (row = null) => {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : { id: null, nurseId: null, shiftDate: '', shiftType: '', notes: '' })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateSchedule(form.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createSchedule(form)
      ElMessage.success('排班成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // 冲突错误由拦截器统一提示
  }
}

const handleDelete = async row => {
  await ElMessageBox.confirm('确定删除该排班吗？', '确认删除', { type: 'warning' })
  await deleteSchedule(row.id)
  ElMessage.success('已删除')
  fetchData()
}

const handleMarkAbsent = async row => {
  await ElMessageBox.confirm('确认该护工缺勤吗？', '确认缺勤', { type: 'warning' })
  await updateSchedule(row.id, { ...row, status: 'ABSENT' })
  ElMessage.success('已标记缺勤')
  fetchData()
}

const showHandover = row => {
  Object.assign(handoverForm, { scheduleId: row.id, toNurseId: null, shiftDate: row.shiftDate, keyNotes: '', residentNotes: '', materialCheck: '' })
  handoverDialog.value = true
}

const handleHandoverSave = async () => {
  const valid = await handoverFormRef.value.validate().catch(() => false)
  if (!valid) return
  await createHandover(handoverForm)
  ElMessage.success('交接记录已创建')
  handoverDialog.value = false
  fetchData()
}

// 按日期+班次生成二维矩阵
const scheduleMatrix = ref([])
const buildMatrix = () => {
  const dayMap = {}
  schedules.value.forEach(s => {
    const key = s.shiftDate
    if (!dayMap[key]) dayMap[key] = { date: key, MORNING: null, AFTERNOON: null, NIGHT: null }
    dayMap[key][s.shiftType] = s
  })
  scheduleMatrix.value = Object.values(dayMap).sort((a, b) => a.date.localeCompare(b.date))
}

onMounted(async () => {
  const nRes = await pageNurses({ size: 999 })
  nurses.value = nRes.records || []
  await fetchData()
  buildMatrix()
})

watch(schedules, () => buildMatrix(), { deep: true })
</script>

<template>
  <div class="schedule-page">
    <div class="search-bar">
      <el-button type="primary" :icon="Plus" @click="showDialog()">新增排班</el-button>
      <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
    </div>

    <div class="table-card">
      <el-table :data="scheduleMatrix" v-loading="loading" border stripe>
        <template #empty><el-empty description="暂无排班" /></template>
        <el-table-column prop="date" label="日期" width="120" fixed="left" />
        <el-table-column label="早班 (08:00-16:00)" min-width="180">
          <template #default="{ row }">
            <template v-if="row.MORNING">
              <el-tag type="primary" size="small" style="margin-right: 6px">{{ row.MORNING.nurseName }}</el-tag>
              <span v-if="row.MORNING.status === 'ABSENT'" style="color: #f56c6c; font-size: 12px">缺勤</span>
              <div style="margin-top: 4px">
                <el-button size="small" link type="primary" @click="showHandover(row.MORNING)">交接</el-button>
                <el-button size="small" link type="warning" @click="handleMarkAbsent(row.MORNING)">缺勤</el-button>
                <el-button size="small" link type="danger" @click="handleDelete(row.MORNING)">删</el-button>
              </div>
            </template>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="中班 (16:00-00:00)" min-width="180">
          <template #default="{ row }">
            <template v-if="row.AFTERNOON">
              <el-tag type="warning" size="small" style="margin-right: 6px">{{ row.AFTERNOON.nurseName }}</el-tag>
              <span v-if="row.AFTERNOON.status === 'ABSENT'" style="color: #f56c6c; font-size: 12px">缺勤</span>
              <div style="margin-top: 4px">
                <el-button size="small" link type="primary" @click="showHandover(row.AFTERNOON)">交接</el-button>
                <el-button size="small" link type="warning" @click="handleMarkAbsent(row.AFTERNOON)">缺勤</el-button>
                <el-button size="small" link type="danger" @click="handleDelete(row.AFTERNOON)">删</el-button>
              </div>
            </template>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="夜班 (00:00-08:00)" min-width="180">
          <template #default="{ row }">
            <template v-if="row.NIGHT">
              <el-tag type="" size="small" style="margin-right: 6px">{{ row.NIGHT.nurseName }}</el-tag>
              <span v-if="row.NIGHT.status === 'ABSENT'" style="color: #f56c6c; font-size: 12px">缺勤</span>
              <div style="margin-top: 4px">
                <el-button size="small" link type="primary" @click="showHandover(row.NIGHT)">交接</el-button>
                <el-button size="small" link type="warning" @click="handleMarkAbsent(row.NIGHT)">缺勤</el-button>
                <el-button size="small" link type="danger" @click="handleDelete(row.NIGHT)">删</el-button>
              </div>
            </template>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 排班抽屉 -->
    <el-drawer v-model="dialogVisible" :title="isEdit ? '编辑排班' : '新增排班'" size="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="护工" prop="nurseId">
          <el-select v-model="form.nurseId" placeholder="请选择护工" filterable style="width: 100%">
            <el-option v-for="n in nurses" :key="n.id" :label="n.nurseName" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="shiftDate">
          <el-date-picker v-model="form.shiftDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <el-select v-model="form.shiftType" placeholder="请选择班次" style="width: 100%">
            <el-option label="早班 (08:00-16:00)" value="MORNING" />
            <el-option label="中班 (16:00-00:00)" value="AFTERNOON" />
            <el-option label="夜班 (00:00-08:00)" value="NIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </el-drawer>

    <!-- 交接班抽屉 -->
    <el-drawer v-model="handoverDialog" title="交接班登记" size="500px" destroy-on-close>
      <el-form ref="handoverFormRef" :model="handoverForm" :rules="handoverRules" label-width="100px">
        <el-form-item label="接班人" prop="toNurseId">
          <el-select v-model="handoverForm.toNurseId" placeholder="请选择接班人" filterable style="width: 100%">
            <el-option v-for="n in nurses" :key="n.id" :label="n.nurseName" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="重点事项">
          <el-input v-model="handoverForm.keyNotes" type="textarea" :rows="3" placeholder="需要接班人关注的重点事项" />
        </el-form-item>
        <el-form-item label="老人情况">
          <el-input v-model="handoverForm.residentNotes" type="textarea" :rows="3" placeholder="各老人身体/情绪状况" />
        </el-form-item>
        <el-form-item label="物资清点">
          <el-input v-model="handoverForm.materialCheck" type="textarea" :rows="2" placeholder="药品/器械/钥匙等物资清点" />
        </el-form-item>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="handoverDialog = false">取消</el-button>
        <el-button type="primary" @click="handleHandoverSave">确认交接</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.schedule-page { animation: fadeIn 0.35s ease; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.search-bar {
  background: #fff; padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5;
  display: flex; gap: 12px;
}
.table-card {
  margin-top: 16px; background: #fff; padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5;
}
.drawer-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; margin-top: 24px; border-top: 1px solid #ebeef5; }
</style>
