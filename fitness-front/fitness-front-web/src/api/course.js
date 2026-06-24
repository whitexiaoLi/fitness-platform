import request from '@/utils/request'

export function getCourses(params) {
  return request.get('/courses', { params })
}

export function getCourseDetail(id) {
  return request.get(`/courses/${id}`)
}

export function subscribeCourse(id) {
  return request.post(`/courses/${id}/subscribe`)
}

export function getMyCourses(params) {
  return request.get('/user/courses', { params })
}
