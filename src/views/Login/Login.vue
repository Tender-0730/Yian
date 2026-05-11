<template>
  <div class="login-container">
    <div class="login-box" :class="{ 'switch-right': !isRegister }">
      <!-- 左侧面板 -->
      <div class="panel left-panel">
        <div class="content">
          <!-- Logo和项目名称 -->
          <div class="logo-container">
            <img src="@/assets/images/logo3.png" alt="logo" class="logo" />
            <h1 class="project-title">颐安养老管理系统</h1>
          </div>
          <h2>{{ !isRegister ? "没有账号？" : "已有账号？" }}</h2>
          <p>
            {{
              !isRegister ? "立即注册，开启您的账号" : "欢迎回来，请直接登录"
            }}
          </p>
          <el-button class="ghost-btn" @click="isRegister = !isRegister">
            {{ !isRegister ? "去注册" : "去登录" }}
          </el-button>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="panel right-panel">
        <div class="content">
          <h1>{{ isRegister ? "用户注册" : "欢迎登录" }}</h1>
          <!-- <p>{{ isRegister ? "请填写注册信息" : "请输入您的账号和密码" }}</p> -->

          <!-- 表单 -->
          <el-form
            ref="form"
            :model="formModel"
            :rules="rules"
            class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="formModel.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="formModel.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item v-if="isRegister" prop="repassword">
              <el-input
                v-model="formModel.repassword"
                type="password"
                placeholder="请确认密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <div class="btn-container">
              <el-button
                type="primary"
                class="submit-btn"
                @click="isRegister ? register() : login()"
              >
                {{ isRegister ? "注册" : "登录" }}
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;

  .login-box {
    width: 900px;
    min-height: 500px;
    background: #fff;
    border-radius: 15px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    display: flex;
    overflow: hidden;
    transition: all 0.6s ease-in-out;

    &.switch-right {
      flex-direction: row-reverse;
    }
  }

  .panel {
    flex: 1;
    padding: 60px 40px;
    position: relative;
    transition: all 0.6s ease-in-out;

    &.left-panel {
      background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .logo-container {
    text-align: center;
    margin-bottom: 40px;

    .logo {
      width: 180px;
      height: 100px;
      margin-bottom: 20px;
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05);
      }
    }

    .project-title {
      font-size: 28px;
      font-weight: 600;
      color: #fff;
      margin: 0;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      letter-spacing: 1px;
    }
  }

  .content {
    max-width: 320px;
    margin: 0 auto;
    text-align: center;

    h2 {
      font-size: 24px;
      margin-bottom: 15px;
      font-weight: 600;
    }

    p {
      font-size: 14px;
      margin-bottom: 30px;
      opacity: 0.8;
    }
  }

  .ghost-btn {
    background: transparent;
    border: 2px solid #fff;
    color: #fff;
    width: 140px;
    height: 45px;
    font-size: 16px;
    border-radius: 25px;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateY(-3px);
    }
  }

  .login-form {
    .el-form-item {
      margin-bottom: 25px;

      :deep(.el-input) {
        --el-input-bg-color: #f5f7fa;
        --el-input-border-color: transparent;
        --el-input-hover-border-color: #409eff;
        --el-input-focus-border-color: #409eff;

        .el-input__wrapper {
          padding: 0 15px;
          height: 50px;
          border-radius: 10px;
          box-shadow: none;
          transition: all 0.3s ease;

          &:hover {
            --el-input-bg-color: #ecf5ff;
            transform: translateY(-1px);
          }

          &.is-focus {
            --el-input-bg-color: #fff;
            box-shadow: 0 0 0 1px #409eff, 0 4px 12px rgba(64, 158, 255, 0.1);
            transform: translateY(-1px);
          }
        }

        .el-input__inner {
          height: 50px;
          font-size: 15px;
          color: #333;

          &::placeholder {
            color: #909399;
          }
        }

        .el-input__prefix {
          font-size: 18px;
          color: #909399;
        }
      }
    }

    .submit-btn {
      width: 100%;
      height: 50px;
      font-size: 16px;
      font-weight: 500;
      letter-spacing: 1px;
      border-radius: 10px;
      background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
      border: none;
      margin-top: 10px;
      transition: all 0.3s ease;

      &:hover {
        opacity: 0.9;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}
</style>

<script setup>
import axios from "axios";
import { ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user.js";
import { User, Lock } from "@element-plus/icons-vue";

const userStore = useUserStore();
const router = useRouter();
const isRegister = ref(true);
const form = ref();

// 表单数据
const formModel = ref({
  username: "",
  password: "",
  repassword: "",
});

// 表单校验规则
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 5, max: 10, message: "用户名必须是 5-10位 的字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    {
      pattern: /^\S{6,15}$/,
      message: "密码必须是 6-15位 的非空字符",
      trigger: "blur",
    },
  ],
  repassword: [
    { required: true, message: "请输入密码", trigger: "blur" },
    {
      pattern: /^\S{6,15}$/,
      message: "密码必须是 6-15位 的非空字符",
      trigger: "blur",
    },
    {
      validator: (rule, value, callback) => {
        if (value !== formModel.value.password) {
          callback(new Error("两次输入密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

// 注册方法
const register = async () => {
  try {
    await form.value.validate();
    await axios.post("/api/userList", {
      username: formModel.value.username,
      password: formModel.value.password,
    });
    isRegister.value = false;
    ElMessage.success("注册成功");
  } catch (error) {
    console.error("注册失败:", error);
  }
};

// 登录方法
const login = async () => {
  try {
    await form.value.validate();
    const res = await axios.get("/api/userList");
    const flag = res.data.find(
      (item) =>
        item.username === formModel.value.username &&
        item.password === formModel.value.password
    );

    if (flag) {
      ElMessage.success("登录成功");
      userStore.setUser(formModel.value);
      router.push("/");
    } else {
      ElMessage.warning("账号或密码错误");
    }
  } catch (error) {
    console.error("登录失败:", error);
  }
};

// 监听注册状态切换，重置表单
watch(isRegister, () => {
  formModel.value = {
    username: "",
    password: "",
    repassword: "",
  };
  // 如果表单实例存在，重置校验状态
  if (form.value) {
    form.value.resetFields();
  }
});
</script>
