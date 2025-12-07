import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
    {
        path: '/auth',
        name: 'Auth',
        component: () => import('@/views/AuthView.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/chat',
        name: 'Chat',
        component: () => import('@/views/ChatView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/exam',
        name: 'Exam',
        component: () => import('@/views/ExamView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/paper',
        name: 'Paper',
        component: () => import('@/views/PaperView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/settings',
        name: 'Settings',
        component: () => import('@/views/SettingsView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/',
        redirect: '/chat'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/auth')
    } else if (to.path === '/auth' && authStore.isAuthenticated) {
        next('/chat')
    } else {
        next()
    }
})

export default router
