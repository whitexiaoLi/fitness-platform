import request from '@/utils/request'

export function addTrainingRecord(data) { return request.post('/training/records', data) }
export function getTrainingRecords(params) { return request.get('/training/records', { params }) }
export function updateTrainingRecord(id, data) { return request.put(`/training/records/${id}`, data) }
export function deleteTrainingRecord(id) { return request.delete(`/training/records/${id}`) }

// Workout plans
export function getWorkoutPlans() { return request.get('/training/plans') }
export function createWorkoutPlan(data) { return request.post('/training/plans', data) }
export function updateWorkoutPlan(id, data) { return request.put(`/training/plans/${id}`, data) }
export function deleteWorkoutPlan(id) { return request.delete(`/training/plans/${id}`) }

// Exercises
export function searchExercises(keyword) { return request.get('/training/exercises', { params: { keyword } }) }
