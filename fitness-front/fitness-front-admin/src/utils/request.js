import axios from 'axios'
import { useAdminStore } from '@/stores/admin'

const request = axios.create({
  baseURL: 'http://localhost:8080/api/admin',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const store = useAdminStore()
  if (store.token) config.headers.Authorization = `Bearer ${store.token}`
  return config
})

request.interceptors.response.use(
  (r) => r.data,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      useAdminStore().logout()
    }
    return Promise.reject(error)
  },
)

export default request
