import { api } from '@/services/api'
import type {
  Organizer,
  OrganizerProfile,
  UpdateCurrentOrganizerProfilePayload,
} from '@/types/organizer'

export async function getOrganizers(): Promise<Organizer[]> {
  const response = await api.get<Organizer[]>('/organizers')
  return response.data
}

export async function getCurrentOrganizerProfile(): Promise<OrganizerProfile> {
  const response = await api.get<OrganizerProfile>('/organizers/me')
  return response.data
}

export async function updateCurrentOrganizerProfile(
  payload: UpdateCurrentOrganizerProfilePayload,
): Promise<OrganizerProfile> {
  const response = await api.put<OrganizerProfile>('/organizers/me', payload)
  return response.data
}

export async function enableOrganizer(id: number): Promise<Organizer> {
  const response = await api.patch<Organizer>(`/organizers/${id}/enable`)
  return response.data
}

export async function deleteOrganizer(id: number): Promise<void> {
  await api.delete(`/organizers/${id}`)
}
