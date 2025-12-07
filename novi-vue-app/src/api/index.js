import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const instance = axios.create({
    baseURL: '/api',
    timeout: 30000
})

// 请求拦截器：添加Token
instance.interceptors.request.use(
    config => {
        const authStore = useAuthStore()
        if (authStore.token) {
            config.headers.Authorization = `Bearer ${authStore.token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器：统一错误处理
instance.interceptors.response.use(
    response => {
        // 直接返回 response.data
        const res = response.data
        if (res.code === 0 && res.msg === 'INVALID_TOKEN') {
            const authStore = useAuthStore()
            authStore.logout()
            router.push('/auth')
            return Promise.reject(new Error('INVALID_TOKEN'))
        }
        return res
    },
    error => {
        // 401 未授权：跳转登录
        if (error.response?.status === 401) {
            const authStore = useAuthStore()
            authStore.logout()
            router.push('/auth')
        }
        return Promise.reject(error)
    }
)

export default instance
