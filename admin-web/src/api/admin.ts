import http from './http'

export interface ProfileDTO {
  id: number; userId: number; userName: string; collegeName?: string; major?: string;
  title: string; category: number; description?: string; coverUrl?: string;
  achieveDate?: string; achieveLevel?: string; issuingOrg?: string;
  status: number; rejectReason?: string; isOnShelf: number; displayWeight: number;
  viewCount?: number; tags?: string[]; mediaList?: any[];
  createdAt?: string; updatedAt?: string;
}
export interface PageResult<T> { total: number; records: T[] }

export const apiAuditPending = (page = 1, size = 10) =>
  http.get<PageResult<ProfileDTO>, PageResult<ProfileDTO>>('/api/audit/pending', { params: { page, size } })

export const apiAuditDo = (id: number, result: 1 | 2, comment?: string) =>
  http.post(`/api/audit/${id}/audit`, { result, comment })

export const apiAuditHistory = (id: number) =>
  http.get(`/api/audit/${id}/history`)

export const apiLibrary = (params: any) =>
  http.get<PageResult<ProfileDTO>, PageResult<ProfileDTO>>('/api/admin/profile/library', { params })

export const apiSetShelf = (id: number, onShelf: 0 | 1) =>
  http.put(`/api/admin/profile/${id}/shelf`, null, { params: { onShelf } })

export const apiSetWeight = (id: number, weight: number) =>
  http.put(`/api/admin/profile/${id}/weight`, null, { params: { weight } })

export const apiSetTags = (id: number, tagIds: number[]) =>
  http.post(`/api/admin/profile/${id}/tags`, { tagIds })

export const apiTags = () => http.get<any[], any[]>('/api/admin/tags')
export const apiDashboard = () => http.get('/api/admin/dashboard')
