// Servicio maestro que puede estar permitido para circuitos, organizadores o ambos.
export interface ServiceCatalogItem {
  id: number
  name: string
  description: string
  allowedForTrack: boolean
  allowedForOrganizer: boolean
  enabled: boolean
}

export interface ServiceCatalogPayload {
  name: string
  description: string
  allowedForTrack: boolean
  allowedForOrganizer: boolean
}
