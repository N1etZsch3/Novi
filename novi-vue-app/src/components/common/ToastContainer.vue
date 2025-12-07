<template>
  <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1100">
    <transition-group name="toast">
      <div 
        v-for="toast in toasts" 
        :key="toast.id"
        class="toast align-items-center custom-toast show" 
        :class="[
          toast.type === 'success' ? 'border-success' : 
          toast.type === 'error' ? 'border-danger' : ''
        ]"
        role="alert"
      >
        <div class="d-flex p-3 align-items-center">
          <i class="bi fs-5 me-3" :class="[
            toast.type === 'success' ? 'bi-check-circle-fill text-success' : 
            toast.type === 'error' ? 'bi-x-circle-fill text-danger' : 
            'bi-info-circle-fill text-primary'
          ]"></i>
          <div class="toast-body p-0 fw-medium">{{ toast.message }}</div>
          <button type="button" class="btn-close me-2 m-auto" @click="hide(toast.id)"></button>
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { useToast } from '@/composables/useToast'

const { toasts, hide } = useToast()
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
