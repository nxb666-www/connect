<template>
  <div class="settings-page" ref="pageRef">
    <div class="settings-section">
      <h3 class="section-title">个人资料</h3>
      <div class="setting-card">
        <div class="setting-row">
          <label>头像</label>
          <div class="setting-value">
            <el-avatar :size="48" :src="userStore.userInfo.avatar" class="setting-avatar">
              {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <button class="change-btn" @click="triggerAvatarUpload">更换头像</button>
          </div>
        </div>
        <div class="setting-row">
          <label>昵称</label>
          <div class="setting-value">
            <input v-model="form.nickname" class="setting-input" placeholder="请输入昵称" />
          </div>
        </div>
        <div class="setting-row">
          <label>个性签名</label>
          <div class="setting-value">
            <input v-model="form.signature" class="setting-input" placeholder="介绍一下自己..." />
          </div>
        </div>
        <div class="setting-row">
          <label>手机号</label>
          <div class="setting-value">
            <input v-model="form.phone" class="setting-input" placeholder="请输入手机号" />
          </div>
        </div>
        <div class="setting-row">
          <label>邮箱</label>
          <div class="setting-value">
            <input v-model="form.email" class="setting-input" placeholder="请输入邮箱" />
          </div>
        </div>
        <div class="setting-actions">
          <button class="save-btn" @click="handleSave">保存修改</button>
        </div>
      </div>
    </div>

    <div class="settings-section">
      <h3 class="section-title">账号安全</h3>
      <div class="setting-card">
        <div class="setting-row">
          <label>修改密码</label>
          <div class="setting-value">
            <input v-model="pwdForm.oldPassword" type="password" class="setting-input" placeholder="当前密码" />
            <input v-model="pwdForm.newPassword" type="password" class="setting-input" placeholder="新密码" style="margin-top:8px" />
            <input v-model="pwdForm.confirmPassword" type="password" class="setting-input" placeholder="确认新密码" style="margin-top:8px" />
          </div>
        </div>
        <div class="setting-actions">
          <button class="save-btn" @click="handleChangePassword">修改密码</button>
        </div>
      </div>
    </div>

    <div class="settings-section">
      <h3 class="section-title">关于</h3>
      <div class="setting-card">
        <div class="setting-row">
          <label>版本</label>
          <div class="setting-value">
            <span class="setting-text">v1.0.0</span>
          </div>
        </div>
        <div class="setting-row">
          <label>技术栈</label>
          <div class="setting-value">
            <span class="setting-text">Spring Cloud + Vue 3 + Element Plus</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { updateUser, uploadAvatar } from '../api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const pageRef = ref(null)

const form = reactive({
  nickname: '',
  signature: '',
  phone: '',
  email: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

let animCtx = null

onMounted(() => {
  form.nickname = userStore.userInfo.nickname || ''
  form.signature = userStore.userInfo.signature || ''
  form.phone = userStore.userInfo.phone || ''
  form.email = userStore.userInfo.email || ''

  setTimeout(() => {
    try {
      const gsap = window.gsap
      if (!gsap || !pageRef.value) return
      animCtx = gsap.context(() => {
        gsap.fromTo('.settings-section', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, stagger: 0.1, ease: 'power3.out' })
      }, pageRef.value)
    } catch (e) { console.warn('GSAP animation failed', e) }
  }, 300)
})

onUnmounted(() => {
  animCtx?.revert()
})

const handleSave = async () => {
  try {
    const data = {}
    if (form.nickname) data.nickname = form.nickname
    if (form.signature) data.signature = form.signature
    if (form.phone) data.phone = form.phone
    if (form.email) data.email = form.email
    await updateUser(data)
    userStore.userInfo.nickname = form.nickname
    userStore.userInfo.signature = form.signature
    userStore.userInfo.phone = form.phone
    userStore.userInfo.email = form.email
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleChangePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写密码')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  try {
    await updateUser({
      oldPassword: pwdForm.oldPassword,
      password: pwdForm.newPassword
    })
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    ElMessage.success('密码修改成功')
  } catch (e) {
    ElMessage.error('密码修改失败，请检查当前密码是否正确')
  }
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
.settings-page {
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
  max-width: 700px;
}

.settings-section {
  margin-bottom: 28px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 16px 0;
}

.setting-card {
  background: rgba(22, 22, 30, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px 24px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.setting-row {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.setting-row:last-of-type {
  border-bottom: none;
}

.setting-row label {
  width: 100px;
  flex-shrink: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  padding-top: 10px;
}

.setting-value {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.setting-avatar {
  background: linear-gradient(135deg, #FF6B6B, #FFA07A);
  color: white;
  font-weight: 600;
}

.change-btn {
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.change-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #FF6B6B;
}

.setting-input {
  width: 100%;
  max-width: 360px;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.setting-input:focus {
  border-color: rgba(255, 107, 107, 0.4);
}

.setting-input::placeholder {
  color: rgba(255, 255, 255, 0.2);
}

.setting-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  padding-top: 10px;
}

.setting-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.save-btn {
  padding: 10px 28px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
}

.save-btn:hover {
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.3);
}
</style>
