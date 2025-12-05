<template>
  <div class="modal fade" :class="{ show: modelValue }" :style="{ display: modelValue ? 'block' : 'none' }" tabindex="-1" role="dialog">
    <!-- Backdrop -->
    <div class="modal-backdrop fade show" v-if="modelValue" @click="close"></div>
    
    <div class="modal-dialog modal-fullscreen" role="document">
      <div class="modal-content" style="background: var(--bg-body);">
        <div class="modal-header border-bottom">
          <h5 class="modal-title fw-bold">沉浸式编辑</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <div class="modal-body p-0">
          <div class="container h-100 py-4">
            <textarea 
              id="fullScreenTextarea"
              class="form-control border-0 h-100 shadow-none"
              style="font-size: 1.1rem; background: transparent; resize: none;"
              placeholder="在这里尽情输入..."
              v-model="localText"
              ref="textareaRef"
              autofocus
            ></textarea>
          </div>
        </div>
        <div class="modal-footer border-top">
          <button type="button" class="btn btn-secondary rounded-pill" @click="close">取消</button>
          <button type="button" class="btn btn-primary rounded-pill px-4" @click="confirm">完成</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  text: String
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const localText = ref('')
const textareaRef = ref(null)

watch(() => props.modelValue, (val) => {
  if (val) {
    localText.value = props.text
    nextTick(() => {
      textareaRef.value?.focus()
    })
  }
})

function close() {
  emit('update:modelValue', false)
}

function confirm() {
  emit('confirm', localText.value)
  close()
}
</script>

<style scoped>
.modal.show {
  background-color: transparent; /* Backdrop handles opacity */
}
</style>
