import request from './index'

// 发送消息（普通模式）
export function sendMessage(data) {
    return request.post('/v1/chat/send/call', data)
}

// 发送流式消息（需要使用原生 fetch）
export function sendMessageStream(data, token) {
    // Use relative path /api/v1/...
    return fetch(`/api/v1/chat/send/stream`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
    })
}

// 获取会话列表
export function getSessions() {
    return request.get('/v1/sessions')
}

// 获取会话历史
export function getSessionHistory(sessionId) {
    return request.get(`/v1/sessions/${sessionId}/messages`)
}

// 删除会话
export function deleteSession(sessionId) {
    return request.delete(`/v1/sessions/${sessionId}`)
}

// 批量删除会话
export function deleteSessions(sessionIds) {
    return request.delete('/v1/sessions', { data: { sessionIds } })
}
