import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// 引入 Bootstrap 和样式
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import '@/assets/styles/variables.css'
import '@/assets/styles/base.css'
import '@/assets/styles/components.css'
import 'github-markdown-css/github-markdown-light.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

app.mount('#app')
