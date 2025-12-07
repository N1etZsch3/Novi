<template>
  <div class="px-3 mb-3 mt-2 flex-shrink-0">
    <button
      class="btn btn-primary w-100 rounded-pill py-2 shadow-sm btn-new-chat d-flex align-items-center justify-content-center"
      @click="$emit('new-chat')"
      :title="collapsed ? '新对话' : ''"
    >
      <i class="bi bi-plus-lg" :class="{ 'me-2': !collapsed }"></i>
      <span v-if="!collapsed" class="text-label">新对话</span>
    </button>
  </div>

  <!-- 头部操作区 -->
  <div v-if="!collapsed" class="px-3 mb-2 mt-2 small text-muted text-label fw-bold flex-shrink-0 d-flex justify-content-between align-items-center" style="min-height: 32px;">
    <span>历史记录</span>
    <button 
      class="btn btn-link btn-sm p-0 text-muted text-decoration-none"
      @click="toggleEditMode"
      style="font-size: 0.85rem;"
    >
      {{ isEditMode ? '取消' : '管理' }}
    </button>
  </div>

  <!-- 批量操作栏 -->
  <div v-if="isEditMode && !collapsed" class="px-3 mb-2 d-flex align-items-center border-bottom pb-2">
    <div class="form-check">
      <input 
        class="form-check-input" 
        type="checkbox"
        :checked="isAllSelected"
        @change="toggleSelectAll"
      >
      <label class="form-check-label small text-muted">全选会话</label>
    </div>
    <button 
      v-if="selectedCount > 0"
      class="btn btn-danger btn-sm py-0 px-2 rounded-pill ms-auto"
      @click="$emit('delete-selected', [...selectedSessions])"
      style="font-size: 0.8rem;"
    >
      删除({{ selectedCount }})
    </button>
  </div>

  <!-- 会话列表 -->
  <div v-if="!collapsed" class="flex-grow-1 overflow-auto px-2">
    <div 
      v-for="session in sessions"
      :key="session.id"
      :class="['session-item', {
        active: currentSessionId === session.id && !isEditMode,
        'item-selected': selectedSessions.includes(session.id)
      }]"
      @click="handleSessionClick(session.id)"
    >
      <div v-if="isEditMode" class="form-check me-2" @click.stop>
        <input 
          class="form-check-input"
          type="checkbox"
          :checked="selectedSessions.includes(session.id)"
          @change="toggleSessionSelect(session.id)"
        >
      </div>
      
      <div class="flex-grow-1 overflow-hidden">
        <div class="text-truncate">{{ session.title || '新对话' }}</div>
        <div class="small text-muted">{{ formatTime(session.updatedAt) }}</div>
      </div>
      
      <button 
        v-if="!isEditMode"
        class="btn btn-sm btn-link text-muted p-0"
        @click.stop="$emit('delete-session', session.id)"
      >
        <i class="bi bi-x" style="font-size: 1.25rem;"></i>
      </button>
    </div>
    
    <div v-if="sessions.length === 0" class="text-center text-muted small mt-4 p-3">
      暂无会话记录
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  sessions: {
    type: Array,
    default: () => []
  },
  currentSessionId: {
    type: [String, Number],
    default: null
  },
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['new-chat', 'select-session', 'delete-session', 'delete-selected'])

const isEditMode = ref(false)
const selectedSessions = ref([])

const selectedCount = computed(() => selectedSessions.value.length)
const isAllSelected = computed(() => {
  return props.sessions.length > 0 && selectedSessions.value.length === props.sessions.length
})

function toggleEditMode() {
  isEditMode.value = !isEditMode.value
  if (!isEditMode.value) {
    selectedSessions.value = []
  }
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedSessions.value = []
  } else {
    selectedSessions.value = props.sessions.map(s => s.id)
  }
}

function toggleSessionSelect(sessionId) {
  const index = selectedSessions.value.indexOf(sessionId)
  if (index > -1) {
    selectedSessions.value.splice(index, 1)
  } else {
    selectedSessions.value.push(sessionId)
  }
}

function handleSessionClick(sessionId) {
  if (isEditMode.value) {
    toggleSessionSelect(sessionId)
  } else {
    emit('select-session', sessionId)
  }
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}
function resetEditMode() {
  isEditMode.value = false
  selectedSessions.value = []
}

defineExpose({
  resetEditMode
})
</script>

<style scoped>
.btn-new-chat {
  transition: all 0.2s;
}

.session-item {
  padding: 10px 16px;
  margin: 4px 8px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: var(--text-main);
  transition: all 0.2s;
  min-height: 44px;
  font-size: 0.9rem;
  border: 1px solid transparent;
}

.session-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

[data-bs-theme="dark"] .session-item:hover {
  background-color: rgba(255, 255, 255, 0.12) !important;
}

.session-item.active {
  background-color: rgba(78, 110, 242, 0.1);
  color: var(--primary-color) !important;
  font-weight: 600;
}

.session-item.item-selected {
  background-color: rgba(78, 110, 242, 0.15) !important;
  border-color: rgba(78, 110, 242, 0.3) !important;
}
</style>
