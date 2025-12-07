<template>
  <div class="d-flex flex-column flex-grow-1 overflow-hidden" style="min-height: 0; position: relative;">
    <!-- Header Actions -->
    <div
      class="px-3 mb-2 mt-2 small text-muted text-label fw-bold flex-shrink-0 d-flex justify-content-between align-items-center"
      style="min-height: 32px;">
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

    <!-- History List -->
    <div class="flex-grow-1 overflow-auto px-2" id="examHistoryList">
      <div v-if="history.length === 0" class="text-center text-muted small mt-4 p-3">暂无生成记录</div>
      
      <div v-for="item in history" :key="item.id" 
        :class="['session-item', 'd-flex', 'align-items-center', { 'item-selected': selectedIds.includes(item.id), 'editing-mode': isEditMode }]"
        @click="handleItemClick(item, $event)">
        
        <!-- Checkbox -->
        <div class="form-check me-2" v-if="isEditMode">
          <input class="form-check-input" type="checkbox" :value="item.id" :checked="selectedIds.includes(item.id)" @click.stop @change="toggleSelection(item.id)">
        </div>

        <div class="d-flex flex-column w-100" style="pointer-events: none;">
          <div class="d-flex justify-content-between">
            <span class="fw-bold text-truncate" style="max-width: 140px;">
              {{ item.questionType === '语法填空' ? '语法填空' : item.questionType }}{{ item.theme ? ` | ${item.theme}` : '' }}
            </span>
            <div class="d-flex align-items-center">
              <span class="small text-muted me-2">{{ formatDate(item.createdAt) }}</span>
              <i v-if="!isEditMode" class="bi bi-x btn text-muted p-0 history-delete-btn" style="font-size: 1.1rem; pointer-events: auto;" @click.stop="deleteSingle(item.id)"></i>
            </div>
          </div>
          <div class="small text-muted text-truncate">{{ item.subject }} · {{ item.difficulty }}</div>
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
  }
})

const emit = defineEmits(['select', 'delete', 'batch-delete'])

const isEditMode = ref(false)
const selectedIds = ref([])

const isAllSelected = computed(() => {
  return props.history.length > 0 && selectedIds.value.length === props.history.length
})

function toggleEditMode() {
  isEditMode.value = !isEditMode.value
  selectedIds.value = [] // Clear selection on toggle
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
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

function deleteSingle(id) {
  emit('delete', id)
}

function deleteSelected() {
  if (selectedIds.value.length === 0) return
  emit('batch-delete', [...selectedIds.value])
  // We typically wait for parent to confirm deletion, but we can also reset mode here if we assume success or let parent handle it.
  // Ideally parent should call a method exposed here to reset mode on success.
}

defineExpose({
  resetEditMode: () => {
    isEditMode.value = false
    selectedIds.value = []
  }
})
</script>

<style scoped>
.session-item {
    padding: 10px 16px;
    margin: 4px 8px;
    border-radius: 12px;
    cursor: pointer;
    color: var(--text-main);
    transition: all 0.2s;
    min-height: 44px;
    font-size: 0.9rem;
    position: relative;
    border: 1px solid transparent;
}

.session-item:hover {
    background-color: rgba(0, 0, 0, 0.05);
}

.session-item.editing-mode {
    cursor: pointer;
}

.session-item.item-selected {
    background-color: rgba(78, 110, 242, 0.15) !important;
    border-color: rgba(78, 110, 242, 0.3) !important;
}
</style>
