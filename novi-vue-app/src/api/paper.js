import request from './index'

export function getPaperHistory() {
    return request({
        url: '/v1/papers/history',
        method: 'get'
    })
}

export function getPaperDetail(id) {
    return request({
        url: `/v1/papers/${id}`,
        method: 'get'
    })
}

export function deletePaper(id) {
    return request({
        url: `/v1/papers/${id}`,
        method: 'delete'
    })
}

export function deletePapers(ids) {
    return request({
        url: '/v1/papers',
        method: 'delete',
        data: ids
    })
}

/**
 * SSE Stream Generation
 * @param {Object} data Request payload
 * @param {Function} onMessage Callback for 'question' events
 * @param {Function} onError Callback for 'error' events
 * @param {Function} onComplete Callback for 'complete' events
 */
export async function generatePaperStream(data, onMessage, onError, onComplete) {
    // 1. Correct Token Key
    const token = localStorage.getItem('novi_token') || ''

    try {
        const response = await fetch('/api/v1/papers/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // 2. Add Bearer Prefix
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        })

        if (!response.ok) {
            // 3. User-friendly Error Parsing
            let errorMessage = `请求失败: ${response.status}`
            try {
                const errorText = await response.text()
                // Try parsing JSON error
                const errorJson = JSON.parse(errorText)
                if (errorJson.msg || errorJson.message) {
                    errorMessage = errorJson.msg || errorJson.message
                }
            } catch (e) {
                // Not JSON or parse error, stick to status
                console.warn('Non-JSON error response', e)
            }
            throw new Error(errorMessage)
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
            const { value, done } = await reader.read()
            if (done) break

            const chunk = decoder.decode(value, { stream: true })
            buffer += chunk

            const parts = buffer.split('\n\n')
            buffer = parts.pop()

            for (const part of parts) {
                if (!part.trim()) continue

                const lines = part.split('\n')
                let eventType = 'message'
                let eventData = null

                for (const line of lines) {
                    if (line.startsWith('event:')) {
                        eventType = line.substring(6).trim()
                    } else if (line.startsWith('data:')) {
                        try {
                            eventData = JSON.parse(line.substring(5).trim())
                        } catch (e) {
                            console.error('JSON Parse Error', e)
                        }
                    }
                }

                if (eventData) {
                    if (eventType === 'error') {
                        if (onError) onError(eventData)
                    } else if (eventType === 'complete') {
                        if (onComplete) onComplete(eventData)
                    } else if (eventType === 'question') {
                        if (onMessage) onMessage(eventData)
                    }
                }
            }
        }
    } catch (error) {
        if (onError) onError(error)
    }
}
