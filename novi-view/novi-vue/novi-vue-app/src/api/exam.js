import request from './index'

// 获取科目列表
export function getSubjects() {
    return request.get('/question/categories/subjects')
}

// 获取题型列表
export function getQuestionTypes(subjectId) {
    return request.get(`/question/categories/types?subjectId=${subjectId}`)
}

// 生成试题 - 使用更长的超时时间，因为 AI 生成需要较长时间
export function generatePaper(data) {
    return request.post('/v1/questions/generate', data, {
        timeout: 600000  // 10 分钟超时
    })
}

// 获取生成历史
export function getGenerationHistory() {
    return request.get('/v1/questions/history')
}

// 获取单条历史详情
export function getHistoryRecord(id) {
    return request.get(`/v1/questions/history/${id}`)
}

// 删除生成历史
export function deleteHistory(historyId) {
    return request.delete(`/v1/questions/history/${historyId}`)
}

// 批量删除历史
export function deleteHistories(historyIds) {
    return request.delete('/v1/questions/history', { data: historyIds })
}
