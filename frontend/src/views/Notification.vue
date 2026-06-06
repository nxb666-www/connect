<template>
  <div class="notification-page">
    <div class="notification-header">
      <h3>通知</h3>
      <button v-if="notifications.length" class="read-all-btn" @click="handleMarkAllRead">
        全部已读
      </button>
    </div>

    <div class="notification-list" v-if="notifications.length">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleClick(item)"
      >
        <el-avatar :size="44" :src="item.senderAvatar" class="notif-avatar">
          {{ item.senderName?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="notif-content">
          <div class="notif-text">
            <span class="sender-name">{{ item.senderName }}</span>
            <span class="action-text">{{ typeTextMap[item.type] || item.content }}</span>
          </div>
          <span class="notif-time">{{ formatTime(item.createTime) }}</span>
        </div>
        <div v-if="item.isRead === 0" class="unread-dot"></div>
      </div>
    </div>

    <div v-else class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      <p>暂无通知</p>
    </div>

    <div v-if="total > pageSize" class="load-more">
      <button class="load-more-btn" @click="loadMore" :disabled="loading">
        {{ loading ? '加载中...' : '加载更多' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '../api/notification'
import { ElMessage } from 'element-plus'

const router = useRouter()
const notifications = ref([])
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)
const loading = ref(false)

const typeTextMap = {
  like: '赞了你的动态',
  comment: '评论了你的动态',
  follow: '关注了你',
  friend_request: '向你发送了好友申请'
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN')
}

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications(pageNum.value, pageSize)
    const page = res.data
    if (pageNum.value === 1) {
      notifications.value = page.records || []
    } else {
      notifications.value.push(...(page.records || []))
    }
    total.value = page.total || 0
  } catch (e) {
    console.error('加载通知失败', e)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  pageNum.value++
  loadNotifications()
}

const handleClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await markNotificationRead(item.id)
      item.isRead = 1
      window.dispatchEvent(new CustomEvent('notification-updated'))
    } catch (e) { /* silent */ }
  }

  if (item.type === 'like' || item.type === 'comment') {
    if (item.targetId) {
      router.push({ path: '/post', query: { id: item.targetId } })
    }
  } else if (item.type === 'follow' || item.type === 'friend_request') {
    router.push(`/profile/${item.senderId}`)
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach(n => n.isRead = 1)
    window.dispatchEvent(new CustomEvent('notification-updated'))
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadNotifications()
  // 进入通知页面时刷新Layout的通知计数
  window.dispatchEvent(new CustomEvent('notification-updated'))
})
</script>

<style scoped>
.notification-page {
  max-width: 680px;
  margin: 0 auto;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.notification-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.read-all-btn {
  background: none;
  border: none;
  color: #FF6B6B;
  font-size: 13px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.read-all-btn:hover {
  background: rgba(255, 107, 107, 0.1);
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.notification-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.notification-item.unread {
  background: rgba(255, 107, 107, 0.05);
}

.notif-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-weight: 600;
}

.notif-content {
  flex: 1;
  min-width: 0;
}

.notif-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
}

.sender-name {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-right: 6px;
}

.action-text {
  color: rgba(255, 255, 255, 0.5);
}

.notif-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.25);
  margin-top: 4px;
  display: block;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #FF6B6B;
  flex-shrink: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: rgba(255, 255, 255, 0.2);
}

.empty-state svg {
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}

.load-more-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.5);
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.load-more-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.load-more-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
