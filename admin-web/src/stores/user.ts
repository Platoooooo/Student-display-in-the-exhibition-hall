import { defineStore } from 'pinia'
import { apiLogin, apiLogout, apiMe, type LoginRsp } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    info: null as LoginRsp | null,
  }),
  actions: {
    async login(username: string, password: string) {
      const data = await apiLogin({ username, password })
      this.token = data.token
      this.info = data
      localStorage.setItem('token', data.token)
    },
    async fetchMe() {
      if (!this.token) return
      this.info = await apiMe()
    },
    async logout() {
      try { await apiLogout() } catch {}
      this.token = ''
      this.info = null
      localStorage.removeItem('token')
    },
  },
})
