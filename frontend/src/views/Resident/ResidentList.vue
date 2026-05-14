<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Edit, Delete, Plus, View } from '@element-plus/icons-vue'
import { pageResidents, createResident, updateResident, deleteResident, listCareLevels } from '@/api/resident'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const careLevels = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const query = reactive({ name: '', status: '', careLevelId: null })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive(createEmptyForm())

const genderMap = { 1: '男', 2: '女' }
const statusMap = { IN_RESIDENCE: '在院', CHECKED_OUT: '已退住' }

function createEmptyForm() {
  return {
    id: null,
    name: '',
    gender: null,
    age: null,
    birthday: '',
    idCard: '',
    phone: '',
    emergencyName: '',
    emergencyPhone: '',
    emergencyRelation: '',
    admissionDate: '',
    medicalHistory: '',
    allergies: '',
    remark: '',
  }
}

const rules = {
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    for (const [k, v] of Object.entries(query)) {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    }
    const res = await pageResidents(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const showDialog = (row = null) => {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : createEmptyForm())
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateResident(form.id, form)
    ElMessage.success('编辑成功')
  } else {
    await createResident(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async row => {
  await ElMessageBox.confirm(`确定删除老人"${row.name}"吗？此操作不可恢复。`, '确认删除', { type: 'warning' })
  await deleteResident(row.id)
  ElMessage.success('已删除')
  fetchData()
}

onMounted(async () => {
  careLevels.value = await listCareLevels()
  fetchData()
})
</script>

<template>
  <div class="resident-page">
    <!-- 查询栏 -->
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="输入姓名" clearable class="search-input" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable class="search-select-sm">
            <el-option label="在院" value="IN_RESIDENCE" />
            <el-option label="已退住" value="CHECKED_OUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="护理级别">
          <el-select v-model="query.careLevelId" placeholder="全部" clearable class="search-select-md">
            <el-option v-for="cl in careLevels" :key="cl.id" :label="cl.levelName" :value="cl.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button type="success" :icon="Plus" @click="showDialog()">新增老人</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <template #empty>
          <el-empty description="暂无老人数据" />
        </template>
        <el-table-column prop="name" label="姓名" min-width="90" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">{{ genderMap[row.gender] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="idCard" label="身份证号" min-width="170" />
        <el-table-column prop="phone" label="联系电话" min-width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'IN_RESIDENCE' ? 'success' : 'info'" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="careLevelName" label="护理级别" min-width="100" />
        <el-table-column prop="roomNumber" label="房间" width="80" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="View" link type="primary" @click="router.push(`/residents/${row.id}`)"
              >详情</el-button
            >
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
      :title="isEdit ? '编辑老人信息' : '新增老人'"
      size="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="form.age" :min="0" :max="150" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" placeholder="18位身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="11位手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住日期">
              <el-date-picker v-model="form.admissionDate" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系人">
              <el-input v-model="form.emergencyName" placeholder="联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人电话">
              <el-input v-model="form.emergencyPhone" placeholder="联系人手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="与老人关系">
              <el-input v-model="form.emergencyRelation" placeholder="如：子女、配偶" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="既往病史">
              <el-input v-model="form.medicalHistory" type="textarea" :rows="2" placeholder="如：高血压、糖尿病" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="过敏史">
              <el-input v-model="form.allergies" type="textarea" :rows="2" placeholder="如：青霉素、海鲜" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他需要说明的信息" />
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
.resident-page {
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
    width: 160px;
  }
  .search-select-sm {
    width: 120px;
  }
  .search-select-md {
    width: 140px;
  }
}

.table-card {
  margin-top: 16px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
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
