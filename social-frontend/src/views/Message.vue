<template>
  <div class="message-page" ref="pageRef">
    <div class="message-container">
      <!-- 会话列表 -->
      <aside class="conversation-list">
        <div class="list-header">
          <h3>消息</h3>
          <button class="icon-btn" @click="startNewChat">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </button>
        </div>

        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input type="text" placeholder="搜索联系人..." v-model="searchText" />
        </div>

        <div class="conversations">
          <div
            v-for="conv in filteredConversations"
            :key="conv.id"
            class="conversation-item"
            :class="{ active: currentConv === conv.id, unread: conv.unread }"
            @click="selectConversation(conv)"
          >
            <div class="conv-avatar">
              <el-avatar :size="48" :src="conv.avatar">
                {{ conv.name?.charAt(0) || 'U' }}
              </el-avatar>
              <span v-if="conv.online" class="online-dot"></span>
            </div>
            <div class="conv-info">
              <div class="conv-header">
                <span class="conv-name">{{ conv.name }}</span>
                <span class="conv-time">{{ conv.time }}</span>
              </div>
              <div class="conv-preview">
                <span>{{ conv.lastMessage }}</span>
                <span v-if="conv.unread" class="unread-badge">{{ conv.unread }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 聊天区域 -->
      <main class="chat-area">
        <template v-if="currentConversation">
          <header class="chat-header">
            <div class="header-info">
              <el-avatar :size="40" :src="currentConversation.avatar">
                {{ currentConversation.name?.charAt(0) || 'U' }}
              </el-avatar>
              <div>
                <h3>{{ currentConversation.name }}</h3>
                <span class="status" :class="{ online: currentConversation.online }">
                  {{ currentConversation.online ? '在线' : '离线' }}
                </span>
              </div>
            </div>
            <div class="header-actions">
              <button class="icon-btn" @click="handleVoiceCall">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
                </svg>
              </button>
              <button class="icon-btn" @click="handleVideoCall">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
                </svg>
              </button>
              <button class="icon-btn" @click="handleMoreActions">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/>
                </svg>
              </button>
            </div>
          </header>

          <div class="message-list" ref="messageListRef">
            <div
              v-for="(msg, index) in currentMessages"
              :key="index"
              class="message-item"
              :class="msg.senderId === (userStore.userInfo.userId || userStore.userInfo.id) ? 'sent' : 'received'"
            >
              <el-avatar
                v-if="msg.senderId !== (userStore.userInfo.userId || userStore.userInfo.id)"
                :size="36"
                :src="currentConversation.avatar"
                class="msg-avatar"
              >
                {{ currentConversation.name?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="msg-content">
                <div class="msg-bubble">
                  <p v-if="msg.type === 1">{{ msg.content }}</p>
                  <img v-else-if="msg.type === 2" :src="msg.content" class="msg-image" @click="previewImage(msg.content)" />
                  <a v-else-if="msg.type === 3" :href="msg.content" target="_blank" class="msg-file">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                    </svg>
                    <span>{{ msg.fileName || '文件' }}</span>
                  </a>
                  <p v-else>{{ msg.content }}</p>
                </div>
                <span class="msg-time">{{ msg.time }}</span>
              </div>
            </div>
          </div>

          <div class="input-area">
            <div class="input-toolbar">
              <button class="tool-btn" @click="handleSendImage">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
                </svg>
              </button>
              <button class="tool-btn" @click="handleSendFile">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                </svg>
              </button>
              <button class="tool-btn" @click="handleVoiceInput">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/>
                </svg>
              </button>
            </div>
            <div class="input-wrapper">
              <el-input
                v-model="messageInput"
                type="textarea"
                :rows="1"
                :autosize="{ minRows: 1, maxRows: 4 }"
                placeholder="输入消息..."
                @keydown.enter.exact.prevent="sendTextMessage"
                class="message-input"
              />
              <button
                class="send-btn"
                :class="{ active: messageInput.trim() }"
                :disabled="!messageInput.trim()"
                @click="sendTextMessage"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
                </svg>
              </button>
            </div>
          </div>
        </template>

        <div v-else class="empty-state">
          <div class="empty-icon">
            <div class="empty-orb"></div>
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <h3>选择一个会话</h3>
          <p>从左侧列表中选择一个会话，或从好友列表发起聊天</p>
          <div class="friend-chat-list" v-if="friendList.length">
            <div v-for="friend in friendList" :key="friend.id" class="friend-chat-item" @click="startChatWithFriend(friend)">
              <el-avatar :size="40" :src="friend.avatar">{{ friend.name?.charAt(0) || 'U' }}</el-avatar>
              <span>{{ friend.name }}</span>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getRecentConversations, getConversation, sendMessage as sendMessageApi, markAsRead, uploadMessageFile, heartbeat, checkOnlineStatus } from '../api/message'
import { getFriendList } from '../api/social'
import { getUserInfo } from '../api/user'
import { ElMessage, ElImageViewer } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const searchText = ref('')
const currentConv = ref(null)
const messageInput = ref('')
const messageListRef = ref(null)
const pageRef = ref(null)

const conversations = ref([])
const messages = ref({})
const friendList = ref([])

const filteredConversations = computed(() => {
  if (!searchText.value) return conversations.value
  return conversations.value.filter(conv =>
    conv.name.includes(searchText.value)
  )
})

const currentConversation = computed(() => {
  return conversations.value.find(c => c.id === currentConv.value)
})

const currentMessages = computed(() => {
  return messages.value[currentConv.value] || []
})

const loadingConversations = ref(false)
let pollTimer = null
let heartbeatTimer = null
const imagePreviewUrl = ref(null)

const mapMessage = (msg) => ({
  id: msg.id,
  senderId: msg.senderId,
  content: msg.content,
  type: msg.type || 1,
  fileName: msg.fileName || '',
  time: msg.createTime ? new Date(msg.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '',
  rawTime: msg.createTime ? new Date(msg.createTime).getTime() : 0
})

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (currentConv.value) {
      try {
        const res = await getConversation(currentConv.value)
        const page = res.data
        messages.value[currentConv.value] = (page.records || [])
          .map(mapMessage)
          .sort((a, b) => a.rawTime - b.rawTime)
        scrollToBottom()
      } catch (e) { /* silent */ }
    }
    loadConversations()
    checkConversationsOnline()
  }, 3000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const startHeartbeat = () => {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    heartbeat().catch(() => {})
  }, 60000)
  heartbeat().catch(() => {})
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

const checkConversationsOnline = async () => {
  const ids = conversations.value.map(c => c.id)
  if (ids.length === 0) return
  try {
    const res = await checkOnlineStatus(ids)
    const statusMap = res.data || {}
    conversations.value.forEach(conv => {
      conv.online = !!statusMap[conv.id]
    })
  } catch (e) { /* silent */ }
}

const previewImage = (url) => {
  imagePreviewUrl.value = url
}

const loadConversations = async () => {
  loadingConversations.value = true
  try {
    const res = await getRecentConversations()
    const list = res.data || []
    const convMap = {}
    // 保留现有会话的顺序
    const existingOrder = conversations.value.map(c => c.id)
    list.forEach(msg => {
      const myId = userStore.userInfo.userId || userStore.userInfo.id
      const otherId = msg.senderId === myId ? msg.receiverId : msg.senderId
      const otherName = msg.senderId === myId ? msg.receiverName : msg.senderName
      const otherAvatar = msg.senderId === myId ? msg.receiverAvatar : msg.senderAvatar
      if (!convMap[otherId]) {
        const existing = conversations.value.find(c => c.id === otherId)
        convMap[otherId] = {
          id: otherId,
          name: otherName || existing?.name || '用户',
          avatar: otherAvatar || existing?.avatar || '',
          online: false,
          lastMessage: msg.content,
          time: msg.createTime ? new Date(msg.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '',
          unread: msg.isRead === 0 && msg.senderId !== myId ? 1 : 0
        }
      }
    })
    // 按原有顺序排列，新会话放在末尾
    const newConvs = Object.values(convMap)
    const sorted = []
    existingOrder.forEach(id => {
      const conv = newConvs.find(c => c.id === id)
      if (conv) sorted.push(conv)
    })
    newConvs.forEach(conv => {
      if (!sorted.find(c => c.id === conv.id)) sorted.push(conv)
    })
    conversations.value = sorted

    // auto-select from query param
    const targetId = route.query.userId
    if (targetId && conversations.value.find(c => c.id == targetId)) {
      selectConversation(conversations.value.find(c => c.id == targetId))
    }
  } catch (e) {
    console.error('加载会话失败', e)
  } finally {
    loadingConversations.value = false
  }
}

const loadMessages = async (targetUserId) => {
  try {
    const res = await getConversation(targetUserId)
    const page = res.data
    messages.value[targetUserId] = (page.records || [])
      .map(mapMessage)
      .sort((a, b) => a.rawTime - b.rawTime)
  } catch (e) {
    console.error('加载消息失败', e)
  }
}

const selectConversation = async (conv) => {
  currentConv.value = conv.id
  conv.unread = 0
  if (!messages.value[conv.id]) {
    await loadMessages(conv.id)
  }
  try {
    await markAsRead(conv.id)
    // 标记已读后刷新全局未读数
    const { getUnreadCount } = await import('../api/message')
    const countRes = await getUnreadCount()
    // 通过 window 事件通知 Layout 刷新未读数
    window.dispatchEvent(new CustomEvent('unread-updated', { detail: countRes.data || 0 }))
  } catch (e) {}
  scrollToBottom()
  startPolling()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const sendMessage = async (content, type = 1, fileName = '') => {
  if (!content || !currentConv.value) return

  if (!messages.value[currentConv.value]) {
    messages.value[currentConv.value] = []
  }

  messages.value[currentConv.value].push({
    id: Date.now(),
    senderId: (userStore.userInfo.userId || userStore.userInfo.id),
    content,
    type,
    fileName,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  scrollToBottom()

  try {
    await sendMessageApi({ receiverId: currentConv.value, content, type })
  } catch (e) {
    console.error(e)
  }
}

const sendTextMessage = () => {
  if (!messageInput.value.trim()) return
  const content = messageInput.value
  messageInput.value = ''
  sendMessage(content, 1)
}

const handleSendImage = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    try {
      ElMessage.info('正在上传图片...')
      const res = await uploadMessageFile(file)
      const { url, name } = res.data
      sendMessage(url, 2, name)
    } catch (err) {
      ElMessage.error('图片上传失败')
    }
  }
  input.click()
}

const handleSendFile = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    try {
      ElMessage.info('正在上传文件...')
      const res = await uploadMessageFile(file)
      const { url, name } = res.data
      sendMessage(url, 3, name)
    } catch (err) {
      ElMessage.error('文件上传失败')
    }
  }
  input.click()
}

const startNewChat = () => {
  currentConv.value = null
  stopPolling()
  ElMessage.info('请从左侧选择一个联系人开始聊天')
}

const handleVoiceCall = () => ElMessage.info('语音通话功能开发中')
const handleVideoCall = () => ElMessage.info('视频通话功能开发中')
const handleMoreActions = () => ElMessage.info('更多功能开发中')
const handleVoiceInput = () => ElMessage.info('语音输入功能开发中')

const loadFriendList = async () => {
  try {
    const res = await getFriendList()
    const friends = res.data || []
    if (friends.length === 0) { friendList.value = []; return }
    const details = await Promise.allSettled(friends.map(async (f) => {
      const id = f.friendId
      try {
        const uRes = await getUserInfo(id)
        const u = uRes.data
        return { id: u.userId, name: u.nickname || u.username || '用户' + id, avatar: u.avatar || '' }
      } catch { return { id, name: '用户' + id, avatar: '' } }
    }))
    friendList.value = details.filter(r => r.status === 'fulfilled').map(r => r.value)
  } catch (e) { console.error('加载好友列表失败', e) }
}

const startChatWithFriend = async (friend) => {
  if (!conversations.value.find(c => c.id === friend.id)) {
    conversations.value.unshift({
      id: friend.id,
      name: friend.name,
      avatar: friend.avatar,
      online: false,
      lastMessage: '',
      time: '',
      unread: 0
    })
  }
  await selectConversation(conversations.value.find(c => c.id === friend.id))
}

onMounted(() => {
  loadConversations()
  loadFriendList()
  startHeartbeat()
  checkConversationsOnline()
})

onUnmounted(() => {
  stopPolling()
  stopHeartbeat()
})
</script>

<style scoped>
.message-page {
  height: calc(100vh - 140px);
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.message-container {
  display: flex;
  height: 100%;
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

/* 会话列表 */
.conversation-list {
  width: 320px;
  border-right: 1px solid rgba(255, 255, 255, 0.04);
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.list-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #FF6B6B;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 16px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
}

.search-box svg {
  color: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

.conversations {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.conversation-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.conversation-item.active {
  background: rgba(255, 107, 107, 0.1);
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #22c55e;
  border: 2px solid #16161e;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-name {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.conversation-item.unread .conv-name {
  color: #FF6B6B;
}

.conv-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.25);
}

.conv-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-badge {
  background: #FF6B6B;
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 7px;
  border-radius: 8px;
  min-width: 18px;
  text-align: center;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-info h3 {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.status {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

.status.online {
  color: #22c55e;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}

.message-item.sent {
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-item.sent .msg-content {
  align-items: flex-end;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.message-item.received .msg-bubble {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.8);
  border-bottom-left-radius: 4px;
}

.message-item.sent .msg-bubble {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  border-bottom-right-radius: 4px;
}

.msg-bubble p {
  margin: 0;
}

.msg-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.2);
  margin-top: 4px;
}

/* 输入区域 */
.input-area {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

.input-toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 10px;
}

.tool-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tool-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  color: #FF6B6B;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 8px 8px 8px 16px;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border: none;
  background: transparent;
  box-shadow: none !important;
  padding: 8px 0;
  resize: none;
  font-size: 14px;
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.message-input :deep(.el-textarea__inner::placeholder) {
  color: rgba(255, 255, 255, 0.25);
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.send-btn.active {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  transform: scale(1.05);
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.3);
}

.empty-icon {
  position: relative;
  margin-bottom: 20px;
}

.empty-orb {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 1px solid rgba(255, 107, 107, 0.15);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.5; }
}

.empty-state h3 {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 13px;
  margin: 0;
}

/* 图片消息 */
.msg-image {
  max-width: 240px;
  max-height: 240px;
  border-radius: 10px;
  cursor: pointer;
  object-fit: cover;
}

/* 文件消息 */
.msg-file {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 13px;
}

.msg-file:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* 好友聊天列表 */
.friend-chat-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
  justify-content: center;
}

.friend-chat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

.friend-chat-item:hover {
  background: rgba(255, 107, 107, 0.1);
  border-color: rgba(255, 107, 107, 0.2);
}

/* 响应式 */
@media (max-width: 768px) {
  .conversation-list {
    width: 100%;
  }

  .chat-area {
    display: none;
  }
}
</style>
