import request from './request'

// 护工信息
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageNurses = params => request.get('/nurses', { params })
/** @returns {Promise<object>} */
export const getNurseById = id => request.get(`/nurses/${id}`)
/** @returns {Promise<number>} */
export const createNurse = data => request.post('/nurses', data)
/** @returns {Promise<void>} */
export const updateNurse = (id, data) => request.put(`/nurses/${id}`, data)
/** @returns {Promise<void>} */
export const deleteNurse = id => request.delete(`/nurses/${id}`)

// 排班
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageSchedules = params => request.get('/nurse-schedules', { params })
/** @returns {Promise<number>} */
export const createSchedule = data => request.post('/nurse-schedules', data)
/** @returns {Promise<void>} */
export const updateSchedule = (id, data) => request.put(`/nurse-schedules/${id}`, data)
/** @returns {Promise<void>} */
export const deleteSchedule = id => request.delete(`/nurse-schedules/${id}`)

// 交接班
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageHandovers = params => request.get('/shift-handovers', { params })
/** @returns {Promise<number>} */
export const createHandover = data => request.post('/shift-handovers', data)
