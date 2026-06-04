<template>
  <div class="login-page" ref="pageRef">
    <!-- 动态背景 -->
    <div class="bg-canvas">
      <div class="grain-overlay"></div>
      <div class="orb orb-1" ref="orb1"></div>
      <div class="orb orb-2" ref="orb2"></div>
      <div class="orb orb-3" ref="orb3"></div>
      <div class="grid-lines"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card" ref="cardRef">
      <!-- 左侧装饰 -->
      <div class="card-accent">
        <div class="accent-shape"></div>
        <div class="accent-dots">
          <span v-for="i in 12" :key="i" class="dot"></span>
        </div>
      </div>

      <div class="card-content">
        <div class="card-header" ref="headerRef">
          <div class="logo-mark">
            <div class="logo-shape">
              <div class="logo-inner"></div>
            </div>
            <span class="logo-text">Connect</span>
          </div>
          <h1 class="title">
            <span class="title-line" ref="titleLine1">欢迎</span>
            <span class="title-line" ref="titleLine2">回来</span>
          </h1>
          <p class="subtitle">登录你的账号，继续探索社交世界</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <div class="form-group" ref="formGroup1">
            <label class="form-label">用户名</label>
            <div class="input-field">
              <div class="input-icon-wrap">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
              <el-input
                v-model="form.username"
                placeholder="输入用户名"
                size="large"
                class="custom-input"
              />
            </div>
          </div>

          <div class="form-group" ref="formGroup2">
            <label class="form-label">密码</label>
            <div class="input-field">
              <div class="input-icon-wrap">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
              </div>
              <el-input
                v-model="form.password"
                type="password"
                placeholder="输入密码"
                size="large"
                show-password
                class="custom-input"
                @keyup.enter="handleLogin"
              />
            </div>
          </div>

          <div class="form-options" ref="optionsRef">
            <el-checkbox v-model="rememberMe" class="remember-check">
              记住我
            </el-checkbox>
            <a href="#" class="forgot-link" @click.prevent="handleForgotPassword">忘记密码？</a>
          </div>

          <button
            type="button"
            class="login-btn"
            :class="{ 'is-loading': loading }"
            :disabled="loading"
            @click="handleLogin"
            ref="btnRef"
          >
            <span class="btn-text">{{ loading ? '登录中...' : '登录' }}</span>
            <span class="btn-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </span>
            <span class="btn-ripple"></span>
          </button>
        </el-form>

        <p class="register-link" ref="registerRef">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>

    <!-- 右侧装饰文字 -->
    <div class="brand-watermark">
      <span>CONNECT</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const pageRef = ref(null)
const cardRef = ref(null)
const headerRef = ref(null)
const titleLine1 = ref(null)
const titleLine2 = ref(null)
const formGroup1 = ref(null)
const formGroup2 = ref(null)
const optionsRef = ref(null)
const btnRef = ref(null)
const registerRef = ref(null)
const orb1 = ref(null)
const orb2 = ref(null)
const orb3 = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

let animCtx = null

onMounted(() => {
  try {
    const gsap = window.gsap
    if (!gsap) {
      ensureElementsVisible()
      return
    }

    animCtx = gsap.context(() => {
      const tl = gsap.timeline({ defaults: { ease: 'power3.out' } })

      tl.from(cardRef.value, {
        y: 60,
        opacity: 0,
        duration: 0.8,
        delay: 0.2
      })
      .from(titleLine1.value, {
        x: -40,
        opacity: 0,
        duration: 0.5
      }, '-=0.3')
      .from(titleLine2.value, {
        x: -40,
        opacity: 0,
        duration: 0.5
      }, '-=0.35')
      .from(formGroup1.value, {
        y: 20,
        opacity: 0,
        duration: 0.4
      }, '-=0.2')
      .from(formGroup2.value, {
        y: 20,
        opacity: 0,
        duration: 0.4
      }, '-=0.25')
      .from(optionsRef.value, {
        y: 15,
        opacity: 0,
        duration: 0.3
      }, '-=0.2')
      .from(btnRef.value, {
        y: 15,
        opacity: 0,
        duration: 0.3
      }, '-=0.2')
      .from(registerRef.value, {
        y: 10,
        opacity: 0,
        duration: 0.3
      }, '-=0.2')

      // 背景光球动画
      if (orb1.value) {
        gsap.to(orb1.value, {
          x: 30,
          y: -20,
          duration: 6,
          repeat: -1,
          yoyo: true,
          ease: 'sine.inOut'
        })
      }
      if (orb2.value) {
        gsap.to(orb2.value, {
          x: -25,
          y: 15,
          duration: 8,
          repeat: -1,
          yoyo: true,
          ease: 'sine.inOut'
        })
      }
      if (orb3.value) {
        gsap.to(orb3.value, {
          x: 20,
          y: 25,
          duration: 7,
          repeat: -1,
          yoyo: true,
          ease: 'sine.inOut'
        })
      }
    }, pageRef.value)
  } catch (e) {
    console.warn('GSAP animation failed, ensuring elements are visible', e)
    ensureElementsVisible()
  }
})

