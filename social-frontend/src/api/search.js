import request from './request'

// 综合搜索
export function search(keyword) {
  return request.get('/search', { params: { keyword } })
}

// 获取搜索历史
export function getSearchHistory() {
  return request.get('/search/history')
}

// 清空搜索历史
export function clearSearchHistory() {
  return request.delete('/search/history')
}
