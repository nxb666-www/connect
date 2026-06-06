import request from './request'

export function getNotifications(pageNum = 1, pageSize = 20) {
  return request.get('/notification/list', { params: { pageNum, pageSize } })
}

export function getUnreadNotificationCount() {
  return request.get('/notification/unread/count')
}

export function markNotificationRead(id) {
  return request.post(`/notification/read/${id}`)
}

export function markAllNotificationsRead() {
  return request.post('/notification/read-all')
}
