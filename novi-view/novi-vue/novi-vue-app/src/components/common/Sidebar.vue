<template>
  <aside class="sidebar" :class="{ 'mobile-open': mobileOpen }">
    <!-- 头部区域 -->
    <div class="p-3 d-flex align-items-center flex-shrink-0">
      <button 
        class="btn rounded-pill text-muted border-0 sidebar-toggle-btn"
        @click="toggleSidebar"
      >
        <i class="bi bi-list fs-5"></i>
      </button>
      <span class="fw-bold ms-2 text-label" style="white-space: nowrap;">功能导航</span>
    </div>

    <!-- 模式切换 -->
    <div class="px-3 my-2 flex-shrink-0">
      <div class="mode-item" :class="{ active: currentMode === 'chat' }" @click="switchMode('chat')">
        <i class="bi bi-chat-text"></i>
        <span class="text-label">智能对话</span>
      </div>
      <div class="mode-item" :class="{ active: currentMode === 'exam' }" @click="switchMode('exam')">
        <i class="bi bi-file-earmark-text"></i>
        <span class="text-label">AI 出题</span>
      </div>
    </div>

    <hr class="mx-3 my-2 flex-shrink-0" style="border-color: var(--border-color);">

    <!-- 内容区域（插槽） -->
    <div class="flex-grow-1 overflow-hidden d-flex flex-column" style="min-height: 0;">
      <slot></slot>
    </div>

    <!-- 用户信息区域 - 使用下拉菜单 -->
    <UserDropdown :collapsed="collapsed" />
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'
import UserDropdown from './UserDropdown.vue'

const props = defineProps({
  currentMode: {
    type: String,
    default: 'chat'
  },
  collapsed: {
    type: Boolean,
    default: false
  },
  mobileOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-sidebar', 'switch-mode'])

const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()
const authStore = useAuthStore()

const isDark = computed(() => themeStore.isDark)
const userInfo = computed(() => userStore.userInfo || {})

function toggleTheme() {
  themeStore.toggle()
}

function switchMode(mode) {
  emit('switch-mode', mode)
}

function toggleSidebar() {
  emit('toggle-sidebar')
}

function goToSettings() {
  router.push('/settings')
}

function handleLogout() {
  if (confirm('确认退出登录？')) {
    authStore.logout()
    router.push('/auth')
  }
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  background-color: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  height: 100vh;
  flex-shrink: 0;
  transition: width 0.3s ease, transform 0.3s ease;
  overflow: hidden;
}

.text-label {
  white-space: nowrap;
  opacity: 1;
  transition: opacity 0.2s;
}

.sidebar.collapsed .text-label {
  opacity: 0;
  pointer-events: none;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1040;
    z-index: 1040;
    width: 280px !important;
    box-shadow: none;
    transform: translateX(-100%);
  }

  .sidebar.mobile-open {
    transform: translateX(0);
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  }
}

.sidebar-toggle-btn:hover {
  background-color: rgba(0, 0, 0, 0.05) !important;
}

[data-bs-theme="dark"] .sidebar-toggle-btn:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.mode-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
  border-radius: 0.75rem;
  cursor: pointer;
  color: var(--text-sub);
  transition: all 0.2s;
  font-weight: 500;
}

.mode-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--text-main);
}

[data-bs-theme="dark"] .mode-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.mode-item i {
  font-size: 1.1rem;
  width: 24px;
  text-align: center;
}

.mode-item.active {
  background-color: rgba(78, 110, 242, 0.1);
  color: var(--primary-color);
  font-weight: 600;
}

.mode-item.active i {
  color: var(--primary-color);
}
</style>
