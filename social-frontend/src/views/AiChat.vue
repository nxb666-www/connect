<template>
  <div class="ai-chat-page" ref="pageRef">
    <div class="chat-container">
      <!-- 侧边栏 - 会话列表 -->
      <aside class="chat-sidebar">
        <div class="sidebar-header">
          <h3>AI 助手</h3>
          <button class="new-chat-btn" @click="startNewChat">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
          </button>
        </div>
        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: currentSession === session.id }"
            @click="switchSession(session.id)"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span>{{ session.title }}</span>
            <button class="delete-session-btn" @click.stop="handleDeleteSession(session.id)" title="删除对话">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
            </button>
          </div>
        </div>
      </aside>

      <!-- 聊天区域 -->
      <main class="chat-main">
        <header class="chat-header">
          <div class="header-info">
            <div class="ai-avatar">
              <div class="ai-avatar-ring">
                <div class="ai-avatar-core"></div>
              </div>
            </div>
            <div>
              <h3>Connect AI</h3>
              <span class="status">在线</span>
            </div>
          </div>
          <button class="clear-btn" @click="clearChat">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
            <span>清空</span>
          </button>
        </header>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div class="welcome-message" v-if="messages.length === 0" ref="welcomeRef">
            <div class="welcome-icon">
              <div class="welcome-orbs">
                <div class="welcome-orb"></div>
                <div class="welcome-orb inner"></div>
              </div>
              <div class="welcome-core">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                </svg>
              </div>
            </div>
            <h2>你好，我是 Connect AI</h2>
            <p>帮你快速生成文案、答疑解惑</p>
            <div class="suggestions">
              <button
                v-for="suggestion in suggestions"
                :key="suggestion"
                class="suggestion-btn"
                @click="sendMessage(suggestion)"
              >
                {{ suggestion }}
              </button>
            </div>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="msg.role"
            :ref="el => msgRefs[index] = el"
          >
            <div
              v-if="msg.role === 'assistant'"
              class="message-avatar ai"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="message-text" v-html="formatMessage(msg.content)"></div>
              </div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
            <div
              v-if="msg.role === 'user'"
              class="message-avatar user"
            >
              {{ userStore.userInfo.nickname?.charAt(0) || '我' }}
            </div>
          </div>

          <div v-if="loading" class="message-item assistant">
            <div class="message-avatar ai">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
            </div>
            <div class="message-content">
              <div class="message-bubble typing">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入你的问题... (Enter 发送)"
              @keydown="handleKeydown"
              class="message-input"
            />
            <button
              class="send-btn"
              :class="{ active: inputMessage.trim() }"
              :disabled="!inputMessage.trim() || loading"
              @click="sendMessage()"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
              </svg>
            </button>
          </div>
          <div class="input-tips">
            <span>Connect AI 可能会犯错，请核实重要信息</span>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { aiChat, getChatSessions, getChatHistory, deleteSession } from '../api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const messageListRef = ref(null)
const welcomeRef = ref(null)
const pageRef = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const currentSession = ref('1')
const msgRefs = ref([])

const sessions = ref([])

const suggestions = [
  '帮我写一条朋友圈文案',
  '写一段产品推广文案',
  '解释一下什么是微服务架构',
  '帮我润色这段文字'
]

const messages = ref([])

