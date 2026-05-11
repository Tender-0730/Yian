import { defineStore } from 'pinia'
import { ref } from 'vue'

// 用户模块
export const useUserStore = defineStore(
  'user',
  () => {
    // 登录用户信息
    const user = ref({})
    const setUser = (obj) => {
      console.log(obj);
      user.value = obj
      console.log(user.value);
    }

    return {
      user, 
      setUser
    }
  },
  {
    persist: true
  }
)
