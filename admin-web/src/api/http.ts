import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const http = axios.create({ baseURL: '/', timeout: 15000 })

http.interceptors.request.use(cfg => {
  const u = useUserStore()
  if (u.token) cfg.headers.Authorization = u.token
  return cfg
})

http.interceptors.response.use(
  resp => {
    const r = resp.data
    if (r && typeof r === 'object' && 'code' in r) {
      if (r.code === 200) return r.data
      if (r.code === 401) {
        ElMessage.error('登录已失效')
        useUserStore().logout()
        router.push('/login')
        return Promise.reject(r)
      }
      ElMessage.error(r.msg || '请求失败')
      return Promise.reject(r)
    }
    return r
  },
  err => {
    ElMessage.error(err.message || '网络异常')
    return Promise.reject(err)
  }
)

export default http
