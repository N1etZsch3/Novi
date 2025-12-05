<template>
  <div class="dropdown custom-select-wrapper" ref="dropdownRef">
    <button 
      class="form-select text-start d-flex align-items-center justify-content-between" 
      type="button" 
      @click="toggle"
      :class="{ show: isOpen }"
    >
      <span>{{ selectedLabel }}</span>
    </button>
    <ul class="dropdown-menu w-100" :class="{ show: isOpen }">
      <li v-for="option in options" :key="option.value">
        <a 
          class="dropdown-item" 
          :class="{ active: modelValue === option.value }"
          href="#" 
          @click.prevent="select(option)"
        >
          {{ option.label }}
        </a>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: [String, Number],
  options: {
    type: Array,
    default: () => [] // { label, value }
  }
})

const emit = defineEmits(['update:modelValue'])

const isOpen = ref(false)
const dropdownRef = ref(null)

const selectedLabel = computed(() => {
  const option = props.options.find(o => o.value === props.modelValue)
  return option ? option.label : props.modelValue
})

function toggle() {
  isOpen.value = !isOpen.value
}

function select(option) {
  emit('update:modelValue', option.value)
  isOpen.value = false
}

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
.custom-select-wrapper {
  position: relative;
}
.form-select {
  cursor: pointer;
}
</style>
