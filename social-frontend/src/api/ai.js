import request from './request'

// 发送AI对话消息
export function aiChat(data) {
  return request.post('/ai/chat', data)
}

// 获取对话历史
export function getChatHistory(sessionId) {
  return request.get(`/ai/history/${sessionId}`)
}

// 获取会话列表
export function getChatSessions() {
  return request.get('/ai/sessions')
}

// 删除会话
export function deleteSession(sessionId) {
  return request.delete(`/ai/session/${sessionId}`)
}
