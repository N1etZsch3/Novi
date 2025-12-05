import request from './index'

// 获取科目列表
export function getSubjects() {
    return request.get('/questions/subjects')
}

// 获取题型列表
export function getQuestionTypes(subjectId) {
    return request.get(`/questions/subjects/${subjectId}/types`)
}

// 生成试题
export function generatePaper(data) {
    return request.post('/questions/generate', data)
}

// 获取生成历史
export function getGenerationHistory() {
    return request.get('/questions/history')
}

// 删除生成历史
export function deleteHistory(historyId) {
    return request.delete(`/questions/history/${historyId}`)
}

// 批量删除历史
export function deleteHistories(historyIds) {
    return request.delete('/questions/history', { data: { historyIds } })
}
