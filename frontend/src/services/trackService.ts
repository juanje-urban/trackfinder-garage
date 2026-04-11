import { api } from './api'
import type { Track, TrackPayload } from '@/types/track'
import type { TrackRecord } from '@/types/trackRecord'

export async function getTracks(): Promise<Track[]> {
  const response = await api.get<Track[]>('/tracks')
  return response.data
}

export async function getTrackById(id: number): Promise<Track> {
  const response = await api.get<Track>(`/tracks/${id}`)
  return response.data
}

export async function createTrack(payload: TrackPayload): Promise<Track> {
  const response = await api.post<Track>('/tracks', payload)
  return response.data
}

export async function updateTrack(id: number, payload: TrackPayload): Promise<Track> {
  const response = await api.put<Track>(`/tracks/${id}`, payload)
  return response.data
}

export async function getTrackRanking(id: number, limit = 3): Promise<TrackRecord[]> {
  const response = await api.get<TrackRecord[]>(`/tracks/${id}/ranking`, {
    params: { limit },
  })

  return response.data
}
