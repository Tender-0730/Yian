import request from './request'

// 费用配置
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageFeeConfigs = params => request.get('/fee-configs', { params })
/** @returns {Promise<number>} */
export const createFeeConfig = data => request.post('/fee-configs', data)
/** @returns {Promise<void>} */
export const updateFeeConfig = (id, data) => request.put(`/fee-configs/${id}`, data)
/** @returns {Promise<void>} */
export const deleteFeeConfig = id => request.delete(`/fee-configs/${id}`)

// 账单
/** @returns {Promise<number>} 生成账单数 */
export const generateBills = data => request.post('/bills/generate', data)
/** @returns {Promise<{ records: Array, total: number }>} */
export const pageBills = params => request.get('/bills', { params })
/** @returns {Promise<object>} */
export const getBillDetail = id => request.get(`/bills/${id}`)
/** @returns {Promise<void>} */
export const payBill = (id, data) => request.post(`/bills/${id}/pay`, data)
/** @returns {Promise<void>} */
export const cancelBill = id => request.put(`/bills/${id}/cancel`)
/** @returns {Promise<Array>} */
export const listArrears = () => request.get('/bills/arrears')
