<template>
  <div class="home-page" ref="pageRef">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-bg"></div>
      <div class="welcome-content">
        <h1 class="welcome-title">
          {{ greeting }}，{{ userStore.userInfo.nickname || '用户' }}
        </h1>
        <p class="welcome-subtitle">今天想分享什么呢？</p>
      </div>
      <div class="welcome-actions">
        <button class="quick-btn primary" @click="router.push('/post')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          <span>发动态</span>
        </button>
        <button class="quick-btn" @click="router.push('/search')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <span>搜索</span>
        </button>
        <button class="quick-btn" @click="router.push('/message')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span>消息</span>
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" @click="router.push('/post')">
        <div class="stat-icon posts">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ platformStats.postCount || 0 }}</span>
          <span class="stat-label">动态</span>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/friends')">
        <div class="stat-icon users">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ platformStats.userCount || 0 }}</span>
          <span class="stat-label">用户</span>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/friends')">
        <div class="stat-icon friends">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ platformStats.interactionCount || 0 }}</span>
          <span class="stat-label">互动</span>
        </div>
      </div>
    </div>

    <!-- 热门话题 -->
    <div class="section-card">
      <div class="section-header">
        <h3>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#FF6B6B" stroke-width="2">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
          </svg>
          热门话题
        </h3>
        <button class="more-link" @click="router.push('/search')">查看更多</button>
      </div>
      <div class="topics-list">
        <div v-for="(topic, idx) in hotTopics" :key="topic.name" class="topic-item" :class="{ hot: idx < 3 }" @click="router.push({ path: '/search', query: { q: topic.name } })">
          <span class="topic-rank">{{ idx + 1 }}</span>
          <div class="topic-body">
            <span class="topic-tag">#{{ topic.name }}#</span>
            <span class="topic-count">{{ topic.count }} 条动态</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新动态预览 -->
    <div class="section-card">
      <div class="section-header">
        <h3>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#FF6B6B" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
          </svg>
          最新动态
        </h3>
        <button class="more-link" @click="router.push('/post')">查看全部</button>
      </div>
      <div class="preview-list">
        <div v-for="post in recentPosts" :key="post.id" class="preview-item" @click="router.push('/post')">
          <div class="preview-header">
            <el-avatar :size="36" :src="post.avatar" class="preview-avatar" style="cursor:pointer" @click.stop="router.push(`/profile/${post.userId}`)">{{ post.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="preview-meta">
              <span class="preview-author">{{ post.nickname || post.username }}</span>
              <span class="preview-time">{{ post.createTime }}</span>
            </div>
          </div>
          <p class="preview-content">{{ post.content?.substring(0, 100) }}{{ post.content?.length > 100 ? '...' : '' }}</p>
          <div v-if="post.images?.length" class="preview-images">
            <img v-for="(img, i) in post.images.slice(0, 3)" :key="i" :src="img" alt="预览图" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getPostFeed } from '../api/post'
import { getPlatformStatistics } from '../api/statistics'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const pageRef = ref(null)

const platformStats = ref({ postCount: 0, userCount: 0, interactionCount: 0 })
const recentPosts = ref([])
const hotTopics = ref([])

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

let animCtx = null

onMounted(() => {
  loadData()

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return

      animCtx = gsap.context(() => {
        gsap.fromTo('.welcome-card', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, ease: 'power3.out' })
        gsap.fromTo('.stat-card', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.4, stagger: 0.1, delay: 0.2, ease: 'power2.out' })
        gsap.fromTo('.section-card', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.4, stagger: 0.15, delay: 0.4, ease: 'power2.out' })
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})

watch(() => route.path, (newPath) => {
  if (newPath === '/home') {
    loadData()
  }
})

const loadData = async () => {
  try {
    const [statsRes, postsRes] = await Promise.allSettled([
      getPlatformStatistics(),
      getPostFeed(1, 5)
    ])

    if (postsRes.status === 'fulfilled' && postsRes.value?.data) {
      const page = postsRes.value.data
      const records = page.records || []
      recentPosts.value = records.slice(0, 5)
      // 用动态列表的实际总数，而非统计服务的30天累加
      platformStats.value.postCount = page.total || 0

      // 提取热门话题
      const topicMap = {}
      records.forEach(p => {
        if (p.topic) {
          topicMap[p.topic] = (topicMap[p.topic] || 0) + 1
        }
      })
      hotTopics.value = Object.entries(topicMap)
        .map(([name, count]) => ({ name, count }))
        .sort((a, b) => b.count - a.count)
        .slice(0, 6)
    }

    if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
      const s = statsRes.value.data
      platformStats.value.userCount = s.totalUsers || s.userCount || 0
      platformStats.value.interactionCount = (s.totalLikes || 0) + (s.totalComments || 0)
    }
  } catch (e) {
    console.error('加载首页数据失败', e)
  }
}
</script>

<style scoped>
.home-page {
  max-width: 680px;
  margin: 0 auto;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
  width: 100%;
  padding-bottom: 40px;
  min-height: 100%;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.12), rgba(255, 160, 122, 0.08));
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 107, 107, 0.12);
  position: relative;
  overflow: hidden;
}

.welcome-bg {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.1) 0%, transparent 70%);
  pointer-events: none;
}

.welcome-content {
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 6px 0;
  letter-spacing: -0.3px;
}

.welcome-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.quick-btn:hover {
  background: rgba(255, 107, 107, 0.12);
  border-color: rgba(255, 107, 107, 0.2);
  color: #FF6B6B;
  transform: translateY(-1px);
}

.quick-btn.primary {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.2), rgba(255, 160, 122, 0.15));
  border-color: rgba(255, 107, 107, 0.2);
  color: #FF6B6B;
}

.quick-btn.primary:hover {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.3), rgba(255, 160, 122, 0.2));
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.03);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.posts {
  background: rgba(255, 107, 107, 0.12);
  color: #FF6B6B;
}

.stat-icon.users {
  background: rgba(99, 102, 241, 0.12);
  color: #6366f1;
}

.stat-icon.friends {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
  margin-top: 2px;
}

/* 区块卡片 */
.section-card {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 18px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.more-link {
  background: none;
  border: none;
  color: rgba(255, 107, 107, 0.7);
  font-size: 13px;
  cursor: pointer;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
  transition: color 0.2s;
}

.more-link:hover {
  color: #FF6B6B;
}

/* 热门话题 */
.topics-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.topic-item:hover {
  background: rgba(255, 107, 107, 0.06);
  border-color: rgba(255, 107, 107, 0.12);
}

.topic-item.hot {
  background: rgba(255, 107, 107, 0.04);
}

.topic-rank {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.topic-item.hot .topic-rank {
  background: rgba(255, 107, 107, 0.15);
  color: #FF6B6B;
}

.topic-body {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.topic-tag {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 500;
}

.topic-item.hot .topic-tag {
  color: #FF6B6B;
}

.topic-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.25);
  margin-left: auto;
}

/* 最新动态预览 */
.preview-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.preview-item:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.06);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.preview-avatar {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-weight: 600;
}

.preview-meta {
  display: flex;
  flex-direction: column;
}

.preview-author {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.preview-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.2);
  margin-top: 2px;
}

.preview-content {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.7;
  margin: 0;
}

.preview-images {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.preview-images img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .welcome-actions {
    flex-direction: column;
  }
}
</style>
