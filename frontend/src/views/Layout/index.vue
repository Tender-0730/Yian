<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  Odometer, User, OfficeBuilding, FirstAidKit, Dish,
  UserFilled, EditPen, SwitchButton
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const iconMap = { Odometer, User, OfficeBuilding, FirstAidKit, Dish, UserFilled, EditPen }

const menuItems = computed(() =>
  router.options.routes
    .find(r => r.path === '/')
    .children
    .filter(r => !r.meta?.hidden)
    .map(r => ({ path: `/${r.path}`, title: r.meta?.title, icon: r.meta?.icon }))
)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const items = route.matched.filter(r => r.meta?.title)
  return [{ title: '首页', path: '/dashboard' }, ...items.map(r => ({ title: r.meta.title, path: r.path }))]
})

const avatarText = computed(() => {
  const name = userStore.realName || userStore.username || 'U'
  return name.charAt(0)
})

const handleCommand = async (key) => {
  if (key === 'logout') {
    await ElMessageBox.confirm('你确认要退出吗？', '温馨提示', {
      type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消'
    })
    userStore.logout()
    router.push('/login')
  } else {
    router.push(`/${key}`)
  }
}
</script>

<template>
  <div class="layout">
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <img src="@/assets/images/logo3.png" alt="logo" class="logo" />
          <span class="system-name">颐安养老管理系统</span>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-trigger">
            <span class="avatar">{{ avatarText }}</span>
            <span class="username">{{ userStore.realName || userStore.username }}</span>
            <el-icon class="arrow"><component :is="SwitchButton" style="transform: rotate(180deg)" /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile" :icon="UserFilled">个人资料</el-dropdown-item>
              <el-dropdown-item command="password" :icon="EditPen">修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-container class="layout-body">
        <el-aside width="220px" class="layout-aside">
          <el-menu :default-active="activeMenu" router class="side-menu">
            <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
              <el-icon><component :is="iconMap[item.icon]" /></el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="layout-main">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="{ path: item.path }">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="page-content">
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.layout {
  min-height: 100vh;
  background: #f0f2f5;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    .logo { width: 36px; height: 36px; }
    .system-name { font-size: 18px; font-weight: 600; color: #303133; letter-spacing: 0.5px; }
  }

  .user-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 12px;
    border-radius: 6px;
    transition: background 0.2s;

    &:hover { background: #f5f7fa; }

    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      font-weight: 600;
    }

    .username { font-size: 14px; color: #303133; }
    .arrow { font-size: 12px; color: #909399; }
  }
}

.layout-body { height: calc(100vh - 60px); }

.layout-aside {
  background: #fff;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;

  .side-menu {
    border-right: none;
    padding-top: 8px;

    .el-menu-item {
      margin: 2px 8px;
      border-radius: 8px;
      height: 44px;
      line-height: 44px;
      transition: all 0.2s;

      &:hover { background: #ecf5ff; color: #409eff; }
      &.is-active { background: linear-gradient(135deg, #ecf5ff 0%, #e8f9e8 100%); color: #409eff; font-weight: 600; }
    }
  }
}

.layout-main {
  background: #f0f2f5;
  padding: 16px 20px 20px;
  overflow-y: auto;

  .breadcrumb {
    margin-bottom: 16px;
    padding: 10px 16px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  }

  .page-content {
    min-height: calc(100vh - 160px);
  }
}

/* 页面切换动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
