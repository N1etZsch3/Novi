<template>
  <div class="custom-select-wrapper position-relative" ref="wrapperRef">
    <!-- Trigger Area -->
    <div 
      class="form-control rounded-3 d-flex align-items-center justify-content-between cursor-pointer select-trigger"
      :class="{ 'focused': isOpen, 'has-value': !!modelValue }"
      @click="toggle"
      style="height: 58px; padding-top: 1.625rem; padding-bottom: 0.625rem;"
    >
      <div class="selected-text text-truncate pe-2">
        {{ displayValue || placeholder }}
      </div>
      <i class="bi bi-chevron-down transition-transform text-muted" :class="{ 'rotate-180': isOpen }"></i>
      
      <!-- Floating Label -->
      <label class="custom-label">{{ label }}</label>
    </div>

    <!-- Dropdown Menu -->
    <Transition name="dropdown-anim">
      <div 
        v-show="isOpen"
        class="custom-dropdown-menu position-absolute w-100 bg-white border rounded-3 shadow-lg mt-1 overflow-hidden"
        style="z-index: 1050; max-height: 300px; overflow-y: auto;"
      >
        <div v-if="options.length === 0" class="p-3 text-center text-muted small">
          暂无选项
        </div>
        
        <div 
          v-for="opt in options" 
          :key="opt.value"
          class="dropdown-item px-3 py-2 d-flex align-items-center justify-content-between cursor-pointer"
          :class="{ 'active': modelValue === opt.value }"
          @click="select(opt)"
        >
          <span class="text-truncate" :title="opt.label">{{ opt.label }}</span>
          <i v-if="modelValue === opt.value" class="bi bi-check-lg text-primary"></i>
        </div>
      </div>
    </Transition>
    
    <!-- Backdrop for closing -->
    <div v-if="isOpen" class="fixed-top w-100 h-100" style="z-index: 1040; cursor: default;" @click="close"></div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number, Object],
    default: null
  },
  options: {
    type: Array,
    default: () => [] // { label: string, value: any }
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const wrapperRef = ref(null)

const displayValue = computed(() => {
  const selected = props.options.find(opt => opt.value === props.modelValue)
  return selected ? selected.label : ''
})

function toggle() {
  isOpen.value = !isOpen.value
}

function close() {
  isOpen.value = false
}

function select(option) {
  emit('update:modelValue', option.value)
  emit('change', option.value)
  close()
}
</script>

<style scoped>
.custom-select-wrapper {
  user-select: none;
}

.select-trigger {
  position: relative;
  background-color: #fff;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.select-trigger:hover {
  border-color: var(--bs-border-color-translucent); /* Boostrap var or custom */
  background-color: #f8f9fa;
}

.select-trigger.focused {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

.custom-label {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  padding: 1rem 0.75rem;
  pointer-events: none;
  border: 1px solid transparent;
  transform-origin: 0 0;
  transition: opacity .1s ease-in-out,transform .1s ease-in-out;
  color: rgba(33, 37, 41, 0.65);
  font-size: 1rem;
}

/* Floating Label Logic mimicking Bootstrap form-floating */
.has-value .custom-label,
.focused .custom-label {
  opacity: .65;
  transform: scale(.85) translateY(-0.5rem) translateX(0.15rem);
}

.transition-transform {
  transition: transform 0.2s ease;
}

.rotate-180 {
  transform: rotate(180deg);
}

.custom-dropdown-menu {
  /* Animation handled by Vue Transition */
  border-color: rgba(0,0,0,0.1);
  padding: 6px;
}

.dropdown-anim-enter-active,
.dropdown-anim-leave-active {
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: top center;
}

.dropdown-anim-enter-from,
.dropdown-anim-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}

.dropdown-item {
  transition: all 0.15s;
  color: var(--text-main);
  border-radius: 8px;
  margin-bottom: 2px;
}

.dropdown-item:last-child {
  margin-bottom: 0;
}

.dropdown-item:hover {
  background-color: rgba(78, 110, 242, 0.08); /* Primary tint */
  color: var(--primary-color);
}

.dropdown-item.active {
  background-color: rgba(78, 110, 242, 0.12);
  color: var(--primary-color);
  font-weight: 600;
}

[data-bs-theme="dark"] .select-trigger {
  background-color: var(--bg-card); /* Assuming CSS var exists, else #212529 */
  color: var(--text-main);
  border-color: var(--border-color);
}

[data-bs-theme="dark"] .select-trigger:hover {
  background-color: rgba(255,255,255,0.05);
}

[data-bs-theme="dark"] .custom-dropdown-menu {
  background-color: var(--bg-card) !important;
  border-color: var(--border-color) !important;
}

[data-bs-theme="dark"] .custom-label {
  color: rgba(255, 255, 255, 0.65);
}
</style>
