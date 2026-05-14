<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { pageMeals, createMeal, updateMeal, deleteMeal } from '@/api/meal'
import { pageResidents } from '@/api/resident'

const loading = ref(false)
const tableData = ref([])
const residents = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const query = reactive({ residentId: null, mealDate: '', mealType: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive(createEmptyForm())
const formRef = ref(null)

const mealTypeOptions = [
  { label: '早餐', value: 'BREAKFAST', color: 'warning' },
  { label: '午餐', value: 'LUNCH', color: 'success' },
  { label: '晚餐', value: 'DINNER', color: '' },
  { label: '加餐', value: 'SNACK', color: 'info' }
]
const mealTypeMap = Object.fromEntries(mealTypeOptions.map(o => [o.value, o.label]))

function createEmptyForm() {
  return { id: null, residentId: null, mealDate: '', mealType: 'BREAKFAST', content: '', notes: '' }
}

const rules = {
  residentId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  mealDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  mealType: [{ required: true, message: '请选择餐别', trigger: 'change' }],
  content: [{ required: true, message: '请输入膳食内容', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    for (const [k, v] of Object.entries(query)) {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    }
    const res = await pageMeals(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally { loading.value = false }
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
    await updateMeal(form.id, form)
    ElMessage.success('编辑成功')
  } else {
    await createMeal(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该膳食记录吗？', '确认删除', { type: 'warning' })
  await deleteMeal(row.id)
  ElMessage.success('已删除')
  fetchData()
}

const setToday = () => {
  const d = new Date()
  query.mealDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  fetchData()
}

onMounted(async () => {
  const res = await pageResidents({ size: 999 })
  residents.value = res.records || []
  fetchData()
})
</script>

<template>
  <div class="meal-page">
    <!-- 查询栏 -->
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="老人">
          <el-select v-model="query.residentId" placeholder="全部" clearable filterable style="width: 150px">
            <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="query.mealDate" type="date" placeholder="选择日期" style="width: 140px" />
        </el-form-item>
        <el-form-item label="餐别">
          <el-select v-model="query.mealType" placeholder="全部" clearable style="width: 100px">
            <el-option v-for="o in mealTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button @click="setToday">今天</el-button>
          <el-button type="success" :icon="Plus" @click="showDialog()">新增记录</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <template #empty>
          <el-empty description="暂无膳食记录" />
        </template>
        <el-table-column prop="residentName" label="老人" width="90" />
        <el-table-column prop="mealDate" label="日期" width="110" />
        <el-table-column prop="mealType" label="餐别" width="80">
          <template #default="{ row }">
            <el-tag
              v-for="o in mealTypeOptions.filter(o => o.value === row.mealType)"
              :key="o.value"
              :type="o.color"
              size="small"
            >
              {{ o.label }}
            </el-tag>
            <span v-if="!mealTypeOptions.some(o => o.value === row.mealType)">{{ row.mealType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="膳食内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip />
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑膳食记录' : '新增膳食记录'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="老人" prop="residentId">
          <el-select v-model="form.residentId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期" prop="mealDate">
              <el-date-picker v-model="form.mealDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="餐别" prop="mealType">
              <el-select v-model="form.mealType" style="width: 100%">
                <el-option v-for="o in mealTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="如：米饭、清蒸鱼、炒青菜" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" placeholder="如：忌海鲜、少盐" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.meal-page {
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
</style>
