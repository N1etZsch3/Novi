import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: null,
        persona: null
    }),

    actions: {
        setUserInfo(info) {
            this.userInfo = info
        },

        setPersona(persona) {
            this.persona = persona
        }
    }
})
