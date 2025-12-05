<template>
  <div class="user-dropdown" ref="dropdownRef">
    <div class="user-info-trigger" @click="toggleDropdown">
      <div class="avatar avatar-user">
        {{ (userInfo.nickname || userInfo.username || 'U').charAt(0).toUpperCase() }}
      </div>
      <div class="user-details" v-if="!collapsed">
        <div class="user-name">{{ userInfo.nickname || userInfo.username || '用户' }}</div>
      </div>
      <i class="bi bi-chevron-down ms-auto" v-if="!collapsed"></i>
    </div>

    <!-- 下拉菜单 -->
    <transition name="dropdown">
      <div v-if="isOpen" class="dropdown-menu-custom">
        <button class="dropdown-item" @click="goToSettings">
          <i class="bi bi-gear"></i>
          <span>设置</span>
        </button>
        <div class="dropdown-divider"></div>
        <button class="dropdown-item text-danger" @click="handleLogout">
          <i class="bi bi-box-arrow-right"></i>
          <span>退出登录</span>
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

const isOpen = ref(false)
const dropdownRef = ref(null)

const userInfo = computed(() => userStore.userInfo || {})

function toggleDropdown() {
  isOpen.value = !isOpen.value
}

function goToSettings() {
  isOpen.value = false
  router.push('/settings')
}

function handleLogout() {
  isOpen.value = false
  if (confirm('确认退出登录？')) {
    authStore.logout()
    router.push('/auth')
  }
}

// 点击外部关闭下拉菜单
function handleClickOutside(event) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.user-dropdown {
  position: relative;
  margin-top: auto;
  padding: 0.75rem;
}

.user-info-trigger {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 0.75rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.user-info-trigger:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

[data-bs-theme="dark"] .user-info-trigger:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-user {
  background: var(--primary-color);
  color: white;
  font-size: 1.2rem;
}

.user-details {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.user-name {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email {
  font-size: 0.75rem;
  color: var(--text-sub);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 下拉菜单 */
.dropdown-menu-custom {
  position: absolute;
  bottom: 100%;
  left: 0.75rem;
  right: 0.75rem;
  margin-bottom: 0.5rem;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 0.75rem;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 0.5rem;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem;
  background: none;
  border: none;
  border-radius: 0.5rem;
  color: var(--text-main);
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.2s;
  text-align: left;
}

.dropdown-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

[data-bs-theme="dark"] .dropdown-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.dropdown-item.text-danger {
  color: #dc3545;
}

.dropdown-divider {
  height: 1px;
  background-color: var(--border-color);
  margin: 0.5rem 0;
}

/* 过渡动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
