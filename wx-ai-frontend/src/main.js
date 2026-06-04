import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'
import LoginPage from './components/LoginPage.vue'
import ChatLayout from './components/ChatLayout.vue'

const routes = [
    { path: '/login', component: LoginPage },
    { path: '/', component: ChatLayout, meta: { requiresAuth: true } }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to) => {
    if (to.meta.requiresAuth && !localStorage.getItem('token')) {
        return '/login'
    }
})

const app = createApp(App)
app.use(router)
app.mount('#app')