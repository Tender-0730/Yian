import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login/Login.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: () => import('@/views/Layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard/Dashboard.vue'),
          meta: { title: '仪表盘', icon: 'Odometer' },
        },
        {
          path: 'residents',
          name: 'ResidentList',
          component: () => import('@/views/Resident/ResidentList.vue'),
          meta: { title: '老人管理', icon: 'User' },
        },
        {
          path: 'residents/:id',
          name: 'ResidentDetail',
          component: () => import('@/views/Resident/ResidentDetail.vue'),
          meta: { title: '老人详情', hidden: true },
        },
        {
          path: 'rooms',
          name: 'RoomManage',
          component: () => import('@/views/Room/RoomManage.vue'),
          meta: { title: '房间管理', icon: 'OfficeBuilding' },
        },
        {
          path: 'health-records',
          name: 'HealthRecords',
          component: () => import('@/views/Health/HealthRecords.vue'),
          meta: { title: '健康记录', icon: 'FirstAidKit' },
        },
        {
          path: 'meals',
          name: 'Meals',
          component: () => import('@/views/Meal/MealManage.vue'),
          meta: { title: '膳食管理', icon: 'Dish' },
        },
        {
          path: 'outings',
          name: 'OutingManage',
          component: () => import('@/views/Outing/OutingManage.vue'),
          meta: { title: '外出管理', icon: 'Place' },
        },
        {
          path: 'nurses',
          name: 'NurseManage',
          component: () => import('@/views/Nurse/NurseManage.vue'),
          meta: { title: '护工管理', icon: 'Avatar' },
        },
        {
          path: 'nurse-schedules',
          name: 'ScheduleManage',
          component: () => import('@/views/Nurse/ScheduleManage.vue'),
          meta: { title: '排班管理', icon: 'Calendar' },
        },
        {
          path: 'drugs',
          name: 'DrugManage',
          component: () => import('@/views/Drug/DrugManage.vue'),
          meta: { title: '药品管理', icon: 'Goods' },
        },
        {
          path: 'prescriptions',
          name: 'PrescriptionManage',
          component: () => import('@/views/Drug/PrescriptionManage.vue'),
          meta: { title: '处方用药', icon: 'Collection' },
        },
        {
          path: 'fees',
          name: 'FeeManage',
          component: () => import('@/views/Fee/BillManage.vue'),
          meta: { title: '费用管理', icon: 'Money' },
        },
        {
          path: 'user-center',
          meta: { title: '个人中心', icon: 'UserFilled' },
          children: [
            {
              path: 'profile',
              name: 'Profile',
              component: () => import('@/views/User/Profile.vue'),
              meta: { title: '个人资料' },
            },
            {
              path: 'password',
              name: 'Password',
              component: () => import('@/views/User/Password.vue'),
              meta: { title: '修改密码' },
            },
          ],
        },
      ],
    },
  ],
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    if (userStore.isLoggedIn) return next('/dashboard')
    return next()
  }
  if (!userStore.isLoggedIn) return next('/login')
  next()
})

export default router
