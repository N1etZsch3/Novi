<template>
  <div class="exam-config-panel" :class="{ 'collapsed': isCollapsed && isMobile }">
    <!-- Mobile Toggle Header -->
    <div class="config-header d-flex align-items-center justify-content-between" @click="toggleCollapse">
      <h5 class="fw-bold mb-0">套卷配置</h5>
      <button class="btn btn-link p-0 d-lg-none" type="button">
        <i class="bi" :class="isCollapsed ? 'bi-chevron-down' : 'bi-chevron-up'"></i>
      </button>
    </div>

    <!-- Collapsible Content -->
    <div class="config-content" :class="{ 'show': !isCollapsed || !isMobile }">
      <form @submit.prevent="generate">
        <!-- Model Selection -->
        <div class="mb-3">
          <CustomSelect
            v-model="localConfig.model"
            :options="modelOptions"
            label="AI 模型"
            @change="handleModelChange"
          />
        </div>

        <!-- Deep Thinking Toggle -->
        <div id="deepThinkingContainer" v-if="showDeepThinking"
          class="d-flex align-items-center justify-content-between p-3 mb-3 rounded-3 border border-warning border-opacity-25 bg-warning bg-opacity-10">
          <div class="d-flex align-items-center">
            <div
              class="rounded-circle bg-warning bg-opacity-25 text-warning d-flex align-items-center justify-content-center me-3"
              style="width: 36px; height: 36px; flex-shrink: 0;">
              <i class="bi bi-stars"></i>
            </div>
            <div style="line-height: 1.2;">
              <div class="fw-bold" style="font-size: 0.9rem;">深度思考</div>
              <div class="small opacity-75" style="font-size: 0.75rem; transform: scale(0.95); transform-origin: left;">
                启用强推理模式，耗时较长</div>
            </div>
          </div>
          <div class="form-check form-switch m-0">
            <input class="form-check-input" type="checkbox" role="switch" id="enableThinking"
              v-model="localConfig.enableThinking" style="width: 3em; height: 1.5em;">
          </div>
        </div>

        <!-- Subject Selection -->
        <div class="mb-3">
          <CustomSelect
            v-model="localConfig.subject"
            :options="subjectOptions"
            label="选择科目"
            @change="handleSubjectChange"
          />
        </div>

        <!-- Difficulty -->
        <div class="mb-4">
          <label class="form-label small fw-bold text-muted">难度系数</label>
          <div class="diff-slider-container">
            <div class="diff-slider-track" :class="difficultyColorClass">
              <div class="diff-slider-glider" :style="gliderStyle"></div>
            </div>
            
            <div class="diff-options">
              <div 
                class="diff-option" 
                :class="{ 'active': localConfig.difficulty === 'simple' }"
                @click="localConfig.difficulty = 'simple'">
                简单
              </div>
              <div 
                class="diff-option" 
                :class="{ 'active': localConfig.difficulty === 'medium' }"
                @click="localConfig.difficulty = 'medium'">
                标准
              </div>
              <div 
                class="diff-option" 
                :class="{ 'active': localConfig.difficulty === 'hard' }"
                @click="localConfig.difficulty = 'hard'">
                困难
              </div>
            </div>
          </div>
        </div>

        <button type="submit" class="btn btn-primary w-100 rounded-pill py-2 fw-bold shadow-sm" :disabled="loading || !localConfig.subject">
          <i class="bi bi-file-earmark-richtext me-2"></i>{{ loading ? '正在生成...' : '生成套卷' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import CustomSelect from '@/components/common/CustomSelect.vue'

const props = defineProps({
  models: {
    type: Array,
    default: () => []
  },
  subjects: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  modelValue: { // Config object
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'subject-change', 'generate', 'switch-model'])

// Mobile detection and collapse state
const isMobile = ref(window.innerWidth <= 992)
const isCollapsed = ref(true) // Start collapsed on mobile

// Handle resize
function handleResize() {
  isMobile.value = window.innerWidth <= 992
  // Auto-expand on desktop
  if (!isMobile.value) {
    isCollapsed.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  handleResize()
})

function toggleCollapse() {
  if (isMobile.value) {
    isCollapsed.value = !isCollapsed.value
  }
}

const localConfig = ref({ ...props.modelValue })
let isUpdatingFromProp = false

// Sync prop changes to local
watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localConfig.value)) {
    isUpdatingFromProp = true
    localConfig.value = { ...newVal }
    isUpdatingFromProp = false
  }
}, { deep: true })

// Sync local changes to prop
watch(localConfig, (newVal) => {
  if (!isUpdatingFromProp) {
    emit('update:modelValue', { ...newVal })
  }
}, { deep: true })

const showDeepThinking = computed(() => {
  const model = props.models.find(m => m.modelName === localConfig.value.model)
  // Logic from ExamConfigPanel, ensuring robust check
  return model && (String(model.enableThinking) === 'true' || String(model.enableThinking) === '1')
})

function handleModelChange() {
  emit('switch-model', localConfig.value.model)
}

