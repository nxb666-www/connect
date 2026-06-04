<template>
  <div class="profile-page" ref="pageRef">
    <div class="profile-header">
      <div class="cover-image">
        <div class="cover-overlay"></div>
        <div class="cover-pattern"></div>
      </div>
      <div class="profile-info">
        <div class="avatar-section">
          <div class="avatar-ring-large">
            <el-avatar :size="96" :src="userInfo.avatar" class="profile-avatar">
              {{ userInfo.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <button v-if="isOwn" class="edit-avatar-btn" @click="triggerAvatarUpload">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/>
            </svg>
          </button>
        </div>
        <div class="user-details">
          <h1 class="username">{{ userInfo.nickname || '用户' }}</h1>
          <p class="user-handle">@{{ userInfo.username }}</p>
          <p class="user-bio">{{ userInfo.signature || '这个人很懒，什么都没留下～' }}</p>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.postCount || 0 }}</span>
              <span class="stat-label">动态</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.followingCount || 0 }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.followersCount || 0 }}</span>
              <span class="stat-label">粉丝</span>
            </div>
          </div>
        </div>
        <div class="action-buttons">
          <button v-if="isOwn" class="edit-btn" @click="showEditDialog = true">
            编辑资料
          </button>
          <template v-else>
            <button class="follow-btn" :class="{ following: isFollowing }" @click="handleFollow">
              {{ isFollowing ? '已关注' : '关注' }}
            </button>
            <button class="message-btn" @click="goToMessage">
              发消息
            </button>
          </template>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <div class="tabs-header">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="tab-content">
        <div v-if="activeTab === 'posts'" class="post-list">
          <div v-for="post in userPosts" :key="post.id" class="post-card">
            <div class="post-header">
              <el-avatar :size="36" :src="userInfo.avatar" class="post-avatar">
                {{ userInfo.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="post-meta">
                <span class="post-author">{{ userInfo.nickname }}</span>
                <span class="post-time">{{ post.createTime }}</span>
              </div>
            </div>
            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>
            <div class="post-actions">
              <button class="action-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ post.likeCount }}</span>
              </button>
              <button class="action-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span>{{ post.commentCount }}</span>
              </button>
              <button v-if="isOwn" class="action-btn delete-btn" @click="handleDeletePost(post)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
                <span>删除</span>
              </button>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'collections'" class="post-list">
          <div v-for="post in collectedPosts" :key="post.id" class="post-card">
            <div class="post-header">
              <el-avatar :size="36" :src="post.authorAvatar" class="post-avatar">
                {{ post.authorName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="post-meta">
                <span class="post-author">{{ post.authorName }}</span>
                <span class="post-time">{{ post.createTime }}</span>
              </div>
            </div>
            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>
            <div class="post-actions">
              <button class="action-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ post.likeCount }}</span>
              </button>
              <button class="action-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span>{{ post.commentCount }}</span>
              </button>
            </div>
          </div>
          <div v-if="collectedPosts.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
            </svg>
            <p>暂无收藏内容</p>
          </div>
        </div>

        <div v-if="activeTab === 'likes'" class="post-list">
          <div v-for="post in likedPosts" :key="post.id" class="post-card">
            <div class="post-header">
              <el-avatar :size="36" :src="post.authorAvatar" class="post-avatar">
                {{ post.authorName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="post-meta">
                <span class="post-author">{{ post.authorName }}</span>
                <span class="post-time">{{ post.createTime }}</span>
              </div>
            </div>
            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>
            <div class="post-actions">
              <button class="action-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ post.likeCount }}</span>
              </button>
              <button class="action-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span>{{ post.commentCount }}</span>
              </button>
            </div>
          </div>
          <div v-if="likedPosts.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <p>暂无点赞内容</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showEditDialog"
      title="编辑个人资料"
      width="500px"
      class="edit-dialog"
    >
      <el-form :model="editForm" label-position="top">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="editForm.signature" type="textarea" :rows="3" placeholder="介绍一下自己..." />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="dialog-btn cancel" @click="showEditDialog = false">取消</button>
        <button class="dialog-btn confirm" @click="handleSave">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getUserInfo, updateUser, uploadAvatar } from '../api/user'
import { getUserPosts, getUserCollectedPosts, getUserLikedPosts, deletePost } from '../api/post'
import { follow, unfollow, checkFollowing, getFollowingCount, getFollowerCount } from '../api/social'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const pageRef = ref(null)

const activeTab = ref('posts')
const showEditDialog = ref(false)
const isFollowing = ref(false)

const tabs = [
  { key: 'posts', label: '动态' },
  { key: 'collections', label: '收藏' },
  { key: 'likes', label: '点赞' }
]

const userInfo = ref({
  userId: null,
  username: '',
  nickname: '用户',
  avatar: '',
  signature: '',
  phone: '',
  email: '',
  level: 1,
  points: 0,
  postCount: 0,
  followingCount: 0,
  followersCount: 0
})

const editForm = reactive({
  nickname: '',
  signature: '',
  phone: '',
  email: ''
})

const userPosts = ref([])
const collectedPosts = ref([])
const likedPosts = ref([])

const isOwn = computed(() => {
  const myId = userStore.userInfo.userId || userStore.userInfo.id
  return !route.params.userId || route.params.userId == myId
})

let animCtx = null

onMounted(() => {
  loadPageData()

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return

      animCtx = gsap.context(() => {
        gsap.fromTo('.profile-header', { y: 30, opacity: 0 }, { y: 0, opacity: 1, duration: 0.6, ease: 'power3.out' })
        gsap.fromTo('.profile-content', { y: 30, opacity: 0 }, { y: 0, opacity: 1, duration: 0.6, delay: 0.2, ease: 'power3.out' })
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})

// 路由参数变化时重新加载数据
watch(() => route.params.userId, () => {
  loadPageData()
})

// 进入个人主页时也刷新数据（解决点赞/收藏后不更新的问题）
watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/profile')) {
    loadPageData()
  }
})

const loadPageData = () => {
  const userId = route.params.userId || userStore.userInfo.userId || userStore.userInfo.id
  if (userId) {
    loadUserInfo(userId)
  } else {
    userInfo.value = { ...userInfo.value, ...userStore.userInfo }
  }
}

const loadUserInfo = async (userId) => {
  try {
    const res = await getUserInfo(userId)
    const u = res.data
    userInfo.value = {
      ...userInfo.value,
      id: u.userId,
      username: u.username,
      nickname: u.nickname || u.username,
      avatar: u.avatar || '',
      signature: u.signature || '',
      phone: u.phone || '',
      email: u.email || '',
      level: u.level || 1,
      points: u.points || 0
    }
    editForm.nickname = userInfo.value.nickname
    editForm.signature = userInfo.value.signature
    editForm.phone = userInfo.value.phone
    editForm.email = userInfo.value.email

    const [fRes, cRes] = await Promise.all([
      getFollowingCount(userId).catch(() => ({ data: 0 })),
      getFollowerCount(userId).catch(() => ({ data: 0 }))
    ])
    userInfo.value.followingCount = fRes.data || 0
    userInfo.value.followersCount = cRes.data || 0

    const pRes = await getUserPosts(userId, 1, 20).catch(() => ({ data: { records: [] } }))
    userPosts.value = (pRes.data?.records || []).map(p => ({
      id: p.id,
      content: p.content,
      likeCount: p.likeCount || 0,
      commentCount: p.commentCount || 0,
      createTime: p.createTime || ''
    }))
    userInfo.value.postCount = userPosts.value.length

    // 加载收藏和点赞动态
    const [collectedRes, likedRes] = await Promise.all([
      getUserCollectedPosts(userId, 1, 20).catch(() => ({ data: { records: [] } })),
      getUserLikedPosts(userId, 1, 20).catch(() => ({ data: { records: [] } }))
    ])
    collectedPosts.value = (collectedRes.data?.records || []).map(p => ({
      id: p.id,
      content: p.content,
      likeCount: p.likeCount || 0,
      commentCount: p.commentCount || 0,
      createTime: p.createTime || '',
      authorName: p.nickname || p.username || '用户',
      authorAvatar: p.avatar || ''
    }))
    likedPosts.value = (likedRes.data?.records || []).map(p => ({
      id: p.id,
      content: p.content,
      likeCount: p.likeCount || 0,
      commentCount: p.commentCount || 0,
      createTime: p.createTime || '',
      authorName: p.nickname || p.username || '用户',
      authorAvatar: p.avatar || ''
    }))

    if (!isOwn.value) {
      const cRes2 = await checkFollowing(userId).catch(() => ({ data: false }))
      isFollowing.value = cRes2.data || false
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

const handleFollow = async () => {
  try {
    const userId = route.params.userId
    if (isFollowing.value) {
      await unfollow(userId)
      isFollowing.value = false
      userInfo.value.followersCount--
      ElMessage.success('已取消关注')
    } else {
      await follow(userId)
      isFollowing.value = true
      userInfo.value.followersCount++
      ElMessage.success('关注成功')
    }
  } catch (e) { console.error(e) }
}

const goToMessage = () => {
  router.push('/message')
}

const handleDeletePost = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePost(post.id)
    userPosts.value = userPosts.value.filter(p => p.id !== post.id)
    userInfo.value.postCount = userPosts.value.length
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleSave = async () => {
  try {
    await updateUser({
      nickname: editForm.nickname,
      signature: editForm.signature,
      phone: editForm.phone,
      email: editForm.email
    })
    userInfo.value.nickname = editForm.nickname
    userInfo.value.signature = editForm.signature
    userInfo.value.phone = editForm.phone
    userInfo.value.email = editForm.email
    userStore.userInfo.nickname = editForm.nickname
    showEditDialog.value = false
    ElMessage.success('保存成功')
  } catch (e) { console.error(e) }
}

const triggerAvatarUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    try {
      ElMessage.info('上传中...')
      const res = await uploadAvatar(file)
      const url = res.data.url
      userInfo.value.avatar = url
      userStore.userInfo.avatar = url
      ElMessage.success('头像更新成功')
    } catch (e) {
      console.error(e)
      ElMessage.error('头像上传失败')
    }
  }
  input.click()
}
</script>

<style scoped>
.profile-page {
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.profile-header {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.cover-image {
  height: 200px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 50%, #FFD93D 100%);
  position: relative;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(15, 15, 20, 0.8) 100%);
}

.cover-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.06'/%3E%3C/svg%3E");
  opacity: 0.5;
}

.profile-info {
  padding: 0 28px 28px;
  position: relative;
}

.avatar-section {
  position: relative;
  margin-top: -50px;
  margin-bottom: 18px;
  display: inline-block;
}

.avatar-ring-large {
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  display: inline-block;
}

.profile-avatar {
  border: 3px solid #16161e;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-size: 34px;
  font-weight: 700;
}

.edit-avatar-btn {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #16161e;
  background: #FF6B6B;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.edit-avatar-btn:hover {
  transform: scale(1.1);
}

.user-details {
  margin-bottom: 18px;
}

.username {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 4px 0;
}

.user-handle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.35);
  margin: 0 0 10px 0;
}

.user-bio {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.55);
  margin: 0 0 18px 0;
  line-height: 1.6;
}

.user-stats {
  display: flex;
  gap: 28px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
}

.action-buttons {
  position: absolute;
  top: 16px;
  right: 28px;
  display: flex;
  gap: 10px;
}

.edit-btn, .follow-btn, .message-btn {
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.edit-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%);
  border: none;
  color: white;
}

.edit-btn:hover {
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.3);
}

