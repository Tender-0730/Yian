<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getResidentById } from '@/api/resident'

const route = useRoute()
const router = useRouter()
const resident = ref({})
const loading = ref(false)

const genderMap = { 1: '男', 2: '女' }
const statusMap = { IN_RESIDENCE: '在院', CHECKED_OUT: '已退住' }

onMounted(async () => {
  loading.value = true
  try {
    resident.value = await getResidentById(route.params.id)
  } finally { loading.value = false }
})
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.push('/residents')">返回列表</el-button>
      <span class="header-title">{{ resident.name || '老人详情' }}</span>
    </div>

    <div class="detail-grid">
      <!-- 基本信息 -->
      <div class="detail-card">
        <div class="card-title">基本信息</div>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="姓名">{{ resident.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderMap[resident.gender] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ resident.age ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="resident.status === 'IN_RESIDENCE' ? 'success' : 'info'" size="small">
              {{ statusMap[resident.status] || resident.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ resident.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ resident.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ resident.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ resident.admissionDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退住日期" :span="1">{{ resident.dischargeDate || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 护理与床位 -->
      <div class="detail-card">
        <div class="card-title">护理与床位</div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="护理级别">
            <el-tag type="warning" size="small">{{ resident.careLevelName || '未分配' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="房间号">{{ resident.roomNumber || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="床位号">{{ resident.bedNumber || '未分配' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 紧急联系人 -->
      <div class="detail-card">
        <div class="card-title">紧急联系人</div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="姓名">{{ resident.emergencyName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ resident.emergencyPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关系">{{ resident.emergencyRelation || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 健康信息 -->
      <div class="detail-card">
        <div class="card-title">健康信息</div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="既往病史">{{ resident.medicalHistory || '无' }}</el-descriptions-item>
          <el-descriptions-item label="过敏史">{{ resident.allergies || '无' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ resident.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.detail-page {
  animation: fadeIn 0.35s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #ebeef5;

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 14px;
    padding-left: 10px;
    border-left: 3px solid #409eff;
  }
}
</style>
