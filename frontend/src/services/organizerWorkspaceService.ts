import { api } from '@/services/api'
import type {
  OrganizerEventPayload,
  OrganizerWorkspace,
} from '@/types/organizerWorkspace'

export async function getOrganizerWorkspace(): Promise<OrganizerWorkspace> {
  const response = await api.get<OrganizerWorkspace>('/organizer-workspace')
  return response.data
}

export async function addOrganizerCatalogService(serviceId: number): Promise<OrganizerWorkspace> {
  const response = await api.post<OrganizerWorkspace>('/organizer-workspace/services', { serviceId })
  return response.data
}

export async function removeOrganizerCatalogService(
  organizerServiceId: number,
): Promise<OrganizerWorkspace> {
  const response = await api.delete<OrganizerWorkspace>(
    `/organizer-workspace/services/${organizerServiceId}`,
  )
  return response.data
}

export async function createOrganizerEvent(
  payload: OrganizerEventPayload,
): Promise<OrganizerWorkspace> {
  const response = await api.post<OrganizerWorkspace>('/organizer-workspace/events', payload)
  return response.data
}

export async function updateOrganizerEvent(
  eventId: number,
  payload: OrganizerEventPayload,
): Promise<OrganizerWorkspace> {
  const response = await api.put<OrganizerWorkspace>(
    `/organizer-workspace/events/${eventId}`,
    payload,
  )
  return response.data
}
