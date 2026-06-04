import request from './request'

// 发送消息
export function sendMessage(data) {
  return request.post('/message/send', data)
}

// 获取与某用户的聊天记录
export function getConversation(targetUserId, pageNum = 1, pageSize = 20) {
  return request.get(`/message/conversation/${targetUserId}`, { params: { pageNum, pageSize } })
}

// 获取最近会话列表
export function getRecentConversations() {
  return request.get('/message/conversations')
}

// 获取未读消息数
export function getUnreadCount() {
  return request.get('/message/unread/count')
}

// 标记消息已读
export function markAsRead(senderId) {
  return request.post(`/message/read/${senderId}`)
}

// 上传文件/图片
export function uploadMessageFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/message/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 心跳（更新在线状态）
export function heartbeat() {
  return request.post('/message/heartbeat')
}

// 批量检查用户在线状态
export function checkOnlineStatus(userIds) {
  return request.post('/message/online', userIds)
}
