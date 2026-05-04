import axios from 'axios'
import { getAuthorizationHeader } from '@/composables/useAuth'

const PUBLIC_AUTH_PATHS = new Set([
  '/auth/login',
  '/auth/register',
  '/auth/register/organizer',
])

// Creo una única instancia de Axios para que todos los servicios usen la misma URL base.
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

// Antes de cada petición privada añado la cabecera Basic guardada en la sesión.
// Los endpoints públicos de autenticación se dejan pasar sin cabecera.
api.interceptors.request.use((config) => {
  const requestUrl = config.url ?? ''

  if (PUBLIC_AUTH_PATHS.has(requestUrl)) {
    return config
  }

  const authorizationHeader = getAuthorizationHeader()

  if (authorizationHeader) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = authorizationHeader
  }

  return config
})
