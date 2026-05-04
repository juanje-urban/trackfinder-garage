// Fila de ranking de un circuito, ya lista para mostrarse en tablas de mejores vueltas.
export interface TrackRecord {
  trackId: number
  trackName: string
  userDisplayName: string
  lapDate: string
  lapTimeMs: number
  vehicle: string
}
