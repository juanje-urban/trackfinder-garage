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
