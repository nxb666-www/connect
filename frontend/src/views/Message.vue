<template>
  <div class="message-page" ref="pageRef">
    <div class="message-container">
      <!-- 会话列表 -->
      <aside class="conversation-list">
        <div class="list-header">
          <h3>消息</h3>
          <div class="header-btns">
            <button class="create-group-btn" @click="showCreateGroup = true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              <span>建群</span>
            </button>
          </div>
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
              <el-avatar :size="48" :src="conv.avatar" style="cursor:pointer" @click.stop="!conv.isGroup && router.push(`/profile/${conv.id}`)">
                {{ conv.name?.charAt(0) || (conv.isGroup ? 'G' : 'U') }}
              </el-avatar>
              <span v-if="conv.online && !conv.isGroup" class="online-dot"></span>
              <span v-if="conv.isGroup" class="group-badge">群</span>
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
              <el-avatar :size="40" :src="currentConversation.avatar" style="cursor:pointer" @click="!currentConversation.isGroup && router.push(`/profile/${currentConversation.id}`)">
                {{ currentConversation.name?.charAt(0) || (currentConversation.isGroup ? 'G' : 'U') }}
              </el-avatar>
              <div>
                <h3>{{ currentConversation.name }}</h3>
                <span v-if="currentConversation.isGroup" class="status group-status">群聊</span>
                <span v-else class="status" :class="{ online: currentConversation.online }">
                  {{ currentConversation.online ? '在线' : '离线' }}
                </span>
              </div>
            </div>
            <div class="header-actions">
              <button v-if="currentConversation.isGroup" class="icon-btn" @click="loadGroupMembers" title="群成员">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </button>
              <button v-if="!currentConversation.isGroup" class="icon-btn" @click="handleVoiceCall">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
                </svg>
              </button>
              <button v-if="!currentConversation.isGroup" class="icon-btn" @click="handleVideoCall">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
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
                :src="msg.senderAvatar || currentConversation.avatar"
                class="msg-avatar"
              >
                {{ msg.senderName?.charAt(0) || currentConversation.name?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="msg-content">
                <span v-if="currentConversation.isGroup && msg.senderId !== (userStore.userInfo.userId || userStore.userInfo.id)" class="msg-sender-name">{{ msg.senderName }}</span>
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

      <!-- 群成员面板 -->
      <aside v-if="showGroupMembers" class="group-members-panel">
        <div class="panel-header">
          <h4>群成员 ({{ groupMembers.length }})</h4>
          <button class="icon-btn" @click="showGroupMembers = false">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="member-list">
          <div v-for="member in groupMembers" :key="member.userId" class="member-item">
            <el-avatar :size="32" :src="member.avatar">{{ member.nickname?.charAt(0) || member.userName?.charAt(0) || 'U' }}</el-avatar>
            <span class="member-name">{{ member.nickname || member.userName || '用户' }}</span>
            <span v-if="member.role === 2" class="role-tag owner">群主</span>
            <span v-else-if="member.role === 1" class="role-tag admin">管理员</span>
          </div>
        </div>
        <div class="group-actions">
          <button v-if="isGroupOwner" class="group-action-btn danger" @click="handleDissolveGroup">解散群聊</button>
          <button v-else class="group-action-btn" @click="handleQuitGroup">退出群聊</button>
        </div>
      </aside>
    </div>

    <!-- 创建群聊对话框 -->
    <el-dialog v-model="showCreateGroup" title="创建群聊" width="480px" class="create-group-dialog">
      <div class="create-group-form">
        <el-input v-model="newGroupName" placeholder="群名称" maxlength="20" />
        <div class="member-select">
          <h4>选择成员</h4>
          <div class="member-options">
            <div
              v-for="friend in friendList"
              :key="friend.id"
              class="member-option"
              :class="{ selected: selectedMembers.includes(friend.id) }"
              @click="toggleMember(friend.id)"
            >
              <el-avatar :size="36" :src="friend.avatar">{{ friend.name?.charAt(0) || 'U' }}</el-avatar>
              <span>{{ friend.name }}</span>
              <svg v-if="selectedMembers.includes(friend.id)" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#FF6B6B" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="cancel-btn" @click="showCreateGroup = false">取消</button>
        <button class="confirm-btn" @click="handleCreateGroup">创建 ({{ selectedMembers.length }}人)</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getRecentConversations, getConversation, getGroupConversation, sendMessage as sendMessageApi, markAsRead, markGroupAsRead, uploadMessageFile, heartbeat, checkOnlineStatus } from '../api/message'
import { getFriendList } from '../api/social'
import { getUserInfo } from '../api/user'
import { createGroup, getGroupMembers, getMyGroups, deleteGroup, quitGroup } from '../api/group'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const searchText = ref('')
const currentConv = ref(null)
const messageInput = ref('')
const messageListRef = ref(null)
const pageRef = ref(null)

const conversations = ref([])
const messages = ref({})
const friendList = ref([])
const showCreateGroup = ref(false)
const newGroupName = ref('')
const selectedMembers = ref([])
const showGroupMembers = ref(false)
const groupMembers = ref([])
const isGroupChat = (convId) => String(convId).startsWith('group_')

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
  senderName: msg.senderName || '',
  senderAvatar: msg.senderAvatar || '',
  content: msg.content,
  type: msg.type || 1,
  chatType: msg.chatType || 1,
  fileName: msg.fileName || '',
  time: msg.createTime ? new Date(msg.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '',
  rawTime: msg.createTime ? new Date(msg.createTime).getTime() : 0
})

