<template>
  <div class="friends-page" ref="pageRef">
    <div class="tabs-header">
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <span>{{ tab.label }}</span>
          <span v-if="tab.count" class="tab-count">{{ tab.count }}</span>
        </button>
      </div>
    </div>

    <div class="content-area">
      <template v-if="activeTab === 'friends'">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input type="text" placeholder="搜索好友..." v-model="searchText" />
        </div>

        <div class="friend-list" v-if="filteredFriends.length">
          <div
            v-for="friend in filteredFriends"
            :key="friend.id"
            class="friend-card"
          >
            <div class="friend-avatar">
              <el-avatar :size="52" :src="friend.avatar" style="cursor:pointer" @click="goToProfile(friend.id)">
                {{ friend.name?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="friend-info">
              <h4 class="friend-name">{{ friend.name }}</h4>
              <p class="friend-signature">{{ friend.signature || '暂无签名' }}</p>
            </div>
            <div class="friend-actions">
              <button class="action-btn" @click="goToChat(friend)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </button>
              <button class="action-btn" @click="goToProfile(friend.id)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
          </svg>
          <p>暂无好友</p>
        </div>
      </template>

      <template v-if="activeTab === 'following'">
        <div class="user-list" v-if="followingList.length">
          <div v-for="user in followingList" :key="user.id" class="user-card">
            <el-avatar :size="48" :src="user.avatar" @click="goToProfile(user.id)">
              {{ user.name?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="user-info">
              <h4>{{ user.name }}</h4>
              <p>{{ user.signature || '暂无签名' }}</p>
            </div>
            <button
              class="follow-btn"
              :class="{ following: user.isFollowing }"
              @click="toggleFollow(user)"
            >
              {{ user.isFollowing ? '已关注' : '关注' }}
            </button>
          </div>
        </div>
        <div v-else class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
          </svg>
          <p>暂无关注</p>
        </div>
      </template>

      <template v-if="activeTab === 'followers'">
        <div class="user-list" v-if="followersList.length">
          <div v-for="user in followersList" :key="user.id" class="user-card">
            <el-avatar :size="48" :src="user.avatar" @click="goToProfile(user.id)">
              {{ user.name?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="user-info">
              <h4>{{ user.name }}</h4>
              <p>{{ user.signature || '暂无签名' }}</p>
            </div>
            <button
              class="follow-btn"
              :class="{ following: user.isFollowing }"
              @click="toggleFollow(user)"
            >
              {{ user.isFollowing ? '互相关注' : '回关' }}
            </button>
          </div>
        </div>
        <div v-else class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
          </svg>
          <p>暂无粉丝</p>
        </div>
      </template>

      <template v-if="activeTab === 'requests'">
        <div class="request-list">
          <div v-for="request in friendRequests" :key="request.id" class="request-card">
            <el-avatar :size="48" :src="request.avatar" style="cursor:pointer" @click="goToProfile(request.id)">
              {{ request.name?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="request-info">
              <h4>{{ request.name }}</h4>
              <p>{{ request.message || '请求添加你为好友' }}</p>
              <span class="request-time">{{ request.time }}</span>
            </div>
            <div class="request-actions">
              <button class="accept-btn" @click="acceptRequest(request)">接受</button>
              <button class="reject-btn" @click="rejectRequest(request)">拒绝</button>
            </div>
          </div>

          <div v-if="friendRequests.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            <p>暂无好友申请</p>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getFriendList, getPendingRequests, acceptFriendRequest, rejectFriendRequest } from '../api/social'
import { getFollowingList, getFollowerList, follow, unfollow, checkFollowing } from '../api/social'
import { getUserInfo } from '../api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const pageRef = ref(null)
const activeTab = ref('friends')
const searchText = ref('')
const loading = ref(false)

const friends = ref([])
const followingList = ref([])
const followersList = ref([])
const friendRequests = ref([])

const tabs = computed(() => [
  { key: 'friends', label: '好友', count: friends.value.length },
  { key: 'following', label: '关注', count: followingList.value.length },
  { key: 'followers', label: '粉丝', count: followersList.value.length },
  { key: 'requests', label: '申请', count: friendRequests.value.length }
])

const filteredFriends = computed(() => {
  if (!searchText.value) return friends.value
  return friends.value.filter(friend => friend.name.includes(searchText.value))
})

let animCtx = null

onMounted(() => {
  loadAllData()

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return

      animCtx = gsap.context(() => {
        gsap.fromTo('.tabs-header', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, ease: 'power3.out' })
        gsap.fromTo('.content-area', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, delay: 0.15, ease: 'power3.out' })
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})

// 路由变化时重新加载数据
watch(() => route.path, (newPath) => {
  if (newPath === '/friends') {
    loadAllData()
  }
})

async function loadAllData() {
  loading.value = true
  try {
    await Promise.allSettled([
      loadFriends(),
      loadPendingRequests(),
      loadFollowing(),
      loadFollowers()
    ])
  } finally {
    loading.value = false
  }
}

const loadFriends = async () => {
  try {
    const res = await getFriendList()
    const friendIds = (res.data || []).map(f => f.friendId || f.id).filter(Boolean)
    if (friendIds.length === 0) { friends.value = []; return }
    const friendDetails = await Promise.allSettled(friendIds.map(async (id) => {
      try {
        const uRes = await getUserInfo(id)
        const u = uRes.data
        if (!u || !u.userId) return { id, name: '用户' + id, avatar: '', signature: '' }
        return {
          id: u.userId,
          name: u.nickname || u.username || '用户' + id,
          avatar: u.avatar || '',
          signature: u.signature || ''
        }
      } catch {
        return { id, name: '用户' + id, avatar: '', signature: '' }
      }
    }))
    friends.value = friendDetails
      .filter(r => r.status === 'fulfilled')
      .map(r => r.value)
  } catch (e) { console.error('加载好友列表失败', e) }
}

const loadPendingRequests = async () => {
  try {
    const res = await getPendingRequests()
    const requests = res.data || []
    if (requests.length === 0) return
    const requestDetails = await Promise.allSettled(requests.map(async (r) => {
      const id = r.userId || r.friendId || r.id
      try {
        const uRes = await getUserInfo(id)
        const u = uRes.data
        return {
          id: u.userId || u.id,
          name: u.nickname || u.username || '用户',
          avatar: u.avatar || '',
          message: '请求添加你为好友',
          time: r.createTime || ''
        }
      } catch {
        return { id, name: '用户', avatar: '', message: '请求添加你为好友', time: r.createTime || '' }
      }
    }))
    friendRequests.value = requestDetails
      .filter(r => r.status === 'fulfilled')
      .map(r => r.value)
  } catch (e) { console.error('加载好友申请失败', e) }
}

const loadFollowing = async () => {
  try {
    const userId = userStore.userInfo.userId || userStore.userInfo.id
    if (!userId) return
    const res = await getFollowingList(userId)
    const ids = (res.data || []).filter(Boolean)
    if (ids.length === 0) { followingList.value = []; return }
    const details = await Promise.allSettled(ids.map(async id => {
      try {
        const uRes = await getUserInfo(id)
        const u = uRes.data
        if (!u || !u.userId) return { id, name: '用户' + id, avatar: '', signature: '', isFollowing: true }
        return { id: u.userId, name: u.nickname || u.username || '用户' + id, avatar: u.avatar || '', signature: u.signature || '', isFollowing: true }
      } catch { return { id, name: '用户' + id, avatar: '', signature: '', isFollowing: true } }
    }))
    followingList.value = details.filter(r => r.status === 'fulfilled').map(r => r.value)
  } catch (e) { console.error(e) }
}

const loadFollowers = async () => {
  try {
    const userId = userStore.userInfo.userId || userStore.userInfo.id
    if (!userId) return
    const res = await getFollowerList(userId)
    const ids = (res.data || []).filter(Boolean)
    if (ids.length === 0) { followersList.value = []; return }
    const details = await Promise.allSettled(ids.map(async id => {
      try {
        const [uRes, followRes] = await Promise.all([
          getUserInfo(id).catch(() => ({ data: null })),
          checkFollowing(id).catch(() => ({ data: false }))
        ])
        const u = uRes.data
        if (!u || !u.userId) return { id, name: '用户' + id, avatar: '', signature: '', isFollowing: followRes.data || false }
        return { id: u.userId, name: u.nickname || u.username || '用户' + id, avatar: u.avatar || '', signature: u.signature || '', isFollowing: followRes.data || false }
      } catch { return { id, name: '用户' + id, avatar: '', signature: '', isFollowing: false } }
    }))
    followersList.value = details.filter(r => r.status === 'fulfilled').map(r => r.value)
  } catch (e) { console.error(e) }
}

const goToChat = (friend) => {
  router.push({ path: '/message', query: { userId: friend.id } })
}

const goToProfile = (userId) => {
  router.push(`/profile/${userId}`)
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

const acceptRequest = async (request) => {
  try {
    await acceptFriendRequest(request.id)
    friendRequests.value = friendRequests.value.filter(r => r.id !== request.id)
    friends.value.push({ id: request.id, name: request.name, avatar: request.avatar, signature: '' })
    ElMessage.success('已接受好友申请')
  } catch (e) { console.error(e) }
}

const rejectRequest = async (request) => {
  try {
    await rejectFriendRequest(request.id)
    friendRequests.value = friendRequests.value.filter(r => r.id !== request.id)
    ElMessage.success('已拒绝好友申请')
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.friends-page {
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.tabs-header {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 18px;
  padding: 16px 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.4);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.25s ease;
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

.tab-count {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.tab-btn:not(.active) .tab-count {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.3);
}

.tab-btn.active .tab-count {
  background: rgba(255, 107, 107, 0.2);
  color: #FF6B6B;
}

.content-area {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 18px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  margin-bottom: 24px;
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
  font-size: 14px;
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

/* 好友列表 */
.friend-list, .user-list, .request-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-card, .user-card, .request-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 14px;
  transition: all 0.2s ease;
}

.friend-card:hover, .user-card:hover {
  background: rgba(255, 255, 255, 0.03);
}

.friend-avatar {
  position: relative;
  flex-shrink: 0;
}

.friend-info, .user-info, .request-info {
  flex: 1;
  min-width: 0;
}

.friend-name, .user-info h4, .request-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 4px 0;
}

.friend-signature, .user-info p, .request-info p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.35);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.request-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.2);
}

.friend-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
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

.action-btn:hover {
  background: rgba(255, 107, 107, 0.15);
  color: #FF6B6B;
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

.request-actions {
  display: flex;
  gap: 8px;
}

.accept-btn, .reject-btn {
  padding: 8px 18px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.accept-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%);
  border: none;
  color: white;
}

.reject-btn {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.5);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: rgba(255, 255, 255, 0.25);
}

.empty-state p {
  margin-top: 14px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .tabs {
    overflow-x: auto;
  }

  .tab-btn {
    white-space: nowrap;
  }
}
</style>
