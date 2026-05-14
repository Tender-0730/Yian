import request from './request'

/** @returns {Promise<object>} */
export const getStats = () => request.get('/dashboard/stats')
