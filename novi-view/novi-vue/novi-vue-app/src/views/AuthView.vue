<template>
  <div id="auth-view" class="view-container active">
    <div class="auth-card" id="authCard">
      <!-- 头部 -->
      <div class="auth-header">
        <h2 class="fw-bold mb-2" style="color: var(--primary-color);">Novi</h2>
        <p class="text-muted">AI 挚友 · 懂你所想</p>
      </div>

      <!-- 滑动标签页 -->
      <div 
        :class="['auth-tabs-container', { 'register-mode': !isLogin }]" 
        id="authTabs"
      >
        <div class="auth-tab-bg"></div>
        <button 
          :class="['auth-tab-btn', { active: isLogin }]" 
          @click="switchAuthTab('login')"
        >
          登录
        </button>
        <button 
          :class="['auth-tab-btn', { active: !isLogin }]" 
          @click="switchAuthTab('register')"
        >
          注册
        </button>
      </div>

      <!-- 登录表单 -->
      <div 
        v-show="isLogin"
        :class="['auth-form-wrapper', { fading: isFading }]" 
        id="loginFormWrapper"
      >
        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <input 
              type="text" 
              class="form-control rounded-3 px-3 py-2" 
              v-model="loginForm.username"
              placeholder="用户名" 
              required
            >
          </div>
          <div class="mb-4">
            <div class="input-group password-group">
              <input 
                :type="showLoginPassword ? 'text' : 'password'"
                class="form-control rounded-start-3 px-3 py-2" 
                v-model="loginForm.password"
                placeholder="密码" 
                required
              >
              <span 
                class="input-group-text rounded-end-3"
                @click="showLoginPassword = !showLoginPassword"
                style="cursor: pointer;"
              >
                <i :class="showLoginPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
              </span>
            </div>
          </div>
          <button 
            type="submit" 
            class="btn btn-primary w-100 rounded-pill py-2 fw-bold shadow-sm"
            :disabled="isLoading"
          >
            {{ isLoading ? '登录中...' : '进入' }}
          </button>
        </form>
      </div>

      <!-- 注册表单 -->
      <div 
        v-show="!isLogin"
        :class="['auth-form-wrapper', { fading: isFading }]" 
        id="registerFormWrapper"
      >
        <form @submit.prevent="handleRegister">
          <div class="mb-2">
            <input 
              type="text" 
              class="form-control rounded-3" 
              v-model="registerForm.username"
              placeholder="用户名" 
              required
            >
          </div>
          <div class="mb-2">
            <input 
              type="text" 
              class="form-control rounded-3" 
              v-model="registerForm.nickname"
              placeholder="昵称" 
              required
            >
          </div>
          <div class="mb-2">
            <input 
              type="email" 
              class="form-control rounded-3" 
              v-model="registerForm.email"
              placeholder="邮箱" 
              required
            >
          </div>
          <div class="mb-4">
            <div class="input-group password-group">
              <input 
                :type="showRegPassword ? 'text' : 'password'"
                class="form-control rounded-start-3" 
                v-model="registerForm.password"
                placeholder="密码" 
                required
              >
              <span 
                class="input-group-text rounded-end-3"
                @click="showRegPassword = !showRegPassword"
                style="cursor: pointer;"
              >
                <i :class="showRegPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
              </span>
            </div>
          </div>
          <button 
            type="submit" 
            class="btn btn-primary w-100 rounded-pill py-2 shadow-sm"
            :disabled="isLoading"
          >
            {{ isLoading ? '注册中...' : '注册' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { login, register } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const isLogin = ref(true)
const isFading = ref(false)
const isLoading = ref(false)
const showLoginPassword = ref(false)
const showRegPassword = ref(false)

const loginForm = ref({ 
  username: '', 
  password: '' 
})

const registerForm = ref({ 
  username: '', 
  nickname: '', 
  email: '', 
  password: '' 
})

function switchAuthTab(mode) {
  if ((mode === 'login' && isLogin.value) || (mode === 'register' && !isLogin.value)) {
    return
  }
  
  isFading.value = true
  
  setTimeout(() => {
    isLogin.value = mode === 'login'
    isFading.value = false
  }, 200)
}

async function handleLogin() {
  if (isLoading.value) return
  
  try {
    isLoading.value = true
    const res = await login(loginForm.value)
    
    console.log('Login response:', res)
    
    // 后端成功返回 code: 1，所以支持 1 和 200
    if ((res.code === 1 || res.code === 200) && res.data) {
      // 保存token
      if (res.data.token) {
        authStore.setToken(res.data.token)
      }
      
      // 保存用户信息
      if (res.data.userInfo) {
        userStore.setUserInfo(res.data.userInfo)
      }
      
      // 跳转到聊天页面
      router.push('/chat')
    } else {
      alert(res.msg || res.message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    const errorMsg = error.response?.data?.msg 
      || error.response?.data?.message 
      || error.message 
      || '登录失败，请检查网络连接'
    alert('登录失败：' + errorMsg)
  } finally {
    isLoading.value = false
  }
}

async function handleRegister() {
  if (isLoading.value) return
  
  try {
    isLoading.value = true
    const res = await register(registerForm.value)
    
    console.log('Register response:', res)
    
    // 支持 code: 1 和 code: 200
    if (res.code === 1 || res.code === 200) {
      alert('注册成功，请登录')
      isLogin.value = true
      // 清空注册表单
      registerForm.value = {
        username: '',
        nickname: '',
        email: '',
        password: ''
      }
    } else {
      alert(res.msg || res.message || '注册失败')
    }
  } catch (error) {
    console.error('Register error:', error)
    const errorMsg = error.response?.data?.msg 
      || error.response?.data?.message 
      || error.message 
      || '注册失败，请检查网络连接'
    alert('注册失败：' + errorMsg)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
/* ================= 认证视图样式 ================= */
#auth-view {
  align-items: center;
  justify-content: center;
  padding: 20px;
  min-height: 100vh;
  display: flex;
  background-color: var(--bg-body);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 2.5rem;
  background-color: var(--bg-sidebar);
  border-radius: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color);
  overflow: hidden;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}

.auth-header {
  margin-bottom: 2rem;
  text-align: center;
}

.auth-form-wrapper {
  opacity: 1;
  transition: opacity 0.2s ease;
}

.auth-form-wrapper.fading {
  opacity: 0;
}

.auth-tabs-container {
  position: relative;
  background-color: var(--bg-body);
  border-radius: 50px;
  padding: 4px;
  display: flex;
  margin-bottom: 1.5rem;
  border: 1px solid var(--border-color);
}

.auth-tab-bg {
  position: absolute;
  top: 4px;
  left: 4px;
  bottom: 4px;
  width: calc(50% - 4px);
  background-color: var(--bg-card);
  border-radius: 50px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

.auth-tabs-container.register-mode .auth-tab-bg {
  transform: translateX(100%);
}

.auth-tab-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-weight: 600;
  color: var(--text-sub);
  cursor: pointer;
  z-index: 2;
  transition: color 0.2s;
  background: none;
  border: none;
}

.auth-tab-btn.active {
  color: var(--primary-color);
}

.password-group .input-group-text {
  background-color: var(--bg-input);
  border: 1px solid var(--border-input);
  border-left: none;
  color: var(--text-sub);
  cursor: pointer;
}

.password-group .form-control {
  border-right: none;
}
</style>
