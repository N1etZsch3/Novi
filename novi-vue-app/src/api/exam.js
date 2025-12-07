import request from './index'

// 获取科目列表
export function getSubjects() {
    return request.get('/question/categories/subjects')
}

// 获取题型列表
export function getQuestionTypes(subjectId) {
    return request.get(`/question/categories/types?subjectId=${subjectId}`)
}

// 生成试题 - 深度思考模式使用30分钟超时，普通模式使用10分钟超时
export function generatePaper(data) {
    const timeout = data.enableThinking ? 1800000 : 600000  // 30分钟 vs 10分钟
    return request.post('/v1/questions/generate', data, {
        timeout
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
