import request from './request'

/** @returns {Promise<object>} */
export const getProfile = () => request.get('/users/profile')

/** @returns {Promise<void>} */
export const updateProfile = data => request.put('/users/profile', data)

/** @returns {Promise<void>} */
export const changePassword = data => request.patch('/users/password', data)
