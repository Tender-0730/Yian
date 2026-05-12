import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      component: () => import("@/views/Layout/index.vue"),
      children: [
        {
          path: "",
          redirect: "/home",
        },
        {
          path: "home",
          component: () => import("@/views/Home/Home.vue"),
        },
        {
          path: "message",
          component: () => import("@/views/Message/Message.vue"),
        },
        {
          path: "nurse",
          component: () => import("@/views/Nurse/Nurse.vue"),
        },
        {
          path: "putup",
          component: () => import("@/views/Putup/Putup.vue"),
        },
        {
          path: "meals",
          component: () => import("@/views/Meals/Meals.vue"),
        },
        {
          path: "profile",
          component: () => import("@/views/User/Profile.vue"),
        },
        {
          path: "password",
          component: () => import("@/views/User/Password.vue"),
        },
      ],
    },
    {
      path: "/login",
      component: () => import("@/views/Login/Login.vue"),
    },
  ],
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  // 如果没有登录且不是登录页，跳转到登录页
  if (!userStore.user.username && to.path !== "/login") {
    next("/login");
  } else {
    next();
  }
});

export default router;
