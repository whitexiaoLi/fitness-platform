import request from '@/utils/request'

export function addTrainingRecord(data) {
  return request.post('/training/records', data)
}

export function getTrainingRecords(params) {
  return request.get('/training/records', { params })
}
