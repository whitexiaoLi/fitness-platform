import axios from 'axios'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.accessToken) {
    config.headers.Authorization = `Bearer ${userStore.accessToken}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      if (userStore.refreshToken) {
        try {
          const res = await axios.post('http://localhost:8080/api/auth/refresh', {}, {
            headers: { Authorization: `Bearer ${userStore.refreshToken}` },
          })
          if (res.data?.code === 200) {
            userStore.setTokens(res.data.data.accessToken, res.data.data.refreshToken)
            error.config.headers.Authorization = `Bearer ${res.data.data.accessToken}`
            return request(error.config)
          }
        } catch (e) {
          // refresh failed
        }
      }
      userStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default request
