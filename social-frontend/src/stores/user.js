import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getCurrentUser } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref({})

  function normalizeUser(data) {
    return { ...data, id: data.userId || data.id }
  }

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    token.value = res.data.token
    userInfo.value = normalizeUser(res.data)
    localStorage.setItem('token', res.data.token)
    return res
  }

  async function getUserInfo() {
    const res = await getCurrentUser()
    userInfo.value = { ...userInfo.value, ...normalizeUser(res.data) }
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    login,
    getUserInfo,
    logout
  }
})
