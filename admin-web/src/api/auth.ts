import http from './http'

export interface LoginReq { username: string; password: string }
export interface LoginRsp {
  token: string; userId: number; username: string; realName: string;
  role: number; roleName: string; collegeId?: number; collegeName?: string; avatarUrl?: string;
}

export const apiLogin = (data: LoginReq) => http.post<LoginRsp, LoginRsp>('/api/auth/login', data)
export const apiLogout = () => http.post('/api/auth/logout')
export const apiMe = () => http.get<LoginRsp, LoginRsp>('/api/auth/me')
