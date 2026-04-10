import { api } from '@/services/api'
import type {
  AuthCredentials,
  AuthOrganizerRegisterPayload,
  AuthRegisterPayload,
  AuthSession,
} from '@/types/auth'

export async function login(credentials: AuthCredentials): Promise<AuthSession> {
  const response = await api.post<AuthSession>('/auth/login', credentials)
  return response.data
}

export async function getCurrentSession(): Promise<AuthSession> {
  const response = await api.get<AuthSession>('/auth/me')
  return response.data
}

export async function register(credentials: AuthRegisterPayload): Promise<AuthSession> {
  const response = await api.post<AuthSession>('/auth/register', credentials)
  return response.data
}

export async function registerOrganizer(
  credentials: AuthOrganizerRegisterPayload,
): Promise<AuthSession> {
  const response = await api.post<AuthSession>('/auth/register/organizer', credentials)
  return response.data
}