.follow-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%);
  border: none;
  color: white;
}

.follow-btn.following {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.message-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
}

.message-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* 内容标签页 */
.profile-content {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tabs-header {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.tab-btn {
  padding: 10px 20px;
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
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
}

/* 动态列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  padding: 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.04);
  transition: all 0.2s ease;
}

.post-card:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.08);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-avatar {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-weight: 600;
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
  color: rgba(255, 255, 255, 0.65);
  margin: 0 0 12px 0;
}

.post-actions {
  display: flex;
  gap: 18px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.35);
  font-size: 13px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  color: #FF6B6B;
}

.delete-btn {
  margin-left: auto;
  color: rgba(255, 255, 255, 0.3);
}

.delete-btn:hover {
  color: #ff4d4f;
  background: rgba(255, 77, 77, 0.1);
}

/* 空状态 */
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

/* 编辑对话框 */
.edit-dialog :deep(.el-dialog) {
  background: rgba(22, 22, 30, 0.95);
  backdrop-filter: blur(40px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
}

.edit-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.edit-dialog :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.6);
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.edit-dialog :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: none !important;
}

.edit-dialog :deep(.el-input__inner) {
  color: #fff;
}

.edit-dialog :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #fff;
  box-shadow: none !important;
}

.dialog-btn {
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.dialog-btn.cancel {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
}

.dialog-btn.confirm {
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%);
  border: none;
  color: white;
}

@media (max-width: 768px) {
  .profile-info {
    padding: 0 16px 16px;
  }

  .action-buttons {
    position: static;
    margin-top: 16px;
  }

  .user-stats {
    justify-content: center;
  }
}
</style>
