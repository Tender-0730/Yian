import request from './request'

// 药品字典
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageDrugDicts = params => request.get('/drug-dicts', { params })
/** @returns {Promise<object>} */
export const getDrugDictById = id => request.get(`/drug-dicts/${id}`)
/** @returns {Promise<number>} */
export const createDrugDict = data => request.post('/drug-dicts', data)
/** @returns {Promise<void>} */
export const updateDrugDict = (id, data) => request.put(`/drug-dicts/${id}`, data)
/** @returns {Promise<void>} */
export const deleteDrugDict = id => request.delete(`/drug-dicts/${id}`)

// 库存
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageInventories = params => request.get('/drug-inventories', { params })
/** @returns {Promise<void>} */
export const inbound = data => request.post('/drug-inventories/inbound', data)
/** @returns {Promise<void>} */
export const outbound = data => request.post('/drug-inventories/outbound', data)
/** @returns {Promise<Array>} */
export const listInventoryAlerts = () => request.get('/drug-inventories/alerts')

// 处方
/** @returns {Promise<{ records: Array, total: number }>} */
export const pagePrescriptions = params => request.get('/drug-prescriptions', { params })
/** @returns {Promise<number>} */
export const createPrescription = data => request.post('/drug-prescriptions', data)
/** @returns {Promise<void>} */
export const updatePrescription = (id, data) => request.put(`/drug-prescriptions/${id}`, data)

// 服药记录
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageDrugRecords = params => request.get('/drug-records', { params })
/** @returns {Promise<Array>} */
export const listPending = params => request.get('/drug-records/pending', { params })
/** @returns {Promise<void>} */
export const createDrugRecord = data => request.post('/drug-records', data)
