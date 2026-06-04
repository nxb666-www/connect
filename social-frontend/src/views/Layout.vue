<template>
  <div class="layout" ref="layoutRef">
    <!-- 侧边栏 -->
    <aside class="sidebar" ref="sidebarRef">
      <div class="sidebar-glow"></div>

      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">
            <div class="logo-ring"></div>
            <div class="logo-core"></div>
          </div>
          <span class="logo-text">Connect</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="(item, index) in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          :ref="el => navItemsRef[index] = el"
        >
          <div class="nav-icon-wrap">
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          </div>
          <span class="nav-text">{{ item.label }}</span>
          <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
          <div class="nav-indicator"></div>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <el-dropdown trigger="click" @command="handleUserMenu">
          <div class="user-card">
            <div class="user-avatar-wrap">
              <el-avatar :size="40" :src="userStore.userInfo.avatar" class="user-avatar">
                {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="online-indicator"></span>
            </div>
            <div class="user-info">
              <span class="user-name">{{ userStore.userInfo.nickname || '用户' }}</span>
              <span class="user-status">在线</span>
            </div>
            <div class="user-menu-icon">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="1"/><circle cx="12" cy="5" r="1"/><circle cx="12" cy="19" r="1"/>
              </svg>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <header class="top-header" ref="headerRef">
        <div class="header-left">
          <h2 class="page-title">{{ currentPageTitle }}</h2>
          <span class="page-subtitle">{{ currentDate }}</span>
        </div>
        <div class="header-right">
          <div class="search-box" @click="goToSearch">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <span>搜索</span>
            <kbd>⌘K</kbd>
          </div>
          <button class="icon-btn notification-btn" @click="goToMessage">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="unreadCount" class="notification-dot">{{ unreadCount }}</span>
          </button>
        </div>
      </header>

      <div class="content-area">
        <router-view v-slot="{ Component, route }">
          <component :is="Component" :key="route.path" />
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUnreadCount } from '../api/message'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
const layoutRef = ref(null)
const sidebarRef = ref(null)
const headerRef = ref(null)
const navItemsRef = ref([])
const transitionName = ref('fade')

const menuItems = computed(() => {
  const items = [
    { path: '/home', label: '首页', icon: 'HomeFilled' },
    { path: '/post', label: '动态', icon: 'EditPen' },
    { path: '/friends', label: '好友', icon: 'User' },
    { path: '/message', label: '消息', icon: 'ChatDotRound', badge: unreadCount.value },
    { path: '/ai', label: 'AI助手', icon: 'MagicStick' },
    { path: '/search', label: '搜索', icon: 'Search' }
  ]
  if (userStore.userInfo.role === 'admin') {
    items.push({ path: '/admin', label: '管理', icon: 'Setting' })
  }
  return items
})

const currentDate = computed(() => {
  const now = new Date()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const weekDay = weekDays[now.getDay()]
  return `${month}月${day}日 周${weekDay}`
})

const currentPageTitle = computed(() => {
  const titles = {
    '/home': '首页',
    '/post': '动态',
    '/friends': '好友',
    '/message': '消息',
    '/ai': 'AI助手',
    '/search': '搜索',
    '/profile': '个人中心',
    '/admin': '管理后台'
  }
  return titles[route.path] || '首页'
})

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToSearch = () => {
  router.push('/search')
}

const goToMessage = () => {
  router.push('/message')
}

const handleUserMenu = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}

// 路由过渡动画
const routeOrder = { '/home': 0, '/post': 1, '/friends': 2, '/message': 3, '/ai': 4, '/search': 5, '/profile': 6 }

watch(() => route.path, (to, from) => {
  const toOrder = routeOrder[to] ?? 3
  const fromOrder = routeOrder[from] ?? 3
  transitionName.value = toOrder > fromOrder ? 'slide-left' : 'slide-right'
})

let animCtx = null
let unreadPollTimer = null

const refreshUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) { /* silent */ }
}

const onUnreadUpdated = (e) => {
  unreadCount.value = e.detail || 0
}

