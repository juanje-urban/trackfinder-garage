export interface Event {
  id: number
  organizerId: number
  organizerLegalName: string
  trackId: number
  trackName: string
  eventDate: string
  basePrice: number
  maxParticipants: number
  remainingCapacity: number
}
