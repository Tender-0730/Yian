<script setup>
import {
  Shop,
  UserFilled,
  User,
  Crop,
  EditPen,
  Postcard,
  Message,
  HomeFilled,
  CaretBottom,
  SwitchButton,
} from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
const userStore = useUserStore();
const router = useRouter();
// 下拉跳转
const handleCommand = async (key) => {
  if (key === "logout") {
    // 退出操作
    await ElMessageBox.confirm("你确认要进行退出么", "温馨提示", {
      type: "warning",
      confirmButtonText: "确认",
      cancelButtonText: "取消",
    });

    // 清除本地的数据 (user信息)
    userStore.setUser({});
    router.push("/login");
  } else {
    // 跳转操作
    router.push(`/${key}`);
  }
};
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <div class="header">
          <div class="logo-container">
            <img src="@/assets/images/logo3.png" alt="" />
          </div>
          <div class="username-container">
            <span class="username">用户名：{{ userStore.user.username }}</span>
          </div>
        </div>

        <el-dropdown placement="bottom-end" @command="handleCommand">
          <!-- 展示给用户，默认看到的 -->
          <span class="el-dropdown__box">
            <!-- <el-avatar src="@/assets/images/微信图片_20240618110446.jpg" /> -->
            <img src="@/assets/images/微信图片_20240618110446.jpg" alt="" />
            <el-icon><CaretBottom /></el-icon>
          </span>

          <!-- 折叠的下拉部分 -->
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile" :icon="User"
                >基本资料</el-dropdown-item
              >
              <!-- <el-dropdown-item command="avatar" :icon="Crop"
                >更换头像</el-dropdown-item
              > -->
              <el-dropdown-item command="password" :icon="EditPen"
                >重置密码</el-dropdown-item
              >
              <el-dropdown-item command="logout" :icon="SwitchButton"
                >退出登录</el-dropdown-item
              >
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu default-active="home" class="el-menu-vertical-demo" router>
            <el-menu-item index="home">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="message">
              <el-icon><Shop /></el-icon>
              <span>信息管理</span>
            </el-menu-item>
            <el-menu-item index="nurse">
              <el-icon><User /></el-icon>
              <span>护理管理</span>
            </el-menu-item>
            <el-menu-item index="putup">
              <el-icon><Postcard /></el-icon>
              <span>住宿管理</span>
            </el-menu-item>
            <el-menu-item index="meals">
              <el-icon><Message /></el-icon>
              <span>膳食管理</span>
            </el-menu-item>
            <el-sub-menu index="/user">
              <!-- 多级菜单的标题 - 具名插槽 title -->
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>个人中心</span>
              </template>
              <!-- 展开的内容 - 默认插槽 -->
              <el-menu-item index="/profile">
                <el-icon><User /></el-icon>
                <span>基本资料</span>
              </el-menu-item>
              <el-menu-item index="/password">
                <el-icon><EditPen /></el-icon>
                <span>重置密码</span>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  line-height: 60px;
  background-color: #f6f6f6;

  // 左侧 logo 和用户名样式
  div {
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      object-fit: cover;
      width: 100px;
      margin-left: 15px;
    }

    span {
      margin-left: 10px;
      font-size: 14px;

      color: #333;
    }
  }

  // 右侧下拉菜单样式
  .el-dropdown__box {
    display: flex;
    align-items: center;

    .el-icon {
      color: #999;
      margin-left: 10px;
    }
    img {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      object-fit: cover;
    }
    &:active,
    &:focus {
      outline: none;
    }
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logo-container {
    display: flex;
    align-items: center;
  }

  .username-container {
    position: absolute; // 使用绝对定位
    left: 20%; // 水平居中
    transform: translateX(-50%); // 向左偏移自身宽度的一半

    .username {
      display: flex;
      align-items: center;
      height: 40px;
      padding: 0 15px;
      font-size: 16px;
      font-weight: 500;
      // color: #409eff;
      // background: #ecf5ff;
      // border-radius: 20px;
    }
  }
}
</style>
