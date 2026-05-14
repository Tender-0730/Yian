<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const isRegister = ref(false)
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  rePassword: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^\S{6,15}$/, message: '密码 6-15 位非空字符', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  rePassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== form.password) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    if (isRegister.value) {
      await registerApi({ username: form.username, password: form.password, realName: form.realName })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
    } else {
      const data = await loginApi({ username: form.username, password: form.password })
      userStore.setLogin(data)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}

watch(isRegister, () => {
  formRef.value?.resetFields()
})
</script>

<template>
  <div class="login-page">
    <!-- 装饰背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1" />
      <div class="shape shape-2" />
      <div class="shape shape-3" />
    </div>

    <div class="login-card" :class="{ flipped: isRegister }">
      <!-- 左侧品牌面板 -->
      <div class="brand-panel">
        <div class="brand-content">
          <img src="@/assets/images/logo3.png" alt="logo" class="brand-logo" />
          <h1 class="brand-title">颐安养老管理系统</h1>
          <p class="brand-desc">智慧养老 · 用心呵护每一位老人</p>
          <div class="brand-footer">
            <p>{{ isRegister ? '已有账号？' : '还没有账号？' }}</p>
            <button class="switch-btn" @click="isRegister = !isRegister">
              {{ isRegister ? '立即登录' : '立即注册' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧表单面板 -->
      <div class="form-panel">
        <div class="form-content">
          <h2 class="form-title">{{ isRegister ? '创建账号' : '欢迎回来' }}</h2>
          <p class="form-subtitle">{{ isRegister ? '填写信息注册新账号' : '请输入账号密码登录系统' }}</p>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
            </el-form-item>

            <el-form-item v-if="isRegister" prop="realName">
              <el-input v-model="form.realName" placeholder="真实姓名" :prefix-icon="User" />
            </el-form-item>

            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
            </el-form-item>

            <el-form-item v-if="isRegister" prop="rePassword">
              <el-input
                v-model="form.rePassword"
                type="password"
                placeholder="确认密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
              {{ isRegister ? '注 册' : '登 录' }}
            </el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f9e8 100%);
  overflow: hidden;
  position: relative;
}

.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  .shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.12;
  }
  .shape-1 {
    width: 500px;
    height: 500px;
    background: #409eff;
    top: -200px;
    left: -100px;
  }
  .shape-2 {
    width: 400px;
    height: 400px;
    background: #67c23a;
    bottom: -150px;
    right: -100px;
  }
  .shape-3 {
    width: 200px;
    height: 200px;
    background: #e6a23c;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.login-card {
  position: relative;
  display: flex;
  width: 880px;
  min-height: 520px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.5s ease;

  &.flipped {
    flex-direction: row-reverse;
  }
}

.brand-panel {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
  color: #fff;

  .brand-content {
    text-align: center;
    max-width: 280px;
  }

  .brand-logo {
    width: 120px;
    height: auto;
    margin-bottom: 24px;
  }

  .brand-title {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 8px;
    letter-spacing: 1px;
  }

  .brand-desc {
    font-size: 14px;
    opacity: 0.8;
    margin-bottom: 48px;
  }

  .brand-footer {
    p {
      font-size: 14px;
      opacity: 0.7;
      margin-bottom: 12px;
    }
    .switch-btn {
      background: transparent;
      border: 2px solid #fff;
      color: #fff;
      padding: 10px 40px;
      border-radius: 25px;
      font-size: 15px;
      cursor: pointer;
      transition: all 0.3s;
      &:hover {
        background: rgba(255, 255, 255, 0.2);
        transform: translateY(-2px);
      }
    }
  }
}

.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;

  .form-content {
    width: 100%;
    max-width: 340px;
  }

  .form-title {
    font-size: 26px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 6px;
  }

  .form-subtitle {
    font-size: 14px;
    color: #909399;
    margin-bottom: 32px;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  .submit-btn {
    width: 100%;
    height: 46px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 4px;
    border-radius: 10px;
    background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
    border: none;
    margin-top: 8px;
    transition: all 0.3s;
    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
      box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
    }
  }
}
</style>
