import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/Login.vue') },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '仪表盘' } },
        { path: 'audit', component: () => import('@/views/Audit.vue'), meta: { title: '审核中心' } },
        { path: 'library', component: () => import('@/views/Library.vue'), meta: { title: '资料库' } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
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
