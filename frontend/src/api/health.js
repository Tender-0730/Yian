import request from './request'

/** @returns {Promise<{ records: Array, total: number, page: number, size: number }>} */
export const pageHealthRecords = params => request.get('/health-records', { params })

/** @returns {Promise<object>} */
export const getHealthRecordById = id => request.get(`/health-records/${id}`)

/** @returns {Promise<number>} */
export const createHealthRecord = data => request.post('/health-records', data)

/** @returns {Promise<void>} */
export const updateHealthRecord = (id, data) => request.put(`/health-records/${id}`, data)

/** @returns {Promise<void>} */
export const deleteHealthRecord = id => request.delete(`/health-records/${id}`)
