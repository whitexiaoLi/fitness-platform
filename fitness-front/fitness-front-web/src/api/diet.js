import request from '@/utils/request'

// ---- Diet Records ----
export function addDietRecord(data) {
  return request.post('/diet/records', data)
}

export function getDietRecords(date) {
  return request.get('/diet/records', { params: { date } })
}

export function updateDietRecord(id, data) {
  return request.put(`/diet/records/${id}`, data)
}

export function deleteDietRecord(id) {
  return request.delete(`/diet/records/${id}`)
}

// ---- Food Search ----
export function searchFoods(keyword, pageSize = 20) {
  return request.get('/diet/foods/search', { params: { keyword, pageSize } })
}

export function getFoodDetail(id) {
  return request.get(`/diet/foods/${id}`)
}

export function createCustomFood(data) {
  return request.post('/diet/foods/custom', data)
}

// ---- Custom Meal Types ----
export function getMealTypes() {
  return request.get('/diet/meal-types')
}

export function createMealType(data) {
  return request.post('/diet/meal-types', data)
}

export function updateMealType(id, data) {
  return request.put(`/diet/meal-types/${id}`, data)
}

export function deleteMealType(id) {
  return request.delete(`/diet/meal-types/${id}`)
}
