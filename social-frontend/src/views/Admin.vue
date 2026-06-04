<template>
  <div class="admin-page" ref="pageRef">
    <!-- 统计卡片 -->
    <div class="stats-row" ref="statsRef">
      <div class="stat-card">
        <div class="stat-icon users">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ userCount }}</span>
          <span class="stat-label">总用户数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon posts">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ postCount }}</span>
          <span class="stat-label">总动态数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ activeUsers }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon banned">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ bannedUsers }}</span>
          <span class="stat-label">封禁用户</span>
        </div>
      </div>
    </div>

    <!-- 用户管理 -->
    <div class="section-card" ref="tableRef">
      <div class="section-header">
        <h3>用户管理</h3>
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input type="text" placeholder="搜索用户..." v-model="searchText" />
        </div>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>昵称</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.userId">
              <td class="id-cell">{{ user.userId }}</td>
              <td>
                <div class="user-cell">
                  <el-avatar :size="32" :src="user.avatar">{{ user.nickname?.charAt(0) || 'U' }}</el-avatar>
                  <span>{{ user.username }}</span>
                </div>
              </td>
              <td>{{ user.nickname }}</td>
              <td>
                <span class="role-tag" :class="user.role">{{ user.role === 'admin' ? '管理员' : '用户' }}</span>
              </td>
              <td>
                <span class="status-tag" :class="{ active: user.status === 1, banned: user.status === 0 }">
                  {{ user.status === 1 ? '正常' : '封禁' }}
                </span>
              </td>
              <td>{{ formatDate(user.createTime) }}</td>
              <td>
                <div class="action-btns">
                  <button
                    v-if="user.role !== 'admin'"
                    class="action-btn"
                    :class="{ ban: user.status === 1, unban: user.status === 0 }"
                    @click="toggleUserStatus(user)"
                  >
                    {{ user.status === 1 ? '封禁' : '解封' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getAllUsers, updateUserStatus } from '../api/user'
import { getPostFeed } from '../api/post'
import { ElMessage, ElMessageBox } from 'element-plus'

const pageRef = ref(null)
const statsRef = ref(null)
const tableRef = ref(null)
const searchText = ref('')
const users = ref([])
const postCount = ref(0)

const userCount = computed(() => users.value.length)
const activeUsers = computed(() => users.value.filter(u => u.status === 1).length)
const bannedUsers = computed(() => users.value.filter(u => u.status === 0).length)

const filteredUsers = computed(() => {
  if (!searchText.value) return users.value
  const keyword = searchText.value.toLowerCase()
  return users.value.filter(u =>
    u.username?.toLowerCase().includes(keyword) ||
    u.nickname?.toLowerCase().includes(keyword)
  )
})

let animCtx = null

onMounted(() => {
  loadData()

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return

      animCtx = gsap.context(() => {
        gsap.fromTo(statsRef.value, { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, ease: 'power3.out' })
        gsap.fromTo(tableRef.value, { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, delay: 0.15, ease: 'power3.out' })
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})

const loadData = async () => {
  try {
    const [usersRes, postsRes] = await Promise.allSettled([
      getAllUsers(),
      getPostFeed(1, 1)
    ])
    if (usersRes.status === 'fulfilled') {
      users.value = usersRes.value.data || []
    }
    if (postsRes.status === 'fulfilled') {
      postCount.value = postsRes.value.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  }
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '封禁' : '解封'

  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${user.nickname} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateUserStatus(user.userId, newStatus)
    user.status = newStatus
    ElMessage.success(`已${action}`)
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.admin-page {
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.users {
  background: rgba(102, 126, 234, 0.15);
  color: #667eea;
}

.stat-icon.posts {
  background: rgba(255, 107, 107, 0.15);
  color: #FF6B6B;
}

.stat-icon.active {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.stat-icon.banned {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

/* 区域卡片 */
.section-card {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 18px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
}

.search-box svg {
  color: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.search-box input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: #fff;
  width: 180px;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

/* 表格 */
.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.data-table td {
  padding: 14px 16px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

.data-table tr:hover td {
  background: rgba(255, 255, 255, 0.02);
}

.id-cell {
  color: rgba(255, 255, 255, 0.3);
  font-size: 13px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.role-tag.admin {
  background: rgba(255, 107, 107, 0.15);
  color: #FF6B6B;
}

.role-tag.user {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.5);
}

.status-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.active {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.status-tag.banned {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.action-btn.ban {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.action-btn.ban:hover {
  background: rgba(239, 68, 68, 0.2);
}

.action-btn.unban {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.action-btn.unban:hover {
  background: rgba(34, 197, 94, 0.2);
}

/* 响应式 */
@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
