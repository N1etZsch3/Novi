import { defineStore } from 'pinia'

export const useExamStore = defineStore('exam', {
    state: () => ({
        subjects: [],
        questionTypes: {},
        currentPaper: null,
        history: [],
        config: {
            subject: null,
            questionType: null,
            difficulty: 'medium',
            quantity: 1,
            topic: '',
            model: null,
            enableThinking: false
        }
    }),

    actions: {
        setSubjects(subjects) {
            this.subjects = subjects
        },

        setQuestionTypes(subjectId, types) {
            this.questionTypes[subjectId] = types
        },

        setCurrentPaper(paper) {
            this.currentPaper = paper
        },

        setHistory(history) {
            this.history = history
        },

        updateConfig(config) {
            this.config = { ...this.config, ...config }
        }
    }
})
