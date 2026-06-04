<template>
  <div class="register-page" ref="pageRef">
    <!-- 动态背景 -->
    <div class="bg-canvas">
      <div class="grain-overlay"></div>
      <div class="orb orb-1" ref="orb1"></div>
      <div class="orb orb-2" ref="orb2"></div>
      <div class="orb orb-3" ref="orb3"></div>
    </div>

    <!-- 注册卡片 -->
    <div class="register-card" ref="cardRef">
      <div class="card-accent"></div>

      <div class="card-content">
        <div class="card-header">
          <router-link to="/login" class="back-link">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
            </svg>
            <span>返回登录</span>
          </router-link>
          <h1 class="title">
            <span class="title-line">创建</span>
            <span class="title-line accent">账号</span>
          </h1>
          <p class="subtitle">加入 Connect，开始你的社交之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
        >
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <div class="input-field">
                <el-input
                  v-model="form.username"
                  placeholder="设置用户名"
                  size="large"
                  class="custom-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">昵称</label>
              <div class="input-field">
                <el-input
                  v-model="form.nickname"
                  placeholder="设置昵称"
                  size="large"
                  class="custom-input"
                />
              </div>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-field">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="设置密码"
                size="large"
                show-password
                class="custom-input"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">手机号 <span class="optional">选填</span></label>
            <div class="input-field">
              <el-input
                v-model="form.phone"
                placeholder="输入手机号"
                size="large"
                class="custom-input"
              />
            </div>
          </div>

          <div class="agreement">
            <el-checkbox v-model="form.agreement" class="agreement-check">
              我已阅读并同意 <a href="#">服务条款</a> 和 <a href="#">隐私政策</a>
            </el-checkbox>
          </div>

          <button
            type="button"
            class="register-btn"
            :class="{ 'is-loading': loading }"
            :disabled="loading"
            @click="handleRegister"
          >
            <span class="btn-text">{{ loading ? '注册中...' : '注册' }}</span>
            <span class="btn-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
              </svg>
            </span>
          </button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const pageRef = ref(null)
const cardRef = ref(null)
const orb1 = ref(null)
const orb2 = ref(null)
const orb3 = ref(null)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  phone: '',
  agreement: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  agreement: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请同意服务条款'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

let animCtx = null

onMounted(() => {
  try {
    const gsap = window.gsap
    if (!gsap) return

    animCtx = gsap.context(() => {
      if (cardRef.value) {
        gsap.from(cardRef.value, {
          y: 60,
          opacity: 0,
          duration: 0.8,
          delay: 0.2,
          ease: 'power3.out'
        })
      }
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
    console.warn('GSAP animation failed', e)
  }
})

onUnmounted(() => {
  animCtx?.revert()
})

const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f0f14;
  position: relative;
  overflow: hidden;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

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
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.orb-1 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(107, 203, 119, 0.25) 0%, transparent 70%);
  top: -10%;
  left: 10%;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.2) 0%, transparent 70%);
  bottom: -5%;
  right: 5%;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(255, 217, 61, 0.15) 0%, transparent 70%);
  top: 50%;
  right: 30%;
}

.register-card {
  width: 480px;
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
  width: 6px;
  background: linear-gradient(180deg, #6BCB77 0%, #4ECDC4 50%, #FFD93D 100%);
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  padding: 44px 40px;
}

.card-header {
  margin-bottom: 36px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.4);
  text-decoration: none;
  font-size: 13px;
  margin-bottom: 20px;
  transition: color 0.2s;
}

.back-link:hover {
  color: #FF6B6B;
}

.title {
  margin: 0 0 12px 0;
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

.title-line.accent {
  background: linear-gradient(135deg, #6BCB77, #4ECDC4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.45);
  margin: 0;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
}

.form-label .optional {
  color: rgba(255, 255, 255, 0.25);
  font-weight: 400;
}

.input-field {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  padding: 0 16px;
  transition: all 0.3s ease;
}

.input-field:focus-within {
  border-color: rgba(107, 203, 119, 0.5);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: 0 0 0 4px rgba(107, 203, 119, 0.08);
}

.custom-input :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none !important;
  padding: 14px 0;
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

.agreement {
  margin-top: 4px;
}

.agreement-check :deep(.el-checkbox__label) {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
}

.agreement-check :deep(.el-checkbox__inner) {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.15);
}

.agreement-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #6BCB77;
  border-color: #6BCB77;
}

.agreement-check a {
  color: #6BCB77;
  text-decoration: none;
}

.agreement-check a:hover {
  text-decoration: underline;
}

.register-btn {
  width: 100%;
  height: 52px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 14px;
  background: linear-gradient(135deg, #6BCB77 0%, #4ECDC4 100%);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
  margin-top: 4px;
  opacity: 1 !important;
  transform: none !important;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(107, 203, 119, 0.3);
}

.register-btn:active {
  transform: translateY(0);
}

.register-btn.is-loading {
  opacity: 0.8;
  cursor: wait;
}

.btn-arrow {
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.register-btn:hover .btn-arrow {
  transform: translateX(4px);
}

@media (max-width: 520px) {
  .register-card {
    width: 92%;
  }

  .card-content {
    padding: 32px 24px;
  }

  .title-line {
    font-size: 28px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
