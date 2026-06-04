import request from './request'

// 用户注册
export function register(data) {
  return request.post('/user/register', data)
}

// 用户登录
export function login(data) {
  return request.post('/user/login', data)
}

// 获取当前用户信息
export function getCurrentUser() {
  return request.get('/user/info')
}

// 获取用户信息
export function getUserInfo(userId) {
  return request.get(`/user/${userId}`)
}

// 更新用户信息
export function updateUser(data) {
  return request.put('/user/update', data)
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 管理员-获取所有用户
export function getAllUsers() {
  return request.get('/user/admin/list')
}

// 管理员-封禁/解封用户
export function updateUserStatus(userId, status) {
  return request.put(`/user/admin/${userId}/status`, null, { params: { status } })
}
