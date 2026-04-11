import { api } from '@/services/api'
import type { Organizer } from '@/types/organizer'

export async function getOrganizers(): Promise<Organizer[]> {
  const response = await api.get<Organizer[]>('/organizers')
  return response.data
}

export async function enableOrganizer(id: number): Promise<Organizer> {
  const response = await api.patch<Organizer>(`/organizers/${id}/enable`)
  return response.data
}

export async function disableOrganizer(id: number): Promise<Organizer> {
  const response = await api.patch<Organizer>(`/organizers/${id}/disable`)
  return response.data
}

export async function deleteOrganizer(id: number): Promise<void> {
  await api.delete(`/organizers/${id}`)
}
