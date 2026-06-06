import request from './request'

// 创建群聊
export function createGroup(data) {
  return request.post('/group', data)
}

// 获取群信息
export function getGroupInfo(groupId) {
  return request.get(`/group/${groupId}`)
}

// 获取我的群聊列表
export function getMyGroups() {
  return request.get('/group/my')
}

// 添加群成员
export function addGroupMember(groupId, userId) {
  return request.post(`/group/${groupId}/members`, { userId })
}

// 移除群成员
export function removeGroupMember(groupId, userId) {
  return request.delete(`/group/${groupId}/members/${userId}`)
}

// 获取群成员列表
export function getGroupMembers(groupId) {
  return request.get(`/group/${groupId}/members`)
}

// 解散群聊
export function deleteGroup(groupId) {
  return request.delete(`/group/${groupId}`)
}

// 退出群聊
export function quitGroup(groupId) {
  return request.post(`/group/${groupId}/quit`)
}
