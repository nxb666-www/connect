import request from './request'

// 发布动态
export function createPost(data) {
  return request.post('/post', data)
}

// 上传图片（复用消息服务的文件上传）
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/message/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取动态详情
export function getPostDetail(postId) {
  return request.get(`/post/${postId}`)
}

// 获取动态列表（广场）
export function getPostFeed(pageNum = 1, pageSize = 10) {
  return request.get('/post/feed', { params: { pageNum, pageSize } })
}

// 获取用户动态列表
export function getUserPosts(userId, pageNum = 1, pageSize = 10) {
  return request.get(`/post/user/${userId}`, { params: { pageNum, pageSize } })
}

// 删除动态
export function deletePost(postId) {
  return request.delete(`/post/${postId}`)
}

// 点赞动态
export function likePost(postId) {
  return request.post(`/post/${postId}/like`)
}

// 取消点赞
export function unlikePost(postId) {
  return request.delete(`/post/${postId}/like`)
}

// 收藏动态
export function collectPost(postId) {
  return request.post(`/post/${postId}/collect`)
}

// 取消收藏
export function uncollectPost(postId) {
  return request.delete(`/post/${postId}/collect`)
}

// 发表评论
export function createComment(data) {
  return request.post('/post/comment', data)
}

// 获取动态评论列表
export function getPostComments(postId) {
  return request.get(`/post/${postId}/comments`)
}

// 删除评论
export function deleteComment(commentId) {
  return request.delete(`/post/comment/${commentId}`)
}

// 获取用户收藏的动态
export function getUserCollectedPosts(userId, pageNum = 1, pageSize = 10) {
  return request.get(`/post/collected/${userId}`, { params: { pageNum, pageSize } })
}

// 获取用户点赞的动态
export function getUserLikedPosts(userId, pageNum = 1, pageSize = 10) {
  return request.get(`/post/liked/${userId}`, { params: { pageNum, pageSize } })
}
