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
        { path: 'users', component: () => import('@/views/Users.vue'), meta: { title: '用户管理', roles: [4, 5] } },
        { path: 'tags', component: () => import('@/views/Tags.vue'), meta: { title: '标签管理', roles: [4, 5] } },
        { path: 'display', component: () => import('@/views/DisplayControl.vue'), meta: { title: '大屏控制', roles: [4, 5] } },
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
  // 角色守卫
  const roles = (to.meta as any).roles as number[] | undefined
  if (roles && u.info && !roles.includes(u.info.role)) {
    return '/dashboard'
  }
  return true
})

export default router
