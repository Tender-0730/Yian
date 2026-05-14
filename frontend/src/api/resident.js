import request from './request'

/** @returns {Promise<{ records: Array, total: number, page: number, size: number }>} */
export const pageResidents = (params) => request.get('/residents', { params })

/** @returns {Promise<object>} */
export const getResidentById = (id) => request.get(`/residents/${id}`)

/** @returns {Promise<number>} */
export const createResident = (data) => request.post('/residents', data)

/** @returns {Promise<void>} */
export const updateResident = (id, data) => request.put(`/residents/${id}`, data)

/** @returns {Promise<void>} */
export const deleteResident = (id) => request.delete(`/residents/${id}`)

/** @returns {Promise<Array>} */
export const listCareLevels = () => request.get('/care-levels')

/** @returns {Promise<void>} */
export const changeCareLevel = (residentId, careLevelId) =>
  request.put(`/residents/${residentId}/care-level`, { careLevelId })

/** @returns {Promise<Array>} */
export const getResidentRestrictions = (residentId) =>
  request.get(`/residents/${residentId}/restrictions`)
