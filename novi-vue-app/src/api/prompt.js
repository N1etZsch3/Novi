import request from './index'

// 获取所有性格选项 (Type 1)
export function getPersonalities() {
    return request.get('/prompt/config/type/1')
}

// 获取所有语气选项 (Type 2)
export function getTones() {
    return request.get('/prompt/config/type/2')
}

// 获取用户偏好设置
export function getUserPreferences() {
    return request.get('/v1/preferences')
}

// 更新用户偏好设置 (Combined)
export function updateUserPreferences(data) {
    return request.put('/v1/preferences', data)
}
