import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'post',
        name: 'Post',
        component: () => import('../views/Post.vue'),
        meta: { title: '动态' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('../views/Message.vue'),
        meta: { title: '消息' }
      },
      {
        path: 'ai',
        name: 'AiChat',
        component: () => import('../views/AiChat.vue'),
        meta: { title: 'AI助手' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('../views/Search.vue'),
        meta: { title: '搜索' }
      },
      {
        path: 'profile/:userId?',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'friends',
        name: 'Friends',
        component: () => import('../views/Friends.vue'),
        meta: { title: '好友' }
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('../views/Notification.vue'),
        meta: { title: '通知' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('../views/Admin.vue'),
        meta: { title: '管理后台', requireAdmin: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else if (token && to.path !== '/login' && to.path !== '/register') {
    const userStore = useUserStore()
    if (!userStore.userInfo.userId) {
      try {
        await userStore.getUserInfo()
      } catch (e) {
        console.error('加载用户信息失败', e)
      }
    }
    next()
  } else {
    next()
  }
})

export default router
