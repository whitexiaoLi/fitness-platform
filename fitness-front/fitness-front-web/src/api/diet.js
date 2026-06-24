import request from '@/utils/request'

export function addDietRecord(data) {
  return request.post('/diet/records', data)
}

export function getDietRecords(date) {
  return request.get('/diet/records', { params: { date } })
}

export function deleteDietRecord(id) {
  return request.delete(`/diet/records/${id}`)
}
