import request from '@/utils/request'

export function addBodyMetrics(data) {
  return request.post('/metrics', data)
}

export function getBodyMetrics(params) {
  return request.get('/metrics', { params })
}

export function getLatestMetrics() {
  return request.get('/metrics/latest')
}
