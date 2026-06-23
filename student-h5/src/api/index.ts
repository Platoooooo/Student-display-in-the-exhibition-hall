import http from './http'

export const apiLogin = (data: { username: string; password: string }) =>
  http.post('/api/auth/login', data) as any
export const apiMe = () => http.get('/api/auth/me') as any
export const apiLogout = () => http.post('/api/auth/logout')

export const apiUpload = (file: File, dir = 'common') => {
  const fd = new FormData(); fd.append('file', file); fd.append('dir', dir)
  return http.post('/api/file/upload', fd) as any
}

export const apiProfileSubmit = (data: any) => http.post('/api/profile/submit', data) as any
export const apiProfileDraft = (data: any) => http.post('/api/profile/draft', data) as any
export const apiMyProfiles = (params: any) => http.get('/api/profile/my', { params }) as any
export const apiProfileDetail = (id: number) => http.get(`/api/profile/${id}`) as any
export const apiProfileDelete = (id: number) => http.delete(`/api/profile/${id}`)

export const apiFaceRegister = (data: { featureBase64: string; faceImageUrl?: string }) =>
  http.post('/api/face/register', data)
export const apiFaceStatus = () => http.get('/api/face/status') as any
export const apiFaceExtract = (imageBase64: string) =>
  http.post('/api/face/extract', { imageBase64 }) as any
export const apiFaceRecognize = (imageBase64: string) =>
  http.post('/api/face/recognize-image', { imageBase64, deviceId: 'web-display' }) as any

export const apiAuditHistory = (id: number) => http.get(`/api/audit/${id}/history`) as any
export const apiTagList = () => http.get('/api/tag/list') as any
export const apiChangePassword = (data: { oldPassword: string; newPassword: string }) =>
  http.put('/api/user/change-password', data)
export const apiUpdateProfile = (data: any) => http.put('/api/user/profile', data)
