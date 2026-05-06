// Tipos de usuario: perfil propio, perfil público y representación simplificada para admin.
export interface UserProfile {
  id: number
  displayName: string
  email: string
  created: string
  enabled: boolean
  name: string
  surname: string
  address: string
  phone: string
  roleId: number | null
  roleName: string | null
}

export interface PublicUserProfile {
  id: number
  displayName: string
  completedEvents: number
  visitedCircuits: number
  topFiveLapTimes: number
  poleCount: number
}

export interface UpdateCurrentUserProfilePayload {
  name: string
  surname: string
  email: string
  address: string
  phone: string
  password?: string
}

export type AdminUser = UserProfile
