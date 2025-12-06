<template>
  <Teleport to="body">
    <transition name="modal">
      <div v-if="modelValue" class="modal-wrapper">
        <!-- Backdrop -->
        <div class="modal-backdrop fade show" @click="cancel"></div>
        <!-- Modal Container -->
        <div class="modal fade show" style="display: block;" tabindex="-1" aria-modal="true" role="dialog">
          <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content text-center p-5 border-0 shadow-lg rounded-5" style="background-color: var(--bg-card); color: var(--text-main);">
              <div class="mb-4">
                 <div class="avatar avatar-ai mx-auto" style="width: 80px; height: 80px; font-size: 3rem; background: transparent;">
                   <i class="bi bi-exclamation-circle text-warning" v-if="type === 'warning'"></i>
                   <i class="bi bi-trash text-danger" v-else-if="type === 'danger'"></i>
                   <i class="bi bi-info-circle text-primary" v-else></i>
                 </div>
              </div>
              <h4 class="fw-bold mb-3">{{ title }}</h4>
              <p class="text-muted mb-5">{{ message }}</p>
              
              <div class="d-flex justify-content-center gap-3">
                <button type="button" class="btn btn-light rounded-pill px-4 py-2" @click="cancel">取消</button>
                <button type="button" class="btn rounded-pill px-4 py-2" :class="confirmButtonClass" @click="confirm">
                  {{ confirmText }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '确认' },
  type: { type: String, default: 'primary' } // primary, warning, danger
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const confirmButtonClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'btn-danger'
    case 'warning': return 'btn-warning text-dark'
    default: return 'btn-primary'
  }
})

function cancel() {
  emit('update:modelValue', false)
  emit('cancel')
}

function confirm() {
  emit('confirm')
  emit('update:modelValue', false)
}
</script>

<style scoped>
/* Ensure modal sits above everything */
.modal { z-index: 1055; }
.modal-backdrop { z-index: 1050; }
</style>
