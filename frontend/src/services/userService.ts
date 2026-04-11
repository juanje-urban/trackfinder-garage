import { api } from '@/services/api'
import type {
  AdminUser,
  PublicUserProfile,
  UpdateCurrentUserProfilePayload,
  UserProfile,
} from '@/types/user'

export async function getCurrentUserProfile(): Promise<UserProfile> {
  const response = await api.get<UserProfile>('/users/me')
  return response.data
}

export async function getPublicUserProfile(displayName: string): Promise<PublicUserProfile> {
  const response = await api.get<PublicUserProfile>(`/users/public/${encodeURIComponent(displayName)}`)
  return response.data
}

export async function updateCurrentUserProfile(
  payload: UpdateCurrentUserProfilePayload,
): Promise<UserProfile> {
  const response = await api.put<UserProfile>('/users/me', payload)
  return response.data
}

export async function getUsers(): Promise<AdminUser[]> {
  const response = await api.get<AdminUser[]>('/users')
  return response.data
}

export async function enableUser(id: number): Promise<AdminUser> {
  const response = await api.patch<AdminUser>(`/users/${id}/enable`)
  return response.data
}

export async function disableUser(id: number): Promise<AdminUser> {
  const response = await api.patch<AdminUser>(`/users/${id}/disable`)
  return response.data
}
