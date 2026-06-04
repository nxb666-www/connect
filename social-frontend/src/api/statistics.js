import request from './request'

// 获取平台统计数据
export function getPlatformStatistics() {
  return request.get('/statistics/platform')
}

// 按日期范围获取统计
export function getStatisticsByRange(startDate, endDate) {
  return request.get('/statistics/range', { params: { startDate, endDate } })
}
