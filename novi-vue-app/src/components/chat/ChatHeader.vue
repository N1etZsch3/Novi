<template>
  <header class="chat-header d-flex align-items-center justify-content-between px-4 py-3" style="background: var(--bg-header);">
    <div class="d-flex align-items-center">
      <button 
        class="btn btn-link p-0 me-3 d-md-none" 
        style="color: var(--text-main);"
        @click="toggleSidebar"
      >
        <i class="bi bi-list fs-4"></i>
      </button>
      <h6 class="mb-0 fw-bold" style="color: var(--text-main);">{{ title }}</h6>
    </div>
    
    <div class="d-flex align-items-center gap-3">
      <!-- 主题切换 -->
      <div class="form-check form-switch mb-0" title="深色模式">
        <input 
          class="form-check-input theme-switcher" 
          type="checkbox" 
          :checked="isDark"
          @change="toggleTheme"
        >
        <label class="form-check-label">
          <i class="bi bi-moon-stars" style="color: var(--text-main);"></i>
        </label>
      </div>
      
      <!-- 流式/普通切换 -->
      <div class="btn-group btn-group-sm">
        <input 
          type="radio" 
          class="btn-check" 
          name="apiMode" 
          id="modeStream" 
          value="stream"
          :checked="apiMode === 'stream'"
          @change="$emit('update:apiMode', 'stream')"
        >
        <label class="btn btn-outline-secondary" for="modeStream">流式</label>
        
        <input 
          type="radio" 
          class="btn-check" 
          name="apiMode" 
          id="modeCall" 
          value="call"
          :checked="apiMode === 'call'"
          @change="$emit('update:apiMode', 'call')"
        >
        <label class="btn btn-outline-secondary" for="modeCall">普通</label>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

defineProps({
  title: {
    type: String,
    default: 'Novi AI'
  },
  apiMode: {
    type: String,
    default: 'stream'
  }
})

const emit = defineEmits(['toggle-sidebar', 'update:apiMode'])

const themeStore = useThemeStore()
const isDark = computed(() => themeStore.currentTheme === 'dark')

function toggleSidebar() {
  emit('toggle-sidebar')
}

function toggleTheme() {
  themeStore.toggleTheme()
}
</script>

<style scoped>
.chat-header {
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}
</style>
