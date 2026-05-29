import { defineStore } from 'pinia'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: '',
    name: '',
    avatar: ''
  }),
  actions: {
    async login({ username, password, role }) {
      const res = await userApi.login({ username, password, role })
      const loginData = res.data
      this.token = loginData.token
      this.role = loginData.user.role
      this.name = loginData.user.name || username
      localStorage.setItem('token', this.token)
      return loginData
    },
    async restoreSession() {
      if (!this.token) return
      try {
        const res = await userApi.getProfile()
        const profile = res.data
        this.role = profile.role
        this.name = profile.name
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.role = ''
      this.name = ''
      localStorage.removeItem('token')
    },
    hasRole(expectedRole) {
      return this.role === expectedRole
    }
  }
})
