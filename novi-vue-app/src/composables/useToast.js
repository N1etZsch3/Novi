// Toast 提示组合式函数
import { ref } from 'vue'

const toasts = ref([])
let toastId = 0

export function useToast() {
    const show = (message, type = 'info', duration = 3000) => {
        // 防止重复提示
        const existing = toasts.value.find(t => t.message === message && t.type === type && t.visible)
        if (existing) {
            return existing.id
        }

        // 限制最大数量
        if (toasts.value.length >= 3) {
            const oldest = toasts.value.find(t => t.visible)
            if (oldest) hide(oldest.id)
        }

        const id = toastId++
        const toast = {
            id,
            message,
            type, // 'success', 'error', 'warning', 'info'
            visible: true
        }

        toasts.value.push(toast)

        // 自动隐藏
        if (duration > 0) {
            setTimeout(() => {
                hide(id)
            }, duration)
        }

        return id
    }

    const hide = (id) => {
        const index = toasts.value.findIndex(t => t.id === id)
        if (index > -1) {
            toasts.value[index].visible = false
            // 动画结束后移除
            setTimeout(() => {
                const removeIndex = toasts.value.findIndex(t => t.id === id)
                if (removeIndex > -1) {
                    toasts.value.splice(removeIndex, 1)
                }
            }, 300)
        }
    }

    const success = (message, duration) => show(message, 'success', duration)
    const error = (message, duration) => show(message, 'error', duration)
    const warning = (message, duration) => show(message, 'warning', duration)
    const info = (message, duration) => show(message, 'info', duration)

    return {
        toasts,
        show,
        hide,
        success,
        error,
        warning,
        info
    }
}
