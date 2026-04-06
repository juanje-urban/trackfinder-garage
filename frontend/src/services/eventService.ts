import { api } from './api'
import type { Event } from '@/types/event'

export async function getFutureEvents(): Promise<Event[]> {
  const response = await api.get<Event[]>('/events/future')
  return response.data
}
