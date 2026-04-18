export interface Event {
  id: number
  organizerId: number
  organizerLegalName: string
  trackId: number
  trackName: string
  trackShortName: string
  eventDate: string
  basePrice: number
  maxParticipants: number
  remainingCapacity: number
  description: string
}
