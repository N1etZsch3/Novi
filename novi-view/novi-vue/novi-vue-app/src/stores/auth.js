import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('novi_token') || null,
        isAuthenticated: !!localStorage.getItem('novi_token')
    }),

    actions: {
        setToken(token) {
            this.token = token
            this.isAuthenticated = true
            localStorage.setItem('novi_token', token)
        },

        logout() {
            this.token = null
            this.isAuthenticated = false
            localStorage.removeItem('novi_token')
        }
    }
})
