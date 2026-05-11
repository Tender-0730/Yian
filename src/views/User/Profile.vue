<script setup>
import PageContainer from "@/components/PageContainer.vue";
import { ref } from "vue";
import { useUserStore } from "@/stores/user";
import axios from "axios";
import { ElMessage } from "element-plus";

// 是在使用仓库中数据的初始值 (无需响应式) 解构无问题
const {
  user: { username, nickname, email, password },
} = useUserStore();
const formRef = ref();
const form = ref({
  username,
  nickname,
  email,
  password,
});
const rules = ref({
  nickname: [
    { required: true, message: "请输入用户昵称", trigger: "blur" },
    {
      pattern: /^\S{2,10}/,
      message: "昵称长度在2-10个非空字符",
      trigger: "blur",
    },
  ],
  email: [
    { required: true, message: "请输入用户邮箱", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱格式",
      trigger: ["blur"],
    },
  ],
});
// 提交修改
const submitForm = async () => {
  // 等待校验结果
  await formRef.value.validate();
  // 提交修改
  const res = await axios.get("/api/userList");
  const id = res.data.find((item) => item.username === username).id;
  const user = res.data.some((item) => item.username === username);
  // console.log(user);
  if (user) {
    await axios.put(`/api/userList/${id}`, form.value);
    ElMessage.success("修改成功");
    // 清空表单
    form.value = {
      username,
      nickname: "",
      email: "",
    };
  }
};
</script>

<template>
  <page-container title="基本资料">
    <!-- 表单部分 -->
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="登录名称">
        <el-input v-model="form.username" disabled></el-input>
      </el-form-item>
      <el-form-item label="用户昵称" prop="nickname">
        <el-input v-model="form.nickname"></el-input>
      </el-form-item>
      <el-form-item label="用户邮箱" prop="email">
        <el-input v-model="form.email"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">提交修改</el-button>
      </el-form-item>
    </el-form>
  </page-container>
</template>

<style scoped>
.el-input {
  width: 400px;
}
</style>
