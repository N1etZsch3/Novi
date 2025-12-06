<template>
  <div class="chat-input-area">
    <div class="input-wrapper">
      <!-- 文本输入框 -->
      <textarea
        ref="textareaRef"
        v-model="message"
        class="form-control"
        rows="1"
        placeholder="输入消息..."
        @keydown.enter.exact="handleEnter"
        @input="adjustHeight"
      ></textarea>

      <!-- 工具栏 -->
      <div class="input-toolbar">
        <!-- 模型选择器 -->
        <div class="model-selector dropdown" ref="dropdownRef">
          <button
            class="btn rounded-pill model-selector-btn d-flex align-items-center gap-2"
            type="button"
            @click="toggleDropdown"
          >
            <i class="bi bi-robot"></i>
            <span>{{ selectedModelName }}</span>
            <i class="bi bi-chevron-down small opacity-75"></i>
          </button>
          <transition name="dropdown">
            <ul v-if="isOpen" class="dropdown-menu model-dropdown-menu shadow-lg rounded-4 p-2 show">
              <li v-for="model in models" :key="model.id">
                <a
                  :class="['dropdown-item', { active: selectedModel === model.id }]"
                  href="#"
                  @click.prevent="selectModel(model.id)"
                >
                  {{ model.name }}
                </a>
              </li>
            </ul>
          </transition>
        </div>

        <div class="input-actions">
          <!-- 全屏按钮 -->
          <button id="fullScreenBtn" @click="$emit('fullscreen')" title="全屏编辑">
            <i class="bi bi-arrows-angle-expand"></i>
          </button>
          
          <!-- 发送按钮 -->
          <button 
            id="sendBtn" 
            :disabled="!message.trim() || disabled"
            @click="handleSend" 
            title="发送消息"
          >
            <i class="bi bi-send-fill"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  models: {
    type: Array,
    default: () => []
  },
  selectedModel: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send', 'update:selectedModel', 'fullscreen'])

const message = ref('')
const textareaRef = ref(null)

const selectedModelName = computed(() => {
  const model = props.models.find(m => m.id === props.selectedModel)
  return model ? model.name : 'Loading...'
})

function handleEnter(event) {
  if (!event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

function handleSend() {
  const msg = message.value.trim()
  if (!msg || props.disabled) return
  
  emit('send', msg)
  message.value = ''
  resetHeight()
}

function selectModel(modelId) {
  emit('update:selectedModel', modelId)
  isOpen.value = false
}

const isOpen = ref(false)
const dropdownRef = ref(null)

function toggleDropdown() {
  isOpen.value = !isOpen.value
}

function handleClickOutside(event) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

function setMessage(text) {
  message.value = text
  adjustHeight()
}

defineExpose({
  setMessage,
  message
})

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  resetHeight()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

function adjustHeight() {
  nextTick(() => {
    if (!textareaRef.value) return
    textareaRef.value.style.height = 'auto'
    const newHeight = Math.min(textareaRef.value.scrollHeight, 200)
    textareaRef.value.style.height = newHeight + 'px'
  })
}

function resetHeight() {
  if (textareaRef.value) {
    textareaRef.value.style.height = '52px'
  }
}


</script>

<style scoped>
.chat-input-area {
  background-color: var(--bg-body);
  padding: 0 1rem 1.5rem 1rem;
  position: relative;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.input-wrapper {
  width: 100%;
  max-width: 850px;
  background-color: var(--bg-input);
  border-radius: 1.5rem;
  position: relative;
  border: 1px solid var(--border-input);
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  padding: 12px 16px;
}

.input-wrapper:focus-within {
  background-color: var(--bg-input-focus);
  box-shadow: 0 8px 24px rgba(78, 110, 242, 0.12);
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.form-control {
  background: transparent;
  border: none;
  resize: none;
  overflow-y: hidden;
  min-height: 52px;
  max-height: 200px;
  padding: 0;
  width: 100%;
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 8px;
}

.form-control:focus {
  box-shadow: none;
  outline: none;
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
}

[data-bs-theme="dark"] .input-toolbar {
  /* 保持一致的样式 */
}

.model-selector-btn {
  background-color: rgba(78, 110, 242, 0.08);
  color: var(--primary-color);
  border: 1px solid transparent;
  font-size: 0.85rem;
  font-weight: 600;
  padding: 6px 16px;
  transition: all 0.2s;
}

.model-selector-btn:hover {
  background-color: rgba(78, 110, 242, 0.15);
}

.model-dropdown-menu {
  border: 1px solid var(--border-color);
  padding: 8px;
  min-width: 200px;
  background-color: var(--bg-card);
  bottom: 100%;
  top: auto;
  margin-bottom: 8px;
}

.model-dropdown-menu .dropdown-item {
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 0.9rem;
  color: var(--text-main);
  transition: all 0.2s;
}

.model-dropdown-menu .dropdown-item:hover {
  background-color: rgba(78, 110, 242, 0.08);
  color: var(--primary-color);
}

.model-dropdown-menu .dropdown-item.active {
  background-color: rgba(78, 110, 242, 0.15);
  color: var(--primary-color);
  font-weight: 600;
}

.model-dropdown-menu .dropdown-item.active::after {
  content: '\F26E';
  font-family: 'bootstrap-icons';
  font-size: 1rem;
  margin-left: 8px;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

#fullScreenBtn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  background: transparent;
  border: none;
  transition: all 0.2s;
}

#fullScreenBtn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--text-main);
}

#sendBtn {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: #fff;
  border: none;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(78, 110, 242, 0.3);
}

#sendBtn:disabled {
  background-color: var(--border-color);
  cursor: not-allowed;
  opacity: 0.7;
  box-shadow: none;
}

#sendBtn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(78, 110, 242, 0.4);
}
</style>
