import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/', component: () => import('@/views/Home.vue') },
    { path: '/submit', component: () => import('@/views/Submit.vue') },
    { path: '/me', component: () => import('@/views/Me.vue') },
    { path: '/profile/:id', component: () => import('@/views/ProfileDetail.vue') },
    { path: '/face', component: () => import('@/views/FaceRegister.vue') },
  ],
})

router.beforeEach(async (to) => {
  const u = useUserStore()
  if (to.path === '/login') return true
  if (!u.token) return '/login'
  if (!u.info) {
    try { await u.fetchMe() } catch { return '/login' }
  }
  return true
})

export default router
