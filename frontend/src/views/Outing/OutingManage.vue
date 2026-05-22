<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { pageOutings, createOuting, returnOuting, cancelOuting, listOverdue } from '@/api/outing'
import { pageResidents } from '@/api/resident'

const loading = ref(false)
const tableData = ref([])
const overdueList = ref([])
const residents = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const query = reactive({ residentId: null, status: '', startDate: '', endDate: '' })

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive(createEmptyForm())

const overdueDialogVisible = ref(false)

function createEmptyForm() {
  return {
    id: null,
    residentId: null,
    outTime: '',
    expectedReturnTime: '',
    destination: '',
    companion: '',
    reason: '',
    notes: '',
  }
}

const rules = {
  residentId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  outTime: [{ required: true, message: '请选择外出时间', trigger: 'change' }],
  expectedReturnTime: [{ required: true, message: '请选择预计返回时间', trigger: 'change' }],
}

const statusMap = { OUT: '外出中', RETURNED: '已返回' }
const statusTagType = s => (s === 'OUT' ? 'warning' : 'success')

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    for (const [k, v] of Object.entries(query)) {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    }
    const res = await pageOutings(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const showDialog = () => {
  Object.assign(form, createEmptyForm())
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await createOuting(form)
  ElMessage.success('登记成功')
  dialogVisible.value = false
  fetchData()
}

const handleReturn = async row => {
  await ElMessageBox.confirm('确认该老人已返回吗？', '确认返回', { type: 'info' })
  await returnOuting(row.id)
  ElMessage.success('已登记返回')
  fetchData()
}

const handleCancel = async row => {
  await ElMessageBox.confirm('确定取消该外出记录吗？', '确认取消', { type: 'warning' })
  await cancelOuting(row.id)
  ElMessage.success('已取消')
  fetchData()
}

const showOverdue = async () => {
  overdueList.value = await listOverdue()
  overdueDialogVisible.value = true
}

const formatOverdue = minutes => {
  if (minutes == null) return ''
  if (minutes < 60) return `${minutes}分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}小时${m}分钟` : `${h}小时`
}

const rowClassName = ({ row }) => (row.isOverdue ? 'row-overdue' : '')

onMounted(async () => {
  const res = await pageResidents({ size: 999 })
  residents.value = res.records || []
  fetchData()
})
</script>

<template>
  <div class="outing-page">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="老人">
          <el-select v-model="query.residentId" placeholder="全部" clearable filterable class="search-input">
            <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable class="search-select">
            <el-option label="外出中" value="OUT" />
            <el-option label="已返回" value="RETURNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="query.startDate" type="date" placeholder="开始" class="search-date" />
        </el-form-item>
        <el-form-item label="至">
          <el-date-picker v-model="query.endDate" type="date" placeholder="结束" class="search-date" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button type="success" :icon="Plus" @click="showDialog">新增外出</el-button>
          <el-button type="danger" plain @click="showOverdue">超时未归</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe :row-class-name="rowClassName">
        <template #empty>
          <el-empty description="暂无外出记录" />
        </template>
        <el-table-column prop="residentName" label="老人" min-width="90" />
        <el-table-column prop="outTime" label="外出时间" min-width="150" />
        <el-table-column prop="expectedReturnTime" label="预计返回" min-width="150" />
        <el-table-column prop="actualReturnTime" label="实际返回" min-width="150">
          <template #default="{ row }">{{ row.actualReturnTime ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="destination" label="目的地" min-width="120" />
        <el-table-column prop="companion" label="陪同人" min-width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isOverdue ? 'danger' : statusTagType(row.status)" size="small">
              {{ row.isOverdue ? `超时${formatOverdue(row.overdueMinutes)}` : statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'OUT'" size="small" type="success" link @click="handleReturn(row)">
              登记返回
            </el-button>
            <el-button v-if="row.status === 'OUT'" size="small" :icon="Delete" link type="danger" @click="handleCancel(row)">
              取消
            </el-button>
            <span v-else style="color: #909399; font-size: 13px">已完成</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          @current-change="fetchData"
          @size-change="fetchData"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </div>

    <!-- 新增外出抽屉 -->
    <el-drawer v-model="dialogVisible" title="新增外出登记" size="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="老人" prop="residentId">
          <el-select v-model="form.residentId" placeholder="请选择老人" filterable style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="外出时间" prop="outTime">
          <el-date-picker v-model="form.outTime" type="datetime" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计返回" prop="expectedReturnTime">
          <el-date-picker v-model="form.expectedReturnTime" type="datetime" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="目的地">
          <el-input v-model="form.destination" placeholder="外出目的地" />
        </el-form-item>
        <el-form-item label="陪同人">
          <el-input v-model="form.companion" placeholder="陪同人" />
        </el-form-item>
        <el-form-item label="外出原因">
          <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="外出原因" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="其他说明" />
        </el-form-item>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </el-drawer>

    <!-- 超时未归弹窗 -->
    <el-dialog v-model="overdueDialogVisible" title="超时未归老人" width="700px" destroy-on-close>
      <el-table :data="overdueList" border stripe>
        <template #empty>
          <el-empty description="暂无超时未归老人" />
        </template>
        <el-table-column prop="residentName" label="老人" width="90" />
        <el-table-column prop="expectedReturnTime" label="预计返回" width="150" />
        <el-table-column label="超时时长" width="120">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ formatOverdue(row.overdueMinutes) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="destination" label="目的地" />
        <el-table-column prop="companion" label="陪同人" width="80" />
      </el-table>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.outing-page {
  animation: fadeIn 0.35s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.search-bar {
  background: #fff;
  padding: 16px 20px 0;
  border-radius: 10px;
  border: 1px solid #ebeef5;

  .search-input { width: 150px; }
  .search-select { width: 100px; }
  .search-date { width: 130px; }
}

.table-card {
  margin-top: 16px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 10px;
  border: 1px solid #ebeef5;

  :deep(.row-overdue) {
    background-color: #fef0f0;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  margin-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>