const formatTime = (date) => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const formatMessage = (text) => {
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const animateNewMessage = (index) => {
  const gsap = window.gsap
  if (!gsap || !msgRefs.value[index]) return

  gsap.from(msgRefs.value[index], {
    y: 20,
    opacity: 0,
    duration: 0.4,
    ease: 'power2.out'
  })
}

const sendMessage = async (text) => {
  const message = text || inputMessage.value.trim()
  if (!message || loading.value) return

  const newIndex = messages.value.length
  messages.value.push({
    role: 'user',
    content: message,
    time: formatTime(new Date())
  })

  inputMessage.value = ''
  loading.value = true
  scrollToBottom()
  nextTick(() => animateNewMessage(newIndex))

  try {
    const res = await aiChat({
      message: message,
      sessionId: currentSession.value
    })
    const vo = res.data
    const aiIndex = messages.value.length
    messages.value.push({
      role: 'assistant',
      content: vo.content,
      time: formatTime(new Date(vo.createTime))
    })
    if (vo.sessionId && vo.sessionId !== currentSession.value) {
      currentSession.value = vo.sessionId
      loadSessions()
    }
    nextTick(() => animateNewMessage(aiIndex))
  } catch (error) {
    console.error(error)
    ElMessage.error('AI 服务暂时不可用')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const loadSessions = async () => {
  try {
    const res = await getChatSessions()
    sessions.value = (res.data || []).map((id, i) => ({ id, title: `对话 ${i + 1}` }))
  } catch (e) { console.error(e) }
}

const loadHistory = async (sessionId) => {
  try {
    const res = await getChatHistory(sessionId)
    messages.value = (res.data || []).map(vo => ({
      role: vo.role,
      content: vo.content,
      time: vo.createTime ? formatTime(new Date(vo.createTime)) : ''
    }))
  } catch (e) { console.error(e) }
}

const startNewChat = () => {
  messages.value = []
  currentSession.value = ''
}

const switchSession = (id) => {
  currentSession.value = id
  loadHistory(id)
}

const clearChat = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

const handleDeleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSession.value === sessionId) {
      messages.value = []
      currentSession.value = ''
    }
    ElMessage.success('对话已删除')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

let animCtx = null

onMounted(() => {
  loadSessions()

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return

      animCtx = gsap.context(() => {
        if (welcomeRef.value) {
          gsap.fromTo(welcomeRef.value, { y: 30, opacity: 0 }, { y: 0, opacity: 1, duration: 0.6, delay: 0.2, ease: 'power3.out' })
        }
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})
</script>

<style scoped>
.ai-chat-page {
  height: calc(100vh - 140px);
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.chat-container {
  display: flex;
  height: 100%;
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

/* 侧边栏 */
.chat-sidebar {
  width: 240px;
  background: rgba(18, 18, 24, 0.8);
  border-right: 1px solid rgba(255, 255, 255, 0.04);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.sidebar-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.new-chat-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.new-chat-btn:hover {
  border-color: #FF6B6B;
  color: #FF6B6B;
  background: rgba(255, 107, 107, 0.1);
}

.session-list {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  position: relative;
}

.session-item span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-session-btn {
  opacity: 0;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.session-item:hover .delete-session-btn {
  opacity: 1;
}

.delete-session-btn:hover {
  color: #ff4d4f;
  background: rgba(255, 77, 77, 0.15);
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.7);
}

.session-item.active {
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
}

.session-item.active .delete-session-btn {
  opacity: 1;
  color: rgba(255, 107, 107, 0.5);
}

.session-item.active .delete-session-btn:hover {
  color: #ff4d4f;
}

/* 聊天主区域 */
.chat-main {
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

.ai-avatar-ring {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.ai-avatar-core {
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
}

.header-info h3 {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.status {
  font-size: 12px;
  color: #22c55e;
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.3);
  font-size: 13px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.clear-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  color: #FF6B6B;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 欢迎消息 */
.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px;
}

.welcome-icon {
  position: relative;
  width: 80px;
  height: 80px;
  margin-bottom: 28px;
}

.welcome-orbs {
  position: absolute;
  inset: 0;
}

.welcome-orb {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px solid rgba(255, 107, 107, 0.2);
  animation: spin 8s linear infinite;
}

.welcome-orb.inner {
  inset: 10px;
  border-color: rgba(255, 160, 122, 0.3);
  animation-direction: reverse;
  animation-duration: 6s;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.welcome-core {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 30px rgba(255, 107, 107, 0.3);
}

.welcome-message h2 {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px 0;
}

.welcome-message p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0 0 32px 0;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.suggestion-btn {
  padding: 10px 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.suggestion-btn:hover {
  border-color: rgba(255, 107, 107, 0.3);
  color: #FF6B6B;
  background: rgba(255, 107, 107, 0.05);
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 80%;
}

.message-item.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
}

.message-avatar.ai {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.message-avatar.user {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
}

.message-content {
  display: flex;
  flex-direction: column;
}

.message-item.user .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  max-width: 100%;
}

.message-item.assistant .message-bubble {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.8);
  border-bottom-left-radius: 4px;
}

.message-item.user .message-bubble {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-text :deep(strong) {
  font-weight: 600;
}

.message-text :deep(em) {
  font-style: italic;
}

.message-text :deep(code) {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
}

.message-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.2);
  margin-top: 6px;
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 6px;
  padding: 16px 20px;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  animation: bounce 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 输入区域 */
.input-area {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 8px 8px 8px 16px;
  transition: all 0.25s ease;
}

.input-wrapper:focus-within {
  border-color: rgba(255, 107, 107, 0.3);
  box-shadow: 0 0 0 4px rgba(255, 107, 107, 0.06);
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
  width: 40px;
  height: 40px;
  border-radius: 12px;
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

.input-tips {
  text-align: center;
  margin-top: 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.2);
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-sidebar {
    display: none;
  }

  .message-item {
    max-width: 90%;
  }
}
</style>
