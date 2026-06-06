<template>
  <div class="post-page" ref="pageRef">
    <!-- 发布入口 -->
    <div class="publish-bar">
      <div class="publish-input" @click="showPublishDialog = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
        <span>分享你的生活...</span>
      </div>
    </div>

    <!-- 瀑布流动态列表 -->
    <div class="waterfall">
      <div v-for="post in postList" :key="post.id" class="note-card" @click="showPostDetail(post)">
        <!-- 封面图 -->
        <div class="note-cover">
          <img v-if="post.images?.length" :src="post.images[0]" alt="动态图片" />
          <div v-else class="cover-placeholder">
            <span>{{ post.content?.substring(0, 50) }}</span>
          </div>
        </div>
        <!-- 标题/内容 -->
        <div class="note-info">
          <p class="note-title">{{ post.content?.substring(0, 30) }}{{ post.content?.length > 30 ? '...' : '' }}</p>
          <div class="note-meta">
            <div class="note-author">
              <el-avatar :size="20" :src="post.avatar" @click.stop="router.push(`/profile/${post.userId}`)">{{ post.nickname?.charAt(0) || 'U' }}</el-avatar>
              <span>{{ post.nickname || post.username }}</span>
            </div>
            <div class="note-actions-right">
              <button v-if="isOwnPost(post)" class="delete-btn-sm" @click.stop="handleDelete(post)" title="删除">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
              <div class="note-likes" @click.stop="handleLike(post)">
                <svg width="14" height="14" viewBox="0 0 24 24" :fill="post.isLiked ? '#FF6B6B' : 'none'" :stroke="post.isLiked ? '#FF6B6B' : 'currentColor'" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ post.likeCount || 0 }}</span>
              </div>
              <div class="note-collect" @click.stop="handleCollect(post)">
                <svg width="14" height="14" viewBox="0 0 24 24" :fill="post.isCollected ? '#FFD93D' : 'none'" :stroke="post.isCollected ? '#FFD93D' : 'currentColor'" stroke-width="2">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore">
      <button class="load-more-btn" :class="{ loading: loading }" @click="loadMore">
        <span v-if="!loading">加载更多</span>
        <span v-else class="loading-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </span>
      </button>
    </div>

    <!-- 动态详情弹窗 -->
    <el-dialog v-model="showDetail" width="900px" class="detail-dialog" :close-on-click-modal="true">
      <div class="detail-content" v-if="currentPost">
        <!-- 左侧图片 -->
        <div class="detail-images">
          <div v-if="currentPost.images?.length" class="image-carousel">
            <img :src="currentPost.images[currentImageIndex]" alt="动态图片" />
            <div v-if="currentPost.images.length > 1" class="image-dots">
              <span v-for="(_, idx) in currentPost.images" :key="idx" :class="{ active: idx === currentImageIndex }" @click="currentImageIndex = idx"></span>
            </div>
          </div>
          <div v-else class="no-image">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
            </svg>
          </div>
        </div>
        <!-- 右侧信息 -->
        <div class="detail-info">
          <div class="detail-header">
            <el-avatar :size="40" :src="currentPost.avatar" style="cursor:pointer" @click="router.push(`/profile/${currentPost.userId}`)">{{ currentPost.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="detail-author">
              <span class="author-name">{{ currentPost.nickname || currentPost.username }}</span>
              <span class="post-time">{{ currentPost.createTime }}</span>
            </div>
          </div>
          <div class="detail-text">
            <p>{{ currentPost.content }}</p>
            <div v-if="currentPost.topic" class="detail-topic">#{{ currentPost.topic }}#</div>
          </div>
          <div class="detail-actions">
            <button class="action-btn" :class="{ liked: currentPost.isLiked }" @click="handleLike(currentPost)">
              <svg width="18" height="18" viewBox="0 0 24 24" :fill="currentPost.isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <span>{{ currentPost.likeCount || 0 }}</span>
            </button>
            <button class="action-btn" :class="{ collected: currentPost.isCollected }" @click="handleCollect(currentPost)">
              <svg width="18" height="18" viewBox="0 0 24 24" :fill="currentPost.isCollected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
              </svg>
              <span>{{ currentPost.collectCount || 0 }}</span>
            </button>
            <button class="action-btn" @click="handleShare(currentPost)">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
            </button>
            <button v-if="isOwnPost(currentPost)" class="action-btn delete-btn" @click="handleDelete(currentPost)">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
              <span>删除</span>
            </button>
          </div>
          <!-- 评论区 -->
          <div class="detail-comments">
            <div class="comment-header">
              <span>评论 {{ currentPost.commentCount || 0 }}</span>
            </div>
            <div class="comment-input">
              <el-input v-model="commentText" placeholder="说点什么..." size="small" @keydown.enter="submitComment" />
              <button class="comment-submit" :disabled="!commentText.trim()" @click="submitComment">发送</button>
            </div>
            <div class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <el-avatar :size="28" :src="comment.userAvatar" style="cursor:pointer" @click.stop="comment.userId && router.push(`/profile/${comment.userId}`)">{{ comment.userName?.charAt(0) || 'U' }}</el-avatar>
                <div class="comment-content">
                  <span class="comment-author">{{ comment.userName }}</span>
                  <span class="comment-text">{{ comment.content }}</span>
                  <span class="comment-time">{{ comment.createTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 发布对话框 -->
    <el-dialog v-model="showPublishDialog" title="发布动态" width="600px" class="publish-dialog">
      <div class="publish-form">
        <el-input v-model="newPost.content" type="textarea" :rows="5" placeholder="分享你的生活..." class="publish-textarea" />
        <div v-if="newPost.images.length" class="image-preview-list">
          <div v-for="(img, idx) in newPost.images" :key="idx" class="image-preview-item">
            <img :src="img" alt="预览" />
            <button class="remove-img" @click="newPost.images.splice(idx, 1)">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
        </div>
        <el-input v-if="showTopicInput" v-model="newPost.topic" placeholder="添加话题标签（可选）" class="topic-input" />
        <div class="publish-footer">
          <div class="publish-tools">
            <button class="tool-btn" @click="triggerImageUpload">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
              </svg>
              <span>图片</span>
            </button>
            <button class="tool-btn" @click="triggerVideoUpload">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
              </svg>
              <span>视频</span>
            </button>
            <button class="tool-btn" @click="showTopicInput = !showTopicInput">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 11a9 9 0 0 1 9 9"/><path d="M4 4a16 16 0 0 1 16 16"/><circle cx="5" cy="19" r="1"/>
              </svg>
              <span>话题</span>
            </button>
          </div>
          <button class="publish-btn" :disabled="!newPost.content.trim() || publishing" @click="handlePublish">
            {{ publishing ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getPostFeed, createPost, deletePost, uploadImage, likePost, unlikePost, collectPost, uncollectPost, getPostComments, createComment } from '../api/post'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const pageRef = ref(null)

const postList = ref([])
const publishing = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const loading = ref(false)
const showPublishDialog = ref(false)
const showTopicInput = ref(false)
const showDetail = ref(false)
const currentPost = ref(null)
const currentImageIndex = ref(0)
const comments = ref([])
const commentText = ref('')

const newPost = reactive({
  content: '',
  images: [],
  topic: ''
})

let animCtx = null

onMounted(() => {
  loadPosts()

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return

      animCtx = gsap.context(() => {
        gsap.fromTo('.publish-bar', { y: -20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.4, ease: 'power3.out' })
        gsap.fromTo('.note-card', { y: 30, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, stagger: 0.08, delay: 0.2, ease: 'power2.out' })
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})

const loadPosts = async () => {
  try {
    const res = await getPostFeed(1, 20)
    const page = res.data
    postList.value = (page.records || []).map(p => ({
      ...p,
      userAvatar: p.avatar || '',
      userName: p.nickname || p.username
    }))
    pageNum.value = 1
    hasMore.value = page.current < page.pages
  } catch (e) {
    console.error('加载动态失败', e)
  }
}

const loadMore = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getPostFeed(pageNum.value + 1, 20)
    const page = res.data
    postList.value.push(...(page.records || []).map(p => ({
      ...p,
      userAvatar: p.avatar || '',
      userName: p.nickname || p.username
    })))
    pageNum.value = page.current
    hasMore.value = page.current < page.pages
  } catch (e) {
    console.error('加载更多失败', e)
  } finally {
    loading.value = false
  }
}

const showPostDetail = async (post) => {
  currentPost.value = { ...post }
  currentImageIndex.value = 0
  showDetail.value = true
  comments.value = []
  try {
    const res = await getPostComments(post.id)
    comments.value = (res.data || []).map(c => ({
      ...c,
      userName: c.nickname || c.username,
      userAvatar: c.avatar || ''
    }))
  } catch (e) { console.error(e) }
}

const handleLike = async (post) => {
  const wasLiked = post.isLiked
  try {
    if (wasLiked) {
      await unlikePost(post.id)
      post.isLiked = false
      post.likeCount = Math.max((post.likeCount || 1) - 1, 0)
    } else {
      await likePost(post.id)
      post.isLiked = true
      post.likeCount = (post.likeCount || 0) + 1
    }
    if (currentPost.value?.id === post.id) {
      currentPost.value.isLiked = post.isLiked
      currentPost.value.likeCount = post.likeCount
    }
  } catch (e) {
    post.isLiked = wasLiked
    if (wasLiked) post.likeCount = (post.likeCount || 0) + 1
    else post.likeCount = Math.max((post.likeCount || 1) - 1, 0)
    ElMessage.error('点赞操作失败，请重试')
  }
}

const handleCollect = async (post) => {
  const wasCollected = post.isCollected
  try {
    if (wasCollected) {
      await uncollectPost(post.id)
      post.isCollected = false
      post.collectCount = Math.max((post.collectCount || 1) - 1, 0)
      ElMessage.success('已取消收藏')
    } else {
      await collectPost(post.id)
      post.isCollected = true
      post.collectCount = (post.collectCount || 0) + 1
      ElMessage.success('已收藏')
    }
    if (currentPost.value?.id === post.id) {
      currentPost.value.isCollected = post.isCollected
      currentPost.value.collectCount = post.collectCount
    }
  } catch (e) {
    post.isCollected = wasCollected
    if (wasCollected) post.collectCount = (post.collectCount || 0) + 1
    else post.collectCount = Math.max((post.collectCount || 1) - 1, 0)
    ElMessage.error('收藏操作失败，请重试')
  }
}

const handleShare = async (post) => {
  try {
    await navigator.clipboard.writeText(`${window.location.origin}/post/${post.id}`)
  } catch {
    const input = document.createElement('input')
    input.value = `${window.location.origin}/post/${post.id}`
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
  }
  ElMessage.success('链接已复制')
}

const isOwnPost = (post) => {
  const myId = userStore.userInfo.userId || userStore.userInfo.id
  return post.userId == myId
}

const handleDelete = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePost(post.id)
    postList.value = postList.value.filter(p => p.id !== post.id)
    if (currentPost.value?.id === post.id) {
      showDetail.value = false
    }
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const submitComment = async () => {
  if (!commentText.value.trim() || !currentPost.value) return
  try {
    await createComment({ postId: currentPost.value.id, content: commentText.value })
    comments.value.unshift({
      id: Date.now(),
      userName: userStore.userInfo.nickname || '我',
      userAvatar: userStore.userInfo.avatar,
      content: commentText.value,
      createTime: '刚刚'
    })
    currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
    commentText.value = ''
  } catch (e) { console.error(e) }
}

const triggerImageUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.multiple = true
  input.onchange = async (e) => {
    const files = Array.from(e.target.files)
    if (files.length === 0) return
    ElMessage.info(`正在上传 ${files.length} 张图片...`)
    for (const file of files) {
      try {
        const res = await uploadImage(file)
        newPost.images.push(res.data.url)
      } catch (err) {
        console.error('图片上传失败', err)
        ElMessage.error(`${file.name} 上传失败`)
      }
    }
    ElMessage.success('图片上传完成')
  }
  input.click()
}

const triggerVideoUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'video/*'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    ElMessage.info('正在上传视频...')
    try {
      const res = await uploadImage(file)
      newPost.images.push(res.data.url)
      ElMessage.success('视频上传完成')
    } catch (err) {
      console.error('视频上传失败', err)
      ElMessage.error('视频上传失败')
    }
  }
  input.click()
}

const handlePublish = async () => {
  if (!newPost.content.trim()) return
  publishing.value = true
  try {
    await createPost({
      content: newPost.content,
      images: newPost.images,
      topic: newPost.topic || undefined
    })
    ElMessage.success('发布成功')
    showPublishDialog.value = false
    newPost.content = ''
    newPost.topic = ''
    newPost.images = []
    loadPosts()
  } catch (error) {
    console.error(error)
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.post-page {
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
  width: 100%;
  padding-bottom: 40px;
}

/* 发布入口 */
.publish-bar {
  margin-bottom: 20px;
}

.publish-input {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
  color: rgba(255, 255, 255, 0.35);
  font-size: 14px;
}

.publish-input:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 107, 107, 0.2);
}

/* 瀑布流 */
.waterfall {
  column-count: 2;
  column-gap: 16px;
}

.note-card {
  break-inside: avoid;
  margin-bottom: 16px;
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
  transition: all 0.25s ease;
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 107, 107, 0.15);
}

.note-cover {
  width: 100%;
  overflow: hidden;
}

.note-cover img {
  width: 100%;
  display: block;
  object-fit: cover;
}

.cover-placeholder {
  padding: 24px 16px;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1), rgba(255, 160, 122, 0.05));
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.6;
  text-align: center;
}

