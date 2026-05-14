import request from './request'

/** @returns {Promise<{ records: Array, total: number, page: number, size: number }>} */
export const pageMeals = params => request.get('/meals', { params })

/** @returns {Promise<Array>} */
export const getDailyMeals = date => request.get('/meals/daily', { params: { date } })

/** @returns {Promise<number>} */
export const createMeal = data => request.post('/meals', data)

/** @returns {Promise<void>} */
export const updateMeal = (id, data) => request.put(`/meals/${id}`, data)

/** @returns {Promise<void>} */
export const deleteMeal = id => request.delete(`/meals/${id}`)
