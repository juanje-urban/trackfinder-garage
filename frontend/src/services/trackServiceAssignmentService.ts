import { api } from '@/services/api'
import type {
  TrackServiceAssignment,
  TrackServiceAssignmentPayload,
} from '@/types/trackService'

// Relación entre circuitos y servicios disponibles en pista.
export async function getTrackServiceAssignments(): Promise<TrackServiceAssignment[]> {
  const response = await api.get<TrackServiceAssignment[]>('/track-services')
  return response.data
}

export async function createTrackServiceAssignment(
  payload: TrackServiceAssignmentPayload,
): Promise<TrackServiceAssignment> {
  const response = await api.post<TrackServiceAssignment>('/track-services', payload)
  return response.data
}

export async function deleteTrackServiceAssignment(id: number): Promise<void> {
  await api.delete(`/track-services/${id}`)
}
