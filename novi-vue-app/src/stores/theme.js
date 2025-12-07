import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
    state: () => ({
        currentTheme: localStorage.getItem('novi_theme') || 'light'
    }),

    actions: {
        toggleTheme() {
            this.currentTheme = this.currentTheme === 'light' ? 'dark' : 'light'
            localStorage.setItem('novi_theme', this.currentTheme)
            document.documentElement.setAttribute('data-bs-theme', this.currentTheme)
        },

        initTheme() {
            document.documentElement.setAttribute('data-bs-theme', this.currentTheme)
        }
    }
})
