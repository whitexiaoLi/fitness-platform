import request from '@/utils/request'

export function addBodyMetrics(data) { return request.post('/metrics', data) }
export function getBodyMetrics(params) { return request.get('/metrics', { params }) }
export function getLatestMetrics() { return request.get('/metrics/latest') }
export function updateBodyMetrics(id, data) { return request.put(`/metrics/${id}`, data) }
export function deleteBodyMetrics(id) { return request.delete(`/metrics/${id}`) }
