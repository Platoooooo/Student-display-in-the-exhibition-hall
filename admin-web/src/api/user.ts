import http from './http'

export interface UserDTO {
  id: number; username: string; realName: string; role: number; roleName: string;
  collegeId?: number; collegeName?: string; major?: string;
  enrollmentYear?: number; graduationYear?: number;
  phone?: string; email?: string; avatarUrl?: string; status?: number;
  lastLoginAt?: string; createdAt?: string;
}
export interface PageResult<T> { total: number; records: T[] }

export const apiUserList = (params: any) =>
  http.get<PageResult<UserDTO>, PageResult<UserDTO>>('/api/user/list', { params })
export const apiUserSave = (data: any) => http.post<number, number>('/api/user/save', data)
export const apiUserToggleStatus = (id: number, status: 0 | 1) =>
  http.put(`/api/user/${id}/status`, null, { params: { status } })
export const apiUserResetPassword = (id: number, newPassword: string) =>
  http.put(`/api/user/${id}/reset-password`, null, { params: { newPassword } })
export const apiUserDetail = (id: number) => http.get<UserDTO, UserDTO>(`/api/user/${id}`)

export const apiCollegeList = () => http.get<any[], any[]>('/api/college/list')

export interface TagDTO { id?: number; name: string; color: string; sortOrder?: number }
export const apiTagList = () => http.get<TagDTO[], TagDTO[]>('/api/tag/list')
export const apiTagSave = (data: TagDTO) => http.post<number, number>('/api/tag/save', data)
export const apiTagDelete = (id: number) => http.delete(`/api/tag/${id}`)

export const apiProfileDetail = (id: number) => http.get(`/api/profile/${id}`)

// 大屏控制
export const apiPushProfile = (id: number) => http.post(`/api/admin/display/push/profile/${id}`)
export const apiPushNotice = (text: string) => http.post('/api/admin/display/push/notice', { text })
export const apiRefreshDisplay = () => http.post('/api/admin/display/refresh')
export const apiDisplayOnline = () => http.get<{ count: number }, { count: number }>('/api/admin/display/online')
