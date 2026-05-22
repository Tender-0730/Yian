import request from './request'

/** @returns {Promise<{ records: Array, total: number, page: number, size: number }>} */
export const pageOutings = params => request.get('/outings', { params })

/** @returns {Promise<object>} */
export const getOutingById = id => request.get(`/outings/${id}`)

/** @returns {Promise<number>} */
export const createOuting = data => request.post('/outings', data)

/** @returns {Promise<void>} */
export const returnOuting = id => request.put(`/outings/${id}/return`)

/** @returns {Promise<void>} */
export const cancelOuting = id => request.delete(`/outings/${id}`)

/** @returns {Promise<Array>} */
export const listOverdue = () => request.get('/outings/overdue')
