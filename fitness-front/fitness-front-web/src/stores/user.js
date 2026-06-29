import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')

  function setTokens(access, refresh) {
    accessToken.value = access
    refreshToken.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  async function doLogin(username, password) {
    const res = await request.post('/auth/login', { username, password })
    if (res.code === 200) {
      user.value = res.data.user
      setTokens(res.data.accessToken, res.data.refreshToken)
      localStorage.setItem('user', JSON.stringify(res.data.user))
    }
    return res
  }

  async function doRegister(data) {
    return await request.post('/auth/register', data)
  }

  async function logout() {
    try { await request.post('/auth/logout') } catch {}
    user.value = null
    accessToken.value = ''
    refreshToken.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  const isLoggedIn = () => !!accessToken.value
  const isCoach = () => user.value?.role === 'COACH'
  const isAdmin = () => user.value?.role === 'ADMIN'

  return { user, accessToken, refreshToken, setTokens, doLogin, doRegister, logout, isLoggedIn, isCoach, isAdmin }
})
