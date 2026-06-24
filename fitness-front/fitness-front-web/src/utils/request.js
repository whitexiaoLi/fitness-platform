import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const res = await axios.post('http://localhost:8080/api/auth/refresh', {}, {
            headers: { Authorization: `Bearer ${refreshToken}` },
          })
          if (res.data?.code === 200) {
            const { accessToken, refreshToken: newRefresh } = res.data.data
            localStorage.setItem('accessToken', accessToken)
            localStorage.setItem('refreshToken', newRefresh)
            error.config.headers.Authorization = `Bearer ${accessToken}`
            return request(error.config)
          }
        } catch (e) {
          // refresh failed
        }
      }
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default request