onMounted(async () => {
  // 监听消息已读事件
  window.addEventListener('unread-updated', onUnreadUpdated)

  if (userStore.token) {
    try {
      await userStore.getUserInfo()
      await refreshUnreadCount()
      // 每30秒刷新未读消息数
      unreadPollTimer = setInterval(refreshUnreadCount, 30000)
    } catch (error) {
      console.error(error)
    }
  }

  try {
    const gsap = window.gsap
    if (!gsap || !sidebarRef.value) return

    animCtx = gsap.context(() => {
      gsap.fromTo(sidebarRef.value,
        { x: -60, opacity: 0 },
        { x: 0, opacity: 1, duration: 0.6, ease: 'power3.out' }
      )

      gsap.fromTo(navItemsRef.value.filter(Boolean),
        { x: -30, opacity: 0 },
        { x: 0, opacity: 1, duration: 0.4, stagger: 0.06, ease: 'power2.out', delay: 0.3 }
      )

      if (headerRef.value) {
        gsap.fromTo(headerRef.value,
          { y: -20, opacity: 0 },
          { y: 0, opacity: 1, duration: 0.5, ease: 'power2.out', delay: 0.2 }
        )
      }
    }, layoutRef.value)
  } catch (e) { console.warn('GSAP animation failed', e) }
})

onUnmounted(() => {
  animCtx?.revert()
  if (unreadPollTimer) {
    clearInterval(unreadPollTimer)
    unreadPollTimer = null
  }
  window.removeEventListener('unread-updated', onUnreadUpdated)
})
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: #0f0f14;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: rgba(18, 18, 24, 0.95);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  border-right: 1px solid rgba(255, 255, 255, 0.04);
}

.sidebar-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(180deg, rgba(255, 107, 107, 0.06) 0%, transparent 100%);
  pointer-events: none;
}

.sidebar-header {
  padding: 28px 24px 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  width: 38px;
  height: 38px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(255, 107, 107, 0.3);
  border-radius: 12px;
  transform: rotate(45deg);
}

.logo-core {
  width: 18px;
  height: 18px;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  border-radius: 6px;
  box-shadow: 0 0 20px rgba(255, 107, 107, 0.3);
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
}

/* 导航菜单 */
.sidebar-nav {
  flex: 1;
  padding: 8px 14px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 13px 16px;
  margin-bottom: 4px;
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.4);
  text-decoration: none;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.7);
}

.nav-item.active {
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
}

.nav-icon-wrap {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  transition: all 0.25s ease;
}

.nav-item.active .nav-icon-wrap {
  background: rgba(255, 107, 107, 0.15);
}

.nav-icon {
  font-size: 18px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

.nav-badge {
  margin-left: auto;
  background: #FF6B6B;
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
  min-width: 20px;
  text-align: center;
}

.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: #FF6B6B;
  border-radius: 0 2px 2px 0;
  transition: height 0.25s ease;
}

.nav-item.active .nav-indicator {
  height: 24px;
}

/* 底部用户卡片 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.25s ease;
}

.user-card:hover {
  background: rgba(255, 255, 255, 0.04);
}

.user-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-weight: 600;
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #22c55e;
  border: 2px solid #121218;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  display: block;
  color: #22c55e;
  font-size: 11px;
  margin-top: 2px;
}

.user-menu-icon {
  color: rgba(255, 255, 255, 0.2);
  transition: color 0.2s;
}

.user-card:hover .user-menu-icon {
  color: rgba(255, 255, 255, 0.5);
}

/* 主内容区 */
.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* 顶部头部 */
.top-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 36px;
  background: rgba(18, 18, 24, 0.6);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  color: rgba(255, 255, 255, 0.35);
  font-size: 13px;
}

.search-box:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);
}

.search-box kbd {
  font-size: 11px;
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.25);
  font-family: inherit;
}

.icon-btn {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #FF6B6B;
  border-color: rgba(255, 107, 107, 0.2);
}

.notification-dot {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: #FF6B6B;
  color: white;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  border: 2px solid #121218;
}

/* 内容区域 */
.content-area {
  flex: 1;
  padding: 28px 36px;
  padding-bottom: 60px;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
}

/* 自定义滚动条 */
.content-area::-webkit-scrollbar {
  width: 6px;
}

.content-area::-webkit-scrollbar-track {
  background: transparent;
}

.content-area::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
}

.content-area::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-left-enter-from {
  transform: translateX(20px);
}

.slide-left-leave-to {
  transform: translateX(-20px);
}

.slide-right-enter-from {
  transform: translateX(-20px);
}

.slide-right-leave-to {
  transform: translateX(20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .main-content {
    margin-left: 0;
    height: 100vh;
  }

  .content-area {
    padding: 16px;
  }

  .top-header {
    padding: 16px 20px;
  }

  .page-title {
    font-size: 18px;
  }

  .search-box {
    display: none;
  }
}
</style>
