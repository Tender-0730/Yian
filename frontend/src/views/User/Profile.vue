<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  realName: '',
  phone: '',
  email: '',
  gender: null,
})

const avatarText = computed(() => {
  const name = form.realName || userStore.username || 'U'
  return name.charAt(0)
})

const rules = {
  realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await updateProfile(form)
    userStore.setProfile({ realName: form.realName, phone: form.phone, email: form.email, gender: form.gender })
    ElMessage.success('资料修改成功')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const data = await getProfile()
    Object.assign(form, { realName: data.realName, phone: data.phone, email: data.email, gender: data.gender })
  } catch {
    /* 错误已统一处理 */
  }
})
</script>

<template>
  <div class="profile-page">
    <div class="profile-card">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar">{{ avatarText }}</div>
        <div class="avatar-info">
          <div class="avatar-name">{{ form.realName || userStore.username }}</div>
          <div class="avatar-role">系统用户</div>
        </div>
      </div>

      <!-- 表单 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" size="default" class="profile-form">
        <el-form-item label="用户名">
          <el-input :model-value="userStore.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="性别">
          <el-select v-model="form.gender" style="width: 120px">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存修改</el-button>
          <el-button @click="formRef.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-page {
  animation: fadeIn 0.35s ease;
  max-width: 600px;
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

.profile-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 32px 32px 24px;
  background: linear-gradient(135deg, #ecf5ff 0%, #e8f9e8 100%);
  border-bottom: 1px solid #ebeef5;

  .avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    font-weight: 700;
    flex-shrink: 0;
  }

  .avatar-info {
    .avatar-name {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
    .avatar-role {
      font-size: 13px;
      color: #909399;
      margin-top: 2px;
    }
  }
}

.profile-form {
  padding: 24px 32px;
}
</style>