.note-info {
  padding: 12px 14px;
}

.note-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.note-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.note-likes {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: color 0.2s;
}

.note-likes:hover {
  color: #FF6B6B;
}

.note-collect {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: color 0.2s;
  color: rgba(255, 255, 255, 0.4);
}

.note-collect:hover {
  color: #FFD93D;
}

.note-actions-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.delete-btn-sm {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  padding: 0;
}

.delete-btn-sm:hover {
  background: rgba(255, 77, 77, 0.15);
  color: #ff4d4f;
}

.delete-btn {
  color: rgba(255, 255, 255, 0.4);
  margin-left: auto;
}

.delete-btn:hover {
  color: #ff4d4f;
  background: rgba(255, 77, 77, 0.1);
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 30px 0;
}

.load-more-btn {
  padding: 12px 40px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.load-more-btn:hover {
  background: rgba(255, 255, 255, 0.06);
}

.loading-dots {
  display: flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  animation: dotPulse 1.4s infinite ease-in-out;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 详情弹窗 */
.detail-dialog :deep(.el-dialog) {
  background: rgba(22, 22, 30, 0.95);
  backdrop-filter: blur(40px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
  overflow: hidden;
}

.detail-content {
  display: flex;
  min-height: 500px;
}

.detail-images {
  width: 50%;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-carousel {
  width: 100%;
  height: 100%;
  position: relative;
}

.image-carousel img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}

.image-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
}

.image-dots span.active {
  background: #FF6B6B;
  width: 18px;
  border-radius: 3px;
}

.no-image {
  padding: 60px;
  color: rgba(255, 255, 255, 0.2);
}

.detail-info {
  width: 50%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.detail-author {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
}

.post-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

.detail-text {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.detail-text p {
  font-size: 14px;
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.75);
  margin: 0 0 12px 0;
}

.detail-topic {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
  border-radius: 8px;
  font-size: 13px;
}

.detail-actions {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.detail-actions .action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.detail-actions .action-btn:hover {
  background: rgba(255, 255, 255, 0.04);
}

.detail-actions .action-btn.liked {
  color: #FF6B6B;
}

.detail-actions .action-btn.collected {
  color: #FFD93D;
}

/* 评论区 */
.detail-comments {
  margin-top: 12px;
}

.comment-header {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 12px;
}

.comment-input {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.comment-input .el-input {
  flex: 1;
}

.comment-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: none !important;
}

.comment-input :deep(.el-input__inner) {
  color: #fff;
}

.comment-submit {
  padding: 8px 16px;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  cursor: pointer;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.comment-submit:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.comment-list {
  max-height: 200px;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.comment-content {
  flex: 1;
}

.comment-author {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  margin-right: 8px;
}

.comment-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.comment-time {
  display: block;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.2);
  margin-top: 4px;
}

/* 发布对话框 */
.publish-dialog :deep(.el-dialog) {
  background: rgba(22, 22, 30, 0.95);
  backdrop-filter: blur(40px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
}

.publish-dialog :deep(.el-dialog__title) {
  color: #fff;
}

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.publish-textarea :deep(.el-textarea__inner) {
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.04);
  padding: 16px;
  font-size: 15px;
  resize: none;
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.publish-textarea :deep(.el-textarea__inner:focus) {
  border-color: rgba(255, 107, 107, 0.4);
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-img {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
}

.topic-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: none !important;
  border-radius: 10px;
}

.topic-input :deep(.el-input__inner) {
  color: #fff;
}

.publish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.publish-tools {
  display: flex;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.tool-btn:hover {
  background: rgba(255, 107, 107, 0.1);
  border-color: rgba(255, 107, 107, 0.2);
  color: #FF6B6B;
}

.publish-btn {
  padding: 10px 28px;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.publish-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.3);
}

.publish-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .waterfall {
    column-count: 2;
    column-gap: 10px;
  }

  .detail-content {
    flex-direction: column;
  }

  .detail-images,
  .detail-info {
    width: 100%;
  }

  .detail-images {
    max-height: 300px;
  }
}
</style>
