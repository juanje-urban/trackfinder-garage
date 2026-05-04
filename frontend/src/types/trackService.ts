// Asociación entre un circuito y un servicio que se puede ofrecer en ese circuito.
export interface TrackServiceAssignment {
  id: number
  trackId: number
  trackName: string
  serviceId: number
  serviceName: string
}

export interface TrackServiceAssignmentPayload {
  trackId: number
  serviceId: number
}
