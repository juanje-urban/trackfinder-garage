import axios from 'axios'
import { getAuthorizationHeader } from '@/composables/useAuth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

api.interceptors.request.use((config) => {
  const authorizationHeader = getAuthorizationHeader()

  if (authorizationHeader) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = authorizationHeader
  }

  return config
})
