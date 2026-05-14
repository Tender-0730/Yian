import request from './request'

/** @returns {Promise<Array>} */
export const listBuildings = () => request.get('/buildings')

/** @returns {Promise<Array>} */
export const listRoomsByBuilding = (buildingId) => request.get(`/buildings/${buildingId}/rooms`)

/** @returns {Promise<Array>} */
export const listBedsByRoom = (roomId) => request.get(`/rooms/${roomId}/beds`)

/** @returns {Promise<number>} */
export const createRoom = (data) => request.post('/rooms', data)

/** @returns {Promise<void>} */
export const updateRoom = (id, data) => request.put(`/rooms/${id}`, data)

/** @returns {Promise<void>} */
export const checkIn = (data) => request.post('/check-in', data)

/** @returns {Promise<void>} */
export const checkOut = (recordId) => request.post(`/check-out/${recordId}`)
