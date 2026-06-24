import request from '@/utils/request'

export function login(username, password) {
  return request.post('/auth/login', { username, password })
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function refresh() {
  return request.post('/auth/refresh')
}

export function logout() {
  return request.post('/auth/logout')
}
