<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { pageHealthRecords, createHealthRecord, updateHealthRecord, deleteHealthRecord } from '@/api/health'
import { pageResidents } from '@/api/resident'

const loading = ref(false)
const tableData = ref([])
const residents = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const query = reactive({ residentId: null, status: '', startDate: '', endDate: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive(createEmptyForm())

function createEmptyForm() {
  return {
    id: null,
    residentId: null,
    temperature: null,
    bloodSystolic: null,
    bloodDiastolic: null,
    heartRate: null,
    bloodSugar: null,
    weight: null,
    oxygen: null,
    status: 'NORMAL',
    abnormal: '',
    notes: '',
    recordedAt: '',
  }
}

const rules = {
  residentId: [{ required: true, message: '请选择老人', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    for (const [k, v] of Object.entries(query)) {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    }
    const res = await pageHealthRecords(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const showDialog = (row = null) => {
  isEdit.value = !!row
  Object.assign(form, row || createEmptyForm())
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateHealthRecord(form.id, form)
    ElMessage.success('编辑成功')
  } else {
    await createHealthRecord(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async row => {
  await ElMessageBox.confirm('确定删除该健康记录吗？', '确认删除', { type: 'warning' })
  await deleteHealthRecord(row.id)
  ElMessage.success('已删除')
  fetchData()
}

// 异常行高亮
const rowClassName = ({ row }) => (row.status === 'ABNORMAL' ? 'row-abnormal' : '')

onMounted(async () => {
  const res = await pageResidents({ size: 999 })
  residents.value = res.records || []
  fetchData()
})
</script>

<template>
  <div class="health-page">
    <!-- 查询栏 -->
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="老人">
          <el-select v-model="query.residentId" placeholder="全部" clearable filterable class="search-input">
            <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable class="search-select">
            <el-option label="正常" value="NORMAL" />
            <el-option label="异常" value="ABNORMAL" />
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
          <el-button type="success" :icon="Plus" @click="showDialog()">新增记录</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe :row-class-name="rowClassName">
        <template #empty>
          <el-empty description="暂无健康记录" />
        </template>
        <el-table-column prop="residentName" label="老人" min-width="90" />
        <el-table-column prop="temperature" label="体温(℃)" min-width="90">
          <template #default="{ row }">
            <span :style="{ color: row.temperature > 37.2 ? '#f56c6c' : '' }">{{ row.temperature ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="血压" min-width="110">
          <template #default="{ row }"> {{ row.bloodSystolic ?? '-' }}/{{ row.bloodDiastolic ?? '-' }} </template>
        </el-table-column>
        <el-table-column prop="heartRate" label="心率" width="70" />
        <el-table-column prop="bloodSugar" label="血糖" width="70" />
        <el-table-column prop="weight" label="体重(kg)" min-width="80" />
        <el-table-column prop="oxygen" label="血氧(%)" width="80" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'" size="small">
              {{ row.status === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordedAt" label="测量时间" min-width="150" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" link type="primary" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" :icon="Delete" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑抽屉 -->
    <el-drawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑健康记录' : '新增健康记录'"
      size="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="老人" prop="residentId">
          <el-select v-model="form.residentId" placeholder="请选择老人" filterable style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="体温(℃)">
              <el-input-number
                v-model="form.temperature"
                :precision="1"
                :step="0.1"
                :min="35"
                :max="42"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="心率">
              <el-input-number v-model="form.heartRate" :min="30" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收缩压">
              <el-input-number v-model="form.bloodSystolic" :min="60" :max="250" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="舒张压">
              <el-input-number v-model="form.bloodDiastolic" :min="30" :max="150" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血糖">
              <el-input-number
                v-model="form.bloodSugar"
                :precision="1"
                :step="0.1"
                :min="1"
                :max="30"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number
                v-model="form.weight"
                :precision="1"
                :step="0.1"
                :min="20"
                :max="200"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血氧(%)">
              <el-input-number v-model="form.oxygen" :min="50" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="正常" value="NORMAL" />
                <el-option label="异常" value="ABNORMAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测量时间">
              <el-date-picker v-model="form.recordedAt" type="datetime" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="form.status === 'ABNORMAL'">
            <el-form-item label="异常说明">
              <el-input v-model="form.abnormal" type="textarea" :rows="2" placeholder="描述异常情况" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="其他说明" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.health-page {
  animation: fadeIn 0.35s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-bar {
  background: #fff;
  padding: 16px 20px 0;
  border-radius: 10px;
  border: 1px solid #ebeef5;

  .search-input {
    width: 150px;
  }
  .search-select {
    width: 100px;
  }
  .search-date {
    width: 130px;
  }
}

.table-card {
  margin-top: 16px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 10px;
  border: 1px solid #ebeef5;

  :deep(.row-abnormal) {
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
