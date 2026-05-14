import request from './request'

/** @returns {Promise<{ accessToken: string, refreshToken: string, userInfo: object }>} */
export const login = (params) => request.post('/auth/login', params)

/** @returns {Promise<void>} */
export const register = (params) => request.post('/auth/register', params)

/** @returns {Promise<{ accessToken: string, refreshToken: string, userInfo: object }>} */
export const refreshToken = (params) => request.post('/auth/refresh', params)

/** @returns {Promise<object>} */
export const getMe = () => request.get('/auth/me')
