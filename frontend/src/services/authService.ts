import { api } from '@/services/api'
import type {
  AuthIdentity,
  AuthCredentials,
  AuthOrganizerRegisterPayload,
  AuthRegisterPayload,
  AuthSession,
} from '@/types/auth'

// Este servicio traduce acciones de autenticación del front a llamadas HTTP del backend.
export async function login(credentials: AuthCredentials): Promise<AuthSession> {
  const response = await api.post<AuthIdentity>('/auth/login', credentials)
  return withAuthorizationHeader(response.data, credentials)
}

export async function getCurrentSession(): Promise<AuthIdentity> {
  const response = await api.get<AuthIdentity>('/auth/me')
  return response.data
}

export async function register(credentials: AuthRegisterPayload): Promise<AuthSession> {
  const response = await api.post<AuthIdentity>('/auth/register', credentials)
  return withAuthorizationHeader(response.data, credentials)
}

export async function registerOrganizer(
  credentials: AuthOrganizerRegisterPayload,
): Promise<AuthSession> {
  const response = await api.post<AuthIdentity>('/auth/register/organizer', credentials)
  return withAuthorizationHeader(response.data, credentials)
}

function withAuthorizationHeader(
  identity: AuthIdentity,
  credentials: AuthCredentials,
): AuthSession {
  // El backend me devuelve la identidad; yo le añado la cabecera para reutilizarla después.
  return {
    ...identity,
    authorizationHeader: buildAuthorizationHeader(credentials),
  }
}

function buildAuthorizationHeader(credentials: AuthCredentials): string {
  // Construyo Basic Auth en el navegador. Uso TextEncoder para no romper caracteres especiales.
  const token = `${credentials.email}:${credentials.password}`
  const tokenBytes = new TextEncoder().encode(token)
  let binaryToken = ''

  tokenBytes.forEach((byte) => {
    binaryToken += String.fromCharCode(byte)
  })

  return `Basic ${window.btoa(binaryToken)}`
}
