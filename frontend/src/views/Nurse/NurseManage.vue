<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { pageNurses, createNurse, updateNurse, deleteNurse } from '@/api/nurse'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const query = reactive({ status: '', shiftPreference: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive(createEmptyForm())

function createEmptyForm() {
  return {
    id: null,
    userId: null,
    employeeNo: '',
    phone: '',
    shiftPreference: '',
    remark: '',
  }
}

const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
}

const statusMap = { ACTIVE: '在职', INACTIVE: '离职' }
const preferMap = { MORNING: '早班', AFTERNOON: '中班', NIGHT: '夜班' }

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    for (const [k, v] of Object.entries(query)) {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    }
    const res = await pageNurses(params)
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
  try {
    if (isEdit.value) {
      await updateNurse(form.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createNurse(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误已由拦截器统一提示
  }
}

const handleDelete = async row => {
  await ElMessageBox.confirm('确定删除该护工吗？', '确认删除', { type: 'warning' })
  await deleteNurse(row.id)
  ElMessage.success('已删除')
  fetchData()
}

onMounted(() => fetchData())
</script>

<template>
  <div class="nurse-page">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable class="search-select">
            <el-option label="在职" value="ACTIVE" />
            <el-option label="离职" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="班次偏好">
          <el-select v-model="query.shiftPreference" placeholder="全部" clearable class="search-select">
            <el-option label="早班" value="MORNING" />
            <el-option label="中班" value="AFTERNOON" />
            <el-option label="夜班" value="NIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button type="success" :icon="Plus" @click="showDialog()">新增护工</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <template #empty><el-empty description="暂无护工" /></template>
        <el-table-column prop="nurseName" label="姓名" min-width="90" />
        <el-table-column prop="employeeNo" label="工号" min-width="100" />
        <el-table-column prop="phone" label="电话" min-width="110" />
        <el-table-column label="班次偏好" min-width="90">
          <template #default="{ row }">{{ preferMap[row.shiftPreference] || row.shiftPreference || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" link type="primary" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" :icon="Delete" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :total="pagination.total" :page-sizes="[10, 20, 50]"
          @current-change="fetchData" @size-change="fetchData"
          layout="total, sizes, prev, pager, next" background
        />
      </div>
    </div>

    <el-drawer v-model="dialogVisible" :title="isEdit ? '编辑护工' : '新增护工'" size="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="系统用户" prop="userId">
          <el-input-number v-model="form.userId" :min="1" placeholder="sys_user 用户ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="form.employeeNo" placeholder="护工工号" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="班次偏好">
          <el-select v-model="form.shiftPreference" placeholder="请选择" style="width: 100%">
            <el-option label="早班" value="MORNING" />
            <el-option label="中班" value="AFTERNOON" />
            <el-option label="夜班" value="NIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
.nurse-page { animation: fadeIn 0.35s ease; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.search-bar {
  background: #fff; padding: 16px 20px 0; border-radius: 10px; border: 1px solid #ebeef5;
  .search-select { width: 110px; }
}
.table-card {
  margin-top: 16px; background: #fff; padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5;
}
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; margin-top: 24px; border-top: 1px solid #ebeef5; }
</style>
