<template>
  <div id="app" :data-bs-theme="theme">
    <router-view />
    <ToastContainer />
  </div>
</template>

<script setup>
import { computed, watch, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import ToastContainer from '@/components/common/ToastContainer.vue'

const themeStore = useThemeStore()
const theme = computed(() => themeStore.currentTheme)

// Sync theme to html tag
watch(theme, (newTheme) => {
  document.documentElement.setAttribute('data-bs-theme', newTheme)
}, { immediate: true })

onMounted(() => {
  document.documentElement.setAttribute('data-bs-theme', theme.value)
})
</script>

<style>
/* App.vue 的全局样式 */
</style>