function handleSubjectChange() {
    emit('subject-change', localConfig.value.subject)
}

function generate() {
  if (!localConfig.value.subject) {
    return
  }
  emit('generate')
}

// Option Computed Properties
const modelOptions = computed(() => {
  return props.models.map(m => ({
    label: m.description || m.modelName,
    value: m.modelName
  }))
})

const subjectOptions = computed(() => {
  return props.subjects.map(s => ({
    label: s.name,
    value: s.id // PaperConfig uses id
  }))
})

// Difficulty UI Helpers
const difficultyColorClass = computed(() => {
  switch (localConfig.value.difficulty) {
    case 'simple': return 'diff-simple'
    case 'medium': return 'diff-medium'
    case 'hard': return 'diff-hard'
    default: return 'diff-medium'
  }
})

const gliderStyle = computed(() => {
  // Use left positioning for precise alignment with option centers
  // Each option is 33.33% wide, glider starts at 4px padding
  if (localConfig.value.difficulty === 'simple') return { left: '4px' }
  if (localConfig.value.difficulty === 'medium') return { left: 'calc(33.33% + 1.33px)' }
  if (localConfig.value.difficulty === 'hard') return { left: 'calc(66.66% - 1.33px)' }
  return { left: '4px' }
})
</script>

<style scoped>
.exam-config-panel {
  width: 320px;
  background-color: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  padding: 1.5rem;
  overflow-y: auto;
  flex-shrink: 0;
}

.config-header {
  cursor: default;
  margin-bottom: 1rem;
}

.config-content {
  display: block;
}

@media (max-width: 992px) {
  .exam-config-panel {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
    height: auto;
    padding: 1rem 1.5rem;
    overflow: visible;
    flex-shrink: 0;
    position: static;
  }

  .config-header {
    cursor: pointer;
    margin-bottom: 0;
    padding: 0.5rem 0;
  }

  .config-header:hover {
    opacity: 0.8;
  }

  .config-header .btn-link {
    color: var(--text-main);
    font-size: 1.2rem;
  }

  .config-content {
    display: none;
    padding-top: 1rem;
    animation: slideDown 0.3s ease-out;
  }

  .config-content.show {
    display: block;
  }

  .exam-config-panel.collapsed {
    padding-bottom: 1rem;
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.exam-config-panel .form-floating > label,
.exam-config-panel .form-floating > .form-control:focus ~ label,
.exam-config-panel .form-floating > .form-control:not(:placeholder-shown) ~ label,
.exam-config-panel .form-floating > .form-select ~ label,
.exam-config-panel .form-control,
.exam-config-panel .form-control:focus,
.exam-config-panel .form-select,
.exam-config-panel .form-select:focus {
  z-index: auto !important;
}

/* Difficulty Slider */
.diff-slider-container {
  position: relative;
  height: 40px;
  background-color: var(--bg-body);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  user-select: none;
}

.diff-slider-track {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  transition: background-color 0.3s ease;
}

/* Dynamic container background colors */
.diff-slider-track.diff-simple { background-color: rgba(75, 206, 151, 0.1); }
.diff-slider-track.diff-medium { background-color: rgba(78, 110, 242, 0.1); }
.diff-slider-track.diff-hard { background-color: rgba(246, 78, 96, 0.1); }

.diff-slider-glider {
  position: absolute;
  top: 4px;
  /* left is set dynamically via :style */
  width: calc(33.33% - 2.67px);
  height: calc(100% - 8px);
  border-radius: 6px;
  background-color: #fff;
  box-shadow: 0 2px 5px rgba(0,0,0,0.08);
  transition: left 0.3s cubic-bezier(0.2, 0, 0.2, 1);
}

/* Dark mode glider */
[data-bs-theme="dark"] .diff-slider-glider {
  background-color: #444; /* Darker glider in dark mode */
  box-shadow: 0 2px 5px rgba(0,0,0,0.3);
}

.diff-options {
  position: relative;
  display: flex;
  height: 100%;
  z-index: 2; /* Ensure clicks hit options */
}

.diff-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-sub);
  transition: color 0.3s;
}

.diff-option.active {
  color: var(--text-main);
  font-weight: 600;
}

/* Specific text colors for active state */
.diff-slider-track.diff-simple ~ .diff-options .diff-option.active { color: #2ba77a; } /* Green */
.diff-slider-track.diff-medium ~ .diff-options .diff-option.active { color: #3b5bdb; } /* Blue */
.diff-slider-track.diff-hard ~ .diff-options .diff-option.active { color: #d63948; } /* Red */

/* Dark mode overrides for text color if needed */
[data-bs-theme="dark"] .diff-slider-track.diff-simple ~ .diff-options .diff-option.active { color: #4bce97; }
[data-bs-theme="dark"] .diff-slider-track.diff-medium ~ .diff-options .diff-option.active { color: #5e81f4; }
[data-bs-theme="dark"] .diff-slider-track.diff-hard ~ .diff-options .diff-option.active { color: #f64e60; }
</style>