let onlineCheckCounter = 0

const startPolling = () => {
  stopPolling()
  onlineCheckCounter = 0
  pollTimer = setInterval(async () => {
    if (currentConv.value) {
      try {
        const conv = conversations.value.find(c => c.id === currentConv.value)
        let res
        if (conv?.isGroup) {
          res = await getGroupConversation(conv.groupId)
        } else {
          res = await getConversation(currentConv.value)
        }
        const page = res.data
        messages.value[currentConv.value] = (page.records || [])
          .map(mapMessage)
          .sort((a, b) => a.rawTime - b.rawTime)
        scrollToBottom()
      } catch (e) { /* silent */ }
    }
    loadConversations()
    onlineCheckCounter++
    if (onlineCheckCounter >= 5) {
      onlineCheckCounter = 0
      checkConversationsOnline()
    }
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
  const ids = conversations.value.filter(c => !c.isGroup).map(c => c.id)
  if (ids.length === 0) return
  try {
    const res = await checkOnlineStatus(ids)
    const statusMap = res.data || {}
    conversations.value.forEach(conv => {
      const newStatus = !!statusMap[conv.id]
      if (conv.online !== newStatus) {
        conv.online = newStatus
      }
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
    const existingOrder = conversations.value.map(c => c.id)
    const myId = userStore.userInfo.userId || userStore.userInfo.id

    // 先加载我的群聊列表
    try {
      const groupRes = await getMyGroups()
      const groups = groupRes.data || []
      groups.forEach(g => {
        const convId = 'group_' + g.id
        if (!convMap[convId]) {
          convMap[convId] = {
            id: convId,
            groupId: g.id,
            name: g.name || '群聊',
            avatar: g.avatar || '',
            isGroup: true,
            online: false,
            lastMessage: '',
            time: '',
            unread: 0
          }
        }
      })
    } catch (e) { /* silent */ }

    list.forEach(msg => {
      if (msg.chatType === 2 && msg.groupId) {
        // 群聊会话 — 更新已有群的最后消息
        const convId = 'group_' + msg.groupId
        if (convMap[convId]) {
          convMap[convId].lastMessage = `${msg.senderName}: ${msg.content}`
          convMap[convId].time = msg.createTime ? new Date(msg.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : ''
        }
      } else {
        // 私聊会话
        const otherId = msg.senderId === myId ? msg.receiverId : msg.senderId
        const otherName = msg.senderId === myId ? msg.receiverName : msg.senderName
        const otherAvatar = msg.senderId === myId ? msg.receiverAvatar : msg.senderAvatar
        if (!convMap[otherId]) {
          const existing = conversations.value.find(c => c.id === otherId)
          convMap[otherId] = {
            id: otherId,
            name: otherName || existing?.name || '用户',
            avatar: otherAvatar || existing?.avatar || '',
            isGroup: false,
            online: false,
            lastMessage: msg.content,
            time: msg.createTime ? new Date(msg.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '',
            unread: msg.isRead === 0 && msg.senderId !== myId ? 1 : 0
          }
        }
      }
    })

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

const loadMessages = async (convId) => {
  try {
    const conv = conversations.value.find(c => c.id === convId)
    if (conv?.isGroup) {
      const res = await getGroupConversation(conv.groupId)
      const page = res.data
      messages.value[convId] = (page.records || [])
        .map(mapMessage)
        .sort((a, b) => a.rawTime - b.rawTime)
    } else {
      const res = await getConversation(convId)
      const page = res.data
      messages.value[convId] = (page.records || [])
        .map(mapMessage)
        .sort((a, b) => a.rawTime - b.rawTime)
    }
  } catch (e) {
    console.error('加载消息失败', e)
  }
}

const selectConversation = async (conv) => {
  currentConv.value = conv.id
  conv.unread = 0
  showGroupMembers.value = false
  if (!messages.value[conv.id]) {
    await loadMessages(conv.id)
  }
  try {
    if (conv.isGroup) {
      await markGroupAsRead(conv.groupId)
      // 自动加载群成员以判断角色
      try {
        const mRes = await getGroupMembers(conv.groupId)
        groupMembers.value = mRes.data || []
      } catch (_) {}
    } else {
      await markAsRead(conv.id)
    }
    const { getUnreadCount } = await import('../api/message')
    const countRes = await getUnreadCount()
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
    const conv = conversations.value.find(c => c.id === currentConv.value)
    if (conv?.isGroup) {
      await sendMessageApi({ groupId: conv.groupId, content, type })
    } else {
      await sendMessageApi({ receiverId: currentConv.value, content, type })
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '发送失败'
    ElMessage.error(msg)
    const arr = messages.value[currentConv.value]
    if (arr && arr.length > 0 && arr[arr.length - 1].id === Date.now()) {
      arr.pop()
    }
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
      isGroup: false,
      online: false,
      lastMessage: '',
      time: '',
      unread: 0
    })
  }
  await selectConversation(conversations.value.find(c => c.id === friend.id))
}

const handleCreateGroup = async () => {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  if (selectedMembers.value.length === 0) {
    ElMessage.warning('请选择至少一名成员')
    return
  }
  try {
    const res = await createGroup({
      name: newGroupName.value.trim(),
      memberIds: selectedMembers.value
    })
    ElMessage.success('群聊创建成功')
    showCreateGroup.value = false
    newGroupName.value = ''
    selectedMembers.value = []
    await loadConversations()
    // 自动选中新群
    const convId = 'group_' + res.data
    const conv = conversations.value.find(c => c.id === convId)
    if (conv) await selectConversation(conv)
  } catch (e) {
    ElMessage.error('创建群聊失败')
  }
}

const toggleMember = (friendId) => {
  const idx = selectedMembers.value.indexOf(friendId)
  if (idx >= 0) {
    selectedMembers.value.splice(idx, 1)
  } else {
    selectedMembers.value.push(friendId)
  }
}

const loadGroupMembers = async () => {
  const conv = conversations.value.find(c => c.id === currentConv.value)
  if (!conv?.isGroup) return
  try {
    const res = await getGroupMembers(conv.groupId)
    groupMembers.value = res.data || []
    showGroupMembers.value = true
  } catch (e) {
    ElMessage.error('获取群成员失败')
  }
}

const isGroupOwner = computed(() => {
  const conv = conversations.value.find(c => c.id === currentConv.value)
  if (!conv?.isGroup) return false
  const myId = userStore.userInfo.userId || userStore.userInfo.id
  const me = groupMembers.value.find(m => m.userId === myId)
  return me?.role === 2
})

const handleDissolveGroup = async () => {
  const conv = conversations.value.find(c => c.id === currentConv.value)
  if (!conv?.isGroup) return
  try {
    await ElMessageBox.confirm('确定解散该群聊？解散后不可恢复。', '解散群聊', { type: 'warning' })
    await deleteGroup(conv.groupId)
    ElMessage.success('群聊已解散')
    showGroupMembers.value = false
    conversations.value = conversations.value.filter(c => c.id !== conv.id)
    currentConv.value = null
    stopPolling()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.message || '解散失败')
  }
}

const handleQuitGroup = async () => {
  const conv = conversations.value.find(c => c.id === currentConv.value)
  if (!conv?.isGroup) return
  try {
    await ElMessageBox.confirm('确定退出该群聊？', '退出群聊', { type: 'warning' })
    await quitGroup(conv.groupId)
    ElMessage.success('已退出群聊')
    showGroupMembers.value = false
    conversations.value = conversations.value.filter(c => c.id !== conv.id)
    currentConv.value = null
    stopPolling()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.message || '退出失败')
  }
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

/* 群聊标识 */
.group-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-size: 9px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #16161e;
}

.group-status {
  color: #FFA07A;
}

.msg-sender-name {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 2px;
  display: block;
}

.header-btns {
  display: flex;
  gap: 4px;
}

.create-group-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 10px;
  border: 1px solid rgba(255, 107, 107, 0.3);
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.create-group-btn:hover {
  background: rgba(255, 107, 107, 0.2);
  border-color: rgba(255, 107, 107, 0.5);
}

/* 群成员面板 */
.group-members-panel {
  width: 240px;
  border-left: 1px solid rgba(255, 255, 255, 0.04);
  display: flex;
  flex-direction: column;
  background: rgba(22, 22, 30, 0.8);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.panel-header h4 {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.member-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
}

.member-name {
  flex: 1;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.role-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.role-tag.owner {
  background: rgba(255, 107, 107, 0.15);
  color: #FF6B6B;
}

.role-tag.admin {
  background: rgba(255, 160, 122, 0.15);
  color: #FFA07A;
}

.group-actions {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

.group-action-btn {
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.group-action-btn:hover {
  background: rgba(255, 255, 255, 0.08);
}

.group-action-btn.danger {
  border-color: rgba(255, 80, 80, 0.3);
  color: #ff5050;
}

.group-action-btn.danger:hover {
  background: rgba(255, 80, 80, 0.15);
}

/* 创建群聊对话框 */
.create-group-dialog :deep(.el-dialog) {
  background: rgba(22, 22, 30, 0.95);
  backdrop-filter: blur(40px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
}

.create-group-dialog :deep(.el-dialog__title) {
  color: #fff;
}

.create-group-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.create-group-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: none !important;
  border-radius: 10px;
}

.create-group-form :deep(.el-input__inner) {
  color: #fff;
}

.member-select h4 {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0 0 8px 0;
}

.member-options {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.member-option:hover {
  background: rgba(255, 255, 255, 0.04);
}

.member-option.selected {
  background: rgba(255, 107, 107, 0.1);
}

.member-option span {
  flex: 1;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.check-icon {
  flex-shrink: 0;
}

.cancel-btn {
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  cursor: pointer;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.confirm-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .conversation-list {
    width: 100%;
  }

  .chat-area {
    display: none;
  }

  .group-members-panel {
    display: none;
  }
}
</style>
