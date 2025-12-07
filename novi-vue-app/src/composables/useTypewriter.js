import { ref } from 'vue'

/**
 * 打字机效果组合式函数
 * 使用 Vue 响应式数据实现逐字显示效果
 */
export function useTypewriter() {
    // 当前显示的内容（逐字增加）
    const displayedContent = ref('')
    // 完整的待显示内容
    const fullContent = ref('')
    // 是否正在打字
    const isActive = ref(false)
    // 定时器ID
    let intervalId = null

    /**
     * 开始打字机效果
     */
    function start() {
        displayedContent.value = ''
        fullContent.value = ''
        isActive.value = true
    }

    /**
     * 推送新内容到队列
     * @param {string} text - 要添加的文本
     */
    function push(text) {
        if (!text) return
        fullContent.value += text

        // 如果没有正在运行的定时器且处于激活状态，开始处理队列
        if (!intervalId && isActive.value) {
            processQueue()
        }
    }

    /**
     * 处理队列，逐字显示
     */
    function processQueue() {
        intervalId = setInterval(() => {
            // 如果已显示完所有内容，停止定时器
            if (displayedContent.value.length >= fullContent.value.length) {
                clearInterval(intervalId)
                intervalId = null
                return
            }

            // 每次增加一个字符
            displayedContent.value = fullContent.value.slice(0, displayedContent.value.length + 1)
        }, 20) // 每20ms显示一个字符
    }

    /**
     * 停止打字机效果，立即显示全部内容
     */
    function stop() {
        isActive.value = false
        if (intervalId) {
            clearInterval(intervalId)
            intervalId = null
        }
        // 立即显示所有内容
        displayedContent.value = fullContent.value
    }

    /**
     * 获取最终完整内容
     */
    function getFullContent() {
        return fullContent.value
    }

    return {
        displayedContent,
        fullContent,
        isActive,
        start,
        push,
        stop,
        getFullContent
    }
}