function ensureElementsVisible() {
  const refs = [cardRef, titleLine1, titleLine2, formGroup1, formGroup2, optionsRef, btnRef, registerRef]
  refs.forEach(r => {
    if (r.value) {
      r.value.style.opacity = '1'
      r.value.style.transform = 'none'
    }
  })
}

onUnmounted(() => {
  animCtx?.revert()
})

const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f0f14;
  position: relative;
  overflow: hidden;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

/* 动态背景 */
.bg-canvas {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.grain-overlay {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
  opacity: 0.5;
  z-index: 1;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.3) 0%, transparent 70%);
  top: -10%;
  right: 10%;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 160, 122, 0.25) 0%, transparent 70%);
  bottom: -5%;
  left: 5%;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(255, 217, 61, 0.2) 0%, transparent 70%);
  top: 40%;
  left: 30%;
}

.grid-lines {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* 登录卡片 */
.login-card {
  width: min(440px, 90vw);
  background: rgba(22, 22, 30, 0.85);
  backdrop-filter: blur(40px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
  z-index: 10;
  overflow: hidden;
  display: flex;
}

.card-accent {
  width: 8px;
  background: linear-gradient(180deg, #FF6B6B 0%, #FFA07A 50%, #FFD93D 100%);
  flex-shrink: 0;
  position: relative;
  border-radius: 24px 0 0 24px;
}

.accent-shape {
  position: absolute;
  top: 30%;
  left: -3px;
  width: 14px;
  height: 14px;
  background: #FF6B6B;
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(255, 107, 107, 0.5);
}

.accent-dots {
  position: absolute;
  bottom: 20%;
  left: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.accent-dots .dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
}

.card-content {
  flex: 1;
  padding: 20px 28px;
}

/* Logo */
.logo-mark {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.logo-shape {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: rotate(-10deg);
}

.logo-inner {
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  transform: rotate(10deg);
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
}

/* 标题 */
.card-header {
  margin-bottom: 16px;
}

.title {
  margin: 0 0 4px 0;
  display: flex;
  flex-direction: column;
}

.title-line {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  line-height: 1.15;
  letter-spacing: -1px;
}

.title-line:last-child {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.45);
  margin: 0;
  font-weight: 400;
}

/* 表单 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 0.3px;
}

.input-field {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 0 12px;
  transition: all 0.3s ease;
}

.input-field:focus-within {
  border-color: rgba(255, 107, 107, 0.5);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.08);
}

.input-icon-wrap {
  color: rgba(255, 255, 255, 0.3);
  margin-right: 8px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.custom-input :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none !important;
  padding: 8px 0;
}

.custom-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.custom-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.25);
}

.custom-input :deep(.el-input__suffix) {
  color: rgba(255, 255, 255, 0.3);
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-check :deep(.el-checkbox__label) {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.remember-check :deep(.el-checkbox__inner) {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.15);
}

.remember-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #FF6B6B;
  border-color: #FF6B6B;
}

.forgot-link {
  font-size: 13px;
  color: #FF6B6B;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #FFA07A;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
  margin-top: 8px;
  opacity: 1 !important;
  transform: none !important;
  letter-spacing: 2px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(255, 107, 107, 0.35);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.is-loading {
  opacity: 0.8;
  cursor: wait;
}

.btn-arrow {
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.login-btn:hover .btn-arrow {
  transform: translateX(4px);
}

.btn-ripple {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.2) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s;
}

.login-btn:active .btn-ripple {
  opacity: 1;
}

/* 注册链接 */
.register-link {
  text-align: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
}

.register-link a {
  color: #FF6B6B;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.register-link a:hover {
  color: #FFA07A;
}

/* 品牌水印 */
.brand-watermark {
  position: absolute;
  bottom: 30px;
  right: 40px;
  z-index: 5;
}

.brand-watermark span {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 6px;
  color: rgba(255, 255, 255, 0.08);
}

/* 响应式 */
@media (max-width: 768px) {
  .login-card {
    width: 92%;
    margin: 20px;
  }

  .card-content {
    padding: 36px 28px;
  }

  .title-line {
    font-size: 30px;
  }

  .orb-1 {
    width: 250px;
    height: 250px;
  }

  .orb-2 {
    width: 200px;
    height: 200px;
  }

  .orb-3 {
    width: 150px;
    height: 150px;
  }
}

@media (max-width: 480px) {
  .login-card {
    width: 95%;
    margin: 10px;
    border-radius: 20px;
  }

  .card-content {
    padding: 28px 20px;
  }

  .title-line {
    font-size: 26px;
  }

  .brand-watermark {
    display: none;
  }
}
</style>
