import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// Admin API instance (base: /api/admin)
const request = axios.create({
  baseURL: `${BASE_URL}/api/admin`,
  timeout: 30000,
})

// General API instance (base: /api, for auth endpoints)
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
})

function attachToken(config) {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}

function handleAuthError(error) {
  if (error.response?.status === 401 || error.response?.status === 403) {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    window.location.href = '/login'
  }
  return Promise.reject(error)
}

request.interceptors.request.use(attachToken)
request.interceptors.response.use((r) => r.data, handleAuthError)

api.interceptors.request.use(attachToken)
api.interceptors.response.use((r) => r.data, handleAuthError)

export { request as default, api }
