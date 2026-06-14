import { defineStore } from 'pinia'
import { apiLogin, apiLogout, apiMe } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    info: null as any,
  }),
  actions: {
    async login(username: string, password: string) {
      const r = await apiLogin({ username, password })
      this.token = r.token; this.info = r
      localStorage.setItem('token', r.token)
    },
    async fetchMe() {
      if (!this.token) return
      this.info = await apiMe()
    },
    async logout() {
      try { await apiLogout() } catch {}
      this.token = ''; this.info = null; localStorage.removeItem('token')
    },
  },
})
