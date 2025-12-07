import request from './index'

// 获取所有AI模型列表
export function getModelList() {
    return request.get('/model/config/list')
}

// 获取当前激活的模型
export function getActiveModel() {
    return request.get('/model/config/active')
}

// 切换模型
export function switchModel(modelName) {
    return request.post(`/model/config/switch/${modelName}`)
}
