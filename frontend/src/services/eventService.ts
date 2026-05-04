import { api } from './api'
import type { Event } from '@/types/event'
import type { EventServiceItem } from '@/types/eventService'

// Servicio de lectura de eventos y de los extras que se pueden contratar en cada evento.
export async function getFutureEvents(): Promise<Event[]> {
  const response = await api.get<Event[]>('/events/future')
  return response.data
}

export async function getEventById(eventId: number): Promise<Event> {
  const response = await api.get<Event>(`/events/${eventId}`)
  return response.data
}

export async function getEventServicesByEventId(eventId: number): Promise<EventServiceItem[]> {
  const response = await api.get<EventServiceItem[]>(`/event-services/event/${eventId}`)
  return response.data
}
