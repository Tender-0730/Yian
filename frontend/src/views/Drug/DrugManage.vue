<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { pageDrugDicts, createDrugDict, updateDrugDict, deleteDrugDict } from '@/api/drug'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const query = reactive({ drugName: '', category: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive(createEmptyForm())

function createEmptyForm() {
  return { id: null, drugName: '', specification: '', manufacturer: '', unit: '', category: '', description: '' }
}

const rules = {
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    for (const [k, v] of Object.entries(query)) {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    }
    const res = await pageDrugDicts(params)
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
      await updateDrugDict(form.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createDrugDict(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误已由拦截器统一提示
  }
}

const handleDelete = async row => {
  await ElMessageBox.confirm('确定删除该药品吗？', '确认删除', { type: 'warning' })
  await deleteDrugDict(row.id)
  ElMessage.success('已删除')
  fetchData()
}

onMounted(() => fetchData())
</script>

<template>
  <div class="drug-page">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="药品名称">
          <el-input v-model="query.drugName" placeholder="搜索药品" clearable class="search-input" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button type="success" :icon="Plus" @click="showDialog()">新增药品</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <template #empty><el-empty description="暂无药品" /></template>
        <el-table-column prop="drugName" label="药品名称" min-width="120" />
        <el-table-column prop="specification" label="规格" min-width="120" />
        <el-table-column prop="manufacturer" label="厂家" min-width="120" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="category" label="分类" width="80" />
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

    <el-drawer v-model="dialogVisible" :title="isEdit ? '编辑药品' : '新增药品'" size="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="药品名称" prop="drugName">
          <el-input v-model="form.drugName" placeholder="药品名称" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.specification" placeholder="如：10mg×30片" />
        </el-form-item>
        <el-form-item label="厂家">
          <el-input v-model="form.manufacturer" placeholder="生产厂家" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="片/支/瓶" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="药品分类" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
.drug-page { animation: fadeIn 0.35s ease; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.search-bar { background: #fff; padding: 16px 20px 0; border-radius: 10px; border: 1px solid #ebeef5; .search-input { width: 180px; } }
.table-card { margin-top: 16px; background: #fff; padding: 16px 20px; border-radius: 10px; border: 1px solid #ebeef5; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; margin-top: 24px; border-top: 1px solid #ebeef5; }
</style>
