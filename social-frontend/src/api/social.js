import request from './request'

// 关注用户
export function follow(followId) {
  return request.post(`/social/follow/${followId}`)
}

// 取消关注
export function unfollow(followId) {
  return request.delete(`/social/follow/${followId}`)
}

// 获取关注列表
export function getFollowingList(userId) {
  return request.get(`/social/following/${userId}`)
}

// 获取粉丝列表
export function getFollowerList(userId) {
  return request.get(`/social/followers/${userId}`)
}

// 获取关注数
export function getFollowingCount(userId) {
  return request.get(`/social/following/${userId}/count`)
}

// 获取粉丝数
export function getFollowerCount(userId) {
  return request.get(`/social/followers/${userId}/count`)
}

// 是否关注
export function checkFollowing(targetUserId) {
  return request.get(`/social/follow/check/${targetUserId}`)
}

// 发送好友申请
export function sendFriendRequest(friendId) {
  return request.post(`/social/friend/${friendId}`)
}

// 接受好友申请
export function acceptFriendRequest(friendId) {
  return request.post(`/social/friend/${friendId}/accept`)
}

// 拒绝好友申请
export function rejectFriendRequest(friendId) {
  return request.post(`/social/friend/${friendId}/reject`)
}

// 删除好友
export function deleteFriend(friendId) {
  return request.delete(`/social/friend/${friendId}`)
}

// 获取好友列表
export function getFriendList() {
  return request.get('/social/friends')
}

// 获取待处理的好友申请
export function getPendingRequests() {
  return request.get('/social/friends/pending')
}

// 拉黑用户
export function addToBlacklist(blackId) {
  return request.post(`/social/blacklist/${blackId}`)
}

// 取消拉黑
export function removeFromBlacklist(blackId) {
  return request.delete(`/social/blacklist/${blackId}`)
}

// 获取黑名单
export function getBlacklist() {
  return request.get('/social/blacklist')
}
