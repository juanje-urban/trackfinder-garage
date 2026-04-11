import { api } from '@/services/api'
import type { CreateOwnLapTimePayload, LapTime } from '@/types/lapTime'

export async function getLapTimesByUserId(userId: number): Promise<LapTime[]> {
  const response = await api.get<LapTime[]>(`/lap-times/user/${userId}`)
  return response.data
}

export async function getCurrentUserLapTimes(): Promise<LapTime[]> {
  const response = await api.get<LapTime[]>('/lap-times/me')
  return response.data
}

export async function createCurrentUserLapTime(
  payload: CreateOwnLapTimePayload,
): Promise<LapTime> {
  const response = await api.post<LapTime>('/lap-times/me', payload)
  return response.data
}

export async function deleteCurrentUserLapTime(id: number): Promise<void> {
  await api.delete(`/lap-times/me/${id}`)
}
