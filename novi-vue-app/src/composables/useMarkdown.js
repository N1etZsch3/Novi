// Markdown 渲染组合式函数
import { marked } from 'marked'

export function useMarkdown() {
    // 配置 marked
    marked.setOptions({
        breaks: true,
        gfm: true,
        headerIds: false,
        mangle: false
    })

    const render = (content) => {
        if (!content) return ''
        return marked.parse(content)
    }

    return {
        render
    }
}
