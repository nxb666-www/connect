import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

let networkErrorShown = false

// 响应拦截器
request.interceptors.response.use(
  response => {
    networkErrorShown = false
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      networkErrorShown = false
      switch (error.response.status) {
        case 401:
          localStorage.removeItem('token')
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限')
          break
        case 500:
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          ElMessage.error(error.message || '网络错误')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (!networkErrorShown) {
      networkErrorShown = true
      ElMessage.error('无法连接到服务器，请确认后端服务已启动')
      setTimeout(() => { networkErrorShown = false }, 3000)
    }
    return Promise.reject(error)
  }
)

export default request
