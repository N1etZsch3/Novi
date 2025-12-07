<template>
  <div class="h-100 d-flex flex-column">
    <div class="px-3 py-2 d-flex justify-content-between align-items-center">
      <small class="text-muted fw-bold">历史记录</small>
    </div>
    
    <div class="flex-grow-1 overflow-auto px-2 custom-scrollbar">
      <div v-if="history.length === 0" class="text-center text-muted mt-5 py-5">
        <i class="bi bi-clock-history fs-1 opacity-25"></i>
        <p class="small mt-2">暂无生成记录</p>
      </div>
      
      <div v-for="item in history" :key="item.id" 
        class="history-item p-3 mb-2 rounded-3 border position-relative"
        :class="{ active: selectedId === item.id }"
        @click="$emit('select', item.id)">
        
        <div class="d-flex justify-content-between align-items-start mb-1">
           <span class="badge bg-primary bg-opacity-10 text-primary rounded-pill px-2 py-1" style="font-size: 0.7rem;">
               {{ item.subjectName }}
           </span>
           <button class="btn btn-link btn-sm p-0 text-muted delete-btn" 
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
</template>

<script setup>
defineProps({
  history: Array,
  selectedId: [String, Number]
})

defineEmits(['select', 'delete'])

function formatTime(str) {
    if (!str) return ''
    const date = new Date(str)
    return date.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
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

.delete-btn {
    opacity: 0;
    transition: opacity 0.2s;
}

.history-item:hover .delete-btn {
    opacity: 1;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 4px;
}
</style>
