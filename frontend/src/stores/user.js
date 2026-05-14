import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore(
  'user',
  () => {
    const accessToken = ref('')
    const refreshToken = ref('')
    const userInfo = ref(null)

    const isLoggedIn = computed(() => !!accessToken.value)
    const username = computed(() => userInfo.value?.username || '')
    const realName = computed(() => userInfo.value?.realName || '')
    const roleCodes = computed(() => userInfo.value?.roleCodes || [])

    const setLogin = data => {
      accessToken.value = data.accessToken
      refreshToken.value = data.refreshToken
      userInfo.value = data.userInfo
    }

    const setProfile = data => {
      userInfo.value = { ...userInfo.value, ...data }
    }

    const logout = () => {
      accessToken.value = ''
      refreshToken.value = ''
      userInfo.value = null
    }

    return {
      accessToken,
      refreshToken,
      userInfo,
      isLoggedIn,
      username,
      realName,
      roleCodes,
      setLogin,
      setProfile,
      logout,
    }
  },
  {
    persist: {
      key: 'yian-user',
      pick: ['accessToken', 'refreshToken', 'userInfo'],
    },
  },
)
