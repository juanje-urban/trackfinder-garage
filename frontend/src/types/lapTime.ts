// Tiempo por vuelta de un usuario. El tiempo se guarda en milisegundos para comparar fácil.
export interface LapTime {
  id: number
  userId: number
  userDisplayName: string
  trackId: number
  trackName: string
  lapDate: string
  lapTimeMs: number
  vehicle: string | null
}

export interface CreateOwnLapTimePayload {
  trackId: number
  lapDate: string
  lapTimeMs: number
  vehicle?: string
}
