<template>
  <div class="search-page" ref="pageRef">
    <div class="search-header">
      <div class="search-box" :class="{ focused: isFocused }">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          ref="searchInputRef"
          v-model="searchText"
          type="text"
          placeholder="搜索用户、动态、话题..."
          @input="handleSearch"
          @keydown.enter="doSearch"
          @focus="isFocused = true"
          @blur="isFocused = false"
          autofocus
        />
        <button v-if="searchText" class="clear-btn" @click="clearSearch">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="search-content" v-if="searchText">
      <div class="result-tabs">
        <button
          v-for="tab in resultTabs"
          :key="tab.key"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-if="activeTab === 'users'" class="result-list">
        <div v-for="user in searchResults.users" :key="user.id" class="user-item" @click="goToProfile(user.id)">
          <el-avatar :size="52" :src="user.avatar">
            {{ user.name?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-info">
            <h4>{{ user.name }}</h4>
            <p>{{ user.signature || '暂无签名' }}</p>
            <span class="user-meta">{{ user.followers }} 粉丝</span>
          </div>
          <button
            class="follow-btn"
            :class="{ following: user.isFollowing }"
            @click.stop="toggleFollow(user)"
          >
            {{ user.isFollowing ? '已关注' : '关注' }}
          </button>
        </div>
        <div v-if="searchResults.users.length === 0" class="empty-result">
          <p>未找到相关用户</p>
        </div>
      </div>

      <div v-if="activeTab === 'posts'" class="result-list">
        <div v-for="post in searchResults.posts" :key="post.id" class="post-item" @click="goToPost(post)">
          <div class="post-header">
            <el-avatar :size="36" :src="post.avatar">
              {{ post.nickname?.charAt(0) || post.username?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="post-meta">
              <span class="post-author">{{ post.nickname || post.username }}</span>
              <span class="post-time">{{ post.createTime }}</span>
            </div>
          </div>
          <div class="post-content">
            <p v-html="highlightText(post.content)"></p>
          </div>
          <div class="post-stats">
            <span>{{ post.likeCount }} 赞</span>
            <span>{{ post.commentCount }} 评论</span>
          </div>
        </div>
        <div v-if="searchResults.posts.length === 0" class="empty-result">
          <p>未找到相关动态</p>
        </div>
      </div>

      <div v-if="activeTab === 'topics'" class="result-list">
        <div v-for="topic in searchResults.topics" :key="topic.id" class="topic-item" @click="goToTopic(topic)">
          <div class="topic-icon">#</div>
          <div class="topic-info">
            <h4>{{ topic.name }}</h4>
            <p>{{ topic.count }} 条动态</p>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="arrow-icon">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
        <div v-if="searchResults.topics.length === 0" class="empty-result">
          <p>未找到相关话题</p>
        </div>
      </div>
    </div>

    <div class="search-content" v-else>
      <div class="section" v-if="searchHistory.length > 0">
        <div class="section-header">
          <h3>搜索历史</h3>
          <button class="clear-history" @click="clearHistory">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
            <span>清空</span>
          </button>
        </div>
        <div class="history-list">
          <div
            v-for="(item, index) in searchHistory"
            :key="index"
            class="history-item"
            @click="searchText = item; doSearch()"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
            <span>{{ item }}</span>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { search as searchApi, getSearchHistory as getHistoryApi, clearSearchHistory as clearHistoryApi } from '../api/search'
import { follow, unfollow, getFollowerCount, checkFollowing } from '../api/social'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const searchInputRef = ref(null)
const pageRef = ref(null)
const isFocused = ref(false)

const searchText = ref('')
const activeTab = ref('users')

const resultTabs = [
  { key: 'users', label: '用户' },
  { key: 'posts', label: '动态' },
  { key: 'topics', label: '话题' }
]

const searchResults = reactive({
  users: [],
  posts: [],
  topics: []
})

const searchHistory = ref([])

let animCtx = null

onMounted(async () => {
  nextTick(() => {
    searchInputRef.value?.focus()
  })

  try {
    const res = await getHistoryApi()
    searchHistory.value = (res.data || []).map(h => h.keyword || h)
  } catch (e) {}

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return

      animCtx = gsap.context(() => {
        gsap.fromTo('.search-header', { y: -20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, ease: 'power3.out' })
        gsap.fromTo('.search-content', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, delay: 0.15, ease: 'power3.out' })
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})

let searchTimer = null

const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    if (searchText.value.trim()) {
      doSearch()
    }
  }, 300)
}

const doSearch = async () => {
  if (!searchText.value.trim()) return

  try {
    const res = await searchApi(searchText.value)
    const data = res.data
    searchResults.users = await Promise.all((data.users || []).map(async u => {
      const [fcRes, cfRes] = await Promise.allSettled([
        getFollowerCount(u.userId),
        checkFollowing(u.userId)
      ])
      return {
        id: u.userId,
        name: u.nickname || u.username,
        avatar: u.avatar || '',
        signature: u.signature || '',
        followers: fcRes.status === 'fulfilled' ? fcRes.value.data : 0,
        isFollowing: cfRes.status === 'fulfilled' ? cfRes.value.data : false
      }
    }))
    searchResults.posts = (data.posts || []).map(p => ({
      id: p.postId || p.id,
      nickname: p.nickname || p.username,
      avatar: p.avatar || '',
      username: p.username,
      content: p.content,
      likeCount: p.likeCount || 0,
      commentCount: p.commentCount || 0,
      createTime: p.createTime || ''
    }))
    searchResults.topics = (data.topics || []).map(t => ({
      id: t.topic,
      name: t.topic,
      count: t.postCount || 0
    }))
  } catch (e) {
    console.error('搜索失败', e)
  }
}

const clearSearch = () => {
  searchText.value = ''
  searchResults.users = []
  searchResults.posts = []
  searchResults.topics = []
}

const clearHistory = async () => {
  try {
    await clearHistoryApi()
    searchHistory.value = []
  } catch (e) { console.error(e) }
}

const highlightText = (text) => {
  if (!searchText.value) return text
  const regex = new RegExp(`(${searchText.value})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

const goToProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

const goToPost = (post) => {
  router.push('/post')
}

const goToTopic = (topic) => {
  searchText.value = topic.name
  doSearch()
  activeTab.value = 'posts'
}

const toggleFollow = async (user) => {
  try {
    if (user.isFollowing) {
      await unfollow(user.id)
      user.isFollowing = false
      ElMessage.success('已取消关注')
    } else {
      await follow(user.id)
      user.isFollowing = true
      ElMessage.success('关注成功')
    }
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.search-page {
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.search-header {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 18px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  transition: all 0.25s ease;
}

.search-box.focused {
  border-color: rgba(255, 107, 107, 0.4);
  box-shadow: 0 0 0 4px rgba(255, 107, 107, 0.06);
}

.search-icon {
  color: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

.clear-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: none;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
}

.search-content {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 18px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.result-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.tab-btn {
  padding: 8px 18px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.4);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.7);
}

.tab-btn.active {
  background: rgba(255, 107, 107, 0.12);
  color: #FF6B6B;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 4px 0;
}

.user-info p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-meta {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.25);
}

.follow-btn {
  padding: 8px 20px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.follow-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%);
  border: none;
  color: white;
}

.follow-btn.following {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.post-item {
  padding: 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  flex-direction: column;
}

.post-author {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.post-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

.post-content p {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 12px 0;
}

.post-content :deep(mark) {
  background: rgba(255, 107, 107, 0.2);
  color: #FF6B6B;
  padding: 0 2px;
  border-radius: 2px;
}

.post-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.topic-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.topic-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topic-info {
  flex: 1;
}

.topic-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 4px 0;
}

.topic-info p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
  margin: 0;
}

.arrow-icon {
  color: rgba(255, 255, 255, 0.15);
}

.empty-result {
  text-align: center;
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.25);
}

.section {
  margin-bottom: 28px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.clear-history {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.3);
  font-size: 13px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.clear-history:hover {
  background: rgba(255, 255, 255, 0.04);
  color: #FF6B6B;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #FF6B6B;
  border-color: rgba(255, 107, 107, 0.2);
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.hot-rank {
  width: 24px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.25);
  text-align: center;
}

.hot-item.hot .hot-rank {
  color: #FF6B6B;
}

.hot-text {
  flex: 1;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.hot-badge {
  padding: 2px 8px;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 6px;
}
</style>
