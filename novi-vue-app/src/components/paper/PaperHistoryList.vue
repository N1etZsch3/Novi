<template>
  <div class="h-100 d-flex flex-column">
    <!-- New Paper Button -->
    <div class="px-3 mb-3 mt-2 flex-shrink-0">
      <button
        class="btn btn-primary w-100 rounded-pill py-2 shadow-sm btn-new-chat d-flex align-items-center justify-content-center"
        @click="$emit('new-paper')"
        :title="collapsed ? '新套卷' : ''"
      >
        <i class="bi bi-plus-lg" :class="{ 'me-2': !collapsed }"></i>
        <span v-if="!collapsed" class="text-label">新套卷</span>
      </button>
    </div>

    <div class="d-flex flex-column flex-grow-1 overflow-hidden history-list-content" :class="{ 'collapsed': collapsed }">
    <!-- Header Actions -->
    <div class="px-3 mb-2 small text-muted text-label fw-bold flex-shrink-0 d-flex justify-content-between align-items-center" style="min-height: 32px;">
      <span id="historyHeaderTitle">生成历史</span>

      <!-- Default View -->
      <button v-if="!isEditMode && history.length > 0" class="btn btn-link btn-sm p-0 text-muted text-decoration-none"
        @click="toggleEditMode" style="font-size: 0.85rem;">
        管理
      </button>

      <!-- Edit View -->
      <div v-else-if="isEditMode" class="d-flex align-items-center gap-2">
        <button class="btn btn-danger btn-sm py-0 px-2 rounded-pill" @click="deleteSelected" style="font-size: 0.8rem;">
          删除({{ selectedIds.length }})
        </button>
        <button class="btn btn-secondary btn-sm py-0 px-2 rounded-pill" @click="toggleEditMode"
          style="font-size: 0.8rem;">
          取消
        </button>
      </div>
    </div>
    
    <!-- Select All Bar -->
    <div v-if="isEditMode" class="px-3 mb-2 d-flex align-items-center border-bottom pb-2">
      <div class="form-check">
        <input class="form-check-input" type="checkbox" :checked="isAllSelected" @change="toggleSelectAll">
        <label class="form-check-label small text-muted" @click="toggleSelectAll">全选所有记录</label>
      </div>
    </div>

    <div class="flex-grow-1 overflow-auto px-2 custom-scrollbar">
      <div v-if="history.length === 0" class="text-center text-muted mt-5 py-5">
        <i class="bi bi-clock-history fs-1 opacity-25"></i>
        <p class="small mt-2">暂无生成记录</p>
      </div>
      
      <div v-for="item in history" :key="item.id" 
        class="history-item p-3 mb-2 rounded-3 border position-relative"
        :class="{ active: selectedId === item.id, 'item-selected': selectedIds.includes(item.id), 'editing-mode': isEditMode }"
        @click="handleItemClick(item, $event)">
        
        <!-- Checkbox -->
        <div class="form-check me-2 position-absolute" style="top: 50%; left: 10px; transform: translateY(-50%); z-index: 10;" v-if="isEditMode">
          <input class="form-check-input" type="checkbox" :value="item.id" :checked="selectedIds.includes(item.id)" @click.stop @change="toggleSelection(item.id)">
        </div>

        <div :class="{ 'ps-4': isEditMode }" style="transition: padding 0.2s;">
            <div class="d-flex justify-content-between align-items-start mb-1">
            <span class="badge bg-primary bg-opacity-10 text-primary rounded-pill px-2 py-1" style="font-size: 0.7rem;">
                {{ item.subjectName }}
            </span>
            <button v-if="!isEditMode" class="btn btn-link btn-sm p-0 text-muted delete-btn" 
                @click.stop="$emit('delete', item.id)">
                <i class="bi bi-x-lg"></i>
            </button>
            </div>
            
            <div class="fw-medium text-truncate mb-1" style="font-size: 0.9rem; color: var(--text-main);">
                {{ item.paperName || '未命名试卷' }}
            </div>
            
            <div class="d-flex justify-content-between align-items-center">
            <small class="text-muted" style="font-size: 0.75rem;">
                {{ formatTime(item.createdAt) }}
            </small>
            <small class="text-muted" style="font-size: 0.75rem;">
                共 {{ item.totalQuestions }} 题
            </small>
            </div>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  history: {
    type: Array,
    default: () => []
  },
  selectedId: [String, Number],
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'delete', 'batch-delete', 'new-paper'])

const isEditMode = ref(false)
const selectedIds = ref([])

const isAllSelected = computed(() => {
  return props.history.length > 0 && selectedIds.value.length === props.history.length
})

function toggleEditMode() {
  isEditMode.value = !isEditMode.value
  selectedIds.value = []
}

function handleItemClick(item, event) {
  if (isEditMode.value) {
    toggleSelection(item.id)
  } else {
    emit('select', item.id)
  }
}

function toggleSelection(id) {
  const index = selectedIds.value.indexOf(id)
  if (index === -1) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value.splice(index, 1)
  }
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = props.history.map(item => item.id)
  }
}

function deleteSelected() {
  if (selectedIds.value.length === 0) return
  emit('batch-delete', [...selectedIds.value])
}

function formatTime(str) {
    if (!str) return ''
    const date = new Date(str)
    return date.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

defineExpose({
  resetEditMode: () => {
    isEditMode.value = false
    selectedIds.value = []
  }
})
</script>

<style scoped>
.history-item {
    background: var(--bg-card);
    border-color: transparent !important;
    cursor: pointer;
    transition: all 0.2s;
}

.history-item:hover {
    background: rgba(0,0,0,0.03);
}

.history-item.active {
    background: rgba(78, 110, 242, 0.08); /* Primary color opacity */
    border-color: var(--primary-color) !important;
}

.history-item.item-selected {
    background-color: rgba(78, 110, 242, 0.15) !important;
    border-color: rgba(78, 110, 242, 0.3) !important;
}

.delete-btn {
    opacity: 0;
    transition: opacity 0.2s;
}

.history-item:hover .delete-btn {
    opacity: 1;
}

@media (max-width: 768px) {
    .delete-btn {
        opacity: 1;
    }
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 4px;
}

.history-list-content {
  opacity: 1;
  visibility: visible;
  transition: opacity 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.1s,
              visibility 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.1s;
}

.history-list-content.collapsed {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.1s, visibility 0.1s;
  pointer-events: none;
}
</style>
