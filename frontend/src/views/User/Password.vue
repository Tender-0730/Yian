<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  rePassword: ''
})

// 密码强度计算
const strengthLevel = computed(() => {
  const pwd = form.newPassword
  if (!pwd) return { level: 0, text: '', color: '' }
  let score = 0
  if (pwd.length >= 8) score++
  if (pwd.length >= 12) score++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
  if (/\d/.test(pwd)) score++
  if (/[^a-zA-Z0-9]/.test(pwd)) score++
  if (score <= 2) return { level: 1, text: '弱', color: '#f56c6c', percent: 25 }
  if (score <= 3) return { level: 2, text: '中', color: '#e6a23c', percent: 55 }
  return { level: 3, text: '强', color: '#67c23a', percent: 100 }
})

const validateRePass = (_, value, callback) => {
  if (value !== form.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^\S{6,15}$/, message: '密码 6-15 位非空字符', trigger: 'blur' },
    { validator: (_, value, callback) => value === form.oldPassword ? callback(new Error('新密码不能与原密码相同')) : callback(), trigger: 'blur' }
  ],
  rePassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateRePass, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码修改成功，即将跳转登录页')
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="password-page">
    <div class="password-card">
      <div class="card-title">修改密码</div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="password-form">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="输入当前密码" />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="输入新密码" />
        </el-form-item>

        <!-- 密码强度条 -->
        <div v-if="form.newPassword" class="strength-bar">
          <div class="strength-track">
            <div class="strength-fill" :style="{ width: strengthLevel.percent + '%', background: strengthLevel.color }" />
          </div>
          <span class="strength-text" :style="{ color: strengthLevel.color }">
            密码强度：{{ strengthLevel.text }}
          </span>
        </div>

        <el-form-item label="确认密码" prop="rePassword">
          <el-input v-model="form.rePassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认修改</el-button>
          <el-button @click="formRef.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.password-page {
  animation: fadeIn 0.35s ease;
  max-width: 500px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.password-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 32px;

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 24px;
    padding-left: 10px;
    border-left: 3px solid #409eff;
  }
}

.password-form {
  max-width: 400px;
}

.strength-bar {
  margin: -8px 0 16px 100px;
  display: flex;
  align-items: center;
  gap: 10px;

  .strength-track {
    flex: 1;
    max-width: 120px;
    height: 6px;
    background: #ebeef5;
    border-radius: 3px;
    overflow: hidden;

    .strength-fill {
      height: 100%;
      border-radius: 3px;
      transition: width 0.3s ease;
    }
  }

  .strength-text {
    font-size: 12px;
    white-space: nowrap;
  }
}
</style>
