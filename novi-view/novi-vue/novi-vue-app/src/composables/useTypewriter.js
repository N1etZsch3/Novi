// 导入 useMarkdown
import { useMarkdown } from './useMarkdown'

// 打字机效果组合式函数
export function useTypewriter() {
    let currentElement = null
    let queue = ''
    let isTyping = false
    let intervalId = null
    let rawContent = ''

    const start = (element) => {
        currentElement = element
        queue = ''
        rawContent = ''
        isTyping = true
    }

    const push = (text) => {
        if (!currentElement) return

        queue += text
        rawContent += text

        if (!intervalId && isTyping) {
            processQueue()
        }
    }

    const processQueue = () => {
        if (!currentElement) return

        intervalId = setInterval(() => {
            if (queue.length === 0) {
                clearInterval(intervalId)
                intervalId = null
                return
            }

            // 从队列中取出字符
            const char = queue[0]
            queue = queue.slice(1)

            // 更新元素内容
            const { render } = useMarkdown()
            currentElement.innerHTML = render(rawContent.slice(0, rawContent.length - queue.length))

            // 滚动到底部
            const chatWindow = document.getElementById('chatWindow')
            if (chatWindow) {
                chatWindow.scrollTop = chatWindow.scrollHeight
            }
        }, 20) // 每20ms打一个字符
    }

    const stop = () => {
        isTyping = false
        if (intervalId) {
            clearInterval(intervalId)
            intervalId = null
        }

        // 显示所有剩余内容
        if (currentElement && rawContent) {
            const { render } = useMarkdown()
            currentElement.innerHTML = render(rawContent)
        }

        queue = ''
    }

    return {
        start,
        push,
        stop
    }
}
