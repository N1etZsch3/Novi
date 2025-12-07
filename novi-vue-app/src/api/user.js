import request from './index'

// 更新个人资料
export function updateProfile(data) {
    return request.put('/v1/users/me', data)
}

// 修改密码
export function changePassword(data) {
    return request.put('/v1/users/password', data)
}

// 获取Persona列表
export function getPersonas() {
    return request.get('/v1/users/personas')
}

// 更新Persona
export function updatePersona(personaId) {
    return request.put('/v1/users/persona', { personaId })
}
