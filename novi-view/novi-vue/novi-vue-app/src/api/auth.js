import request from './index'

// 登录
export function login(data) {
    return request.post('/v1/users/login', data)
}

// 注册
export function register(data) {
    return request.post('/v1/users/register', data)
}

// 获取个人资料
export function getProfile() {
    return request.get('/v1/users/me')
}
