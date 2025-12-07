import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
    state: () => ({
        sessions: [],
        currentSessionId: null,
        isTyping: false,
        selectedModel: null
    }),

    actions: {
        setSessions(sessions) {
            this.sessions = sessions
        },

        setCurrentSession(sessionId) {
            this.currentSessionId = sessionId
        },

        setTyping(status) {
            this.isTyping = status
        },

        setModel(model) {
            this.selectedModel = model
        }
    }
})
