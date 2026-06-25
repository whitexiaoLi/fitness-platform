import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/utils/request'

export const useAdminStore = defineStore('admin', () => {
  const user = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))
  const token = ref(localStorage.getItem('admin_token') || '')

  async function login(username, password) {
    const res = await api.post('/api/auth/login', { username, password })
    if (res.code === 200 && res.data.user.role === 'ADMIN') {
      user.value = res.data.user
      token.value = res.data.accessToken
      localStorage.setItem('admin_user', JSON.stringify(res.data.user))
      localStorage.setItem('admin_token', res.data.accessToken)
      return { success: true }
    }
    return { success: false, message: '仅限管理员登录' }
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('admin_user')
    localStorage.removeItem('admin_token')
    window.location.href = '/login'
  }

  const isLoggedIn = () => !!token.value

  return { user, token, login, logout, isLoggedIn }
})
