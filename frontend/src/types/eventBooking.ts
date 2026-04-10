export interface EventBooking {
  id: number
  userId: number
  userDisplayName: string
  eventId: number
  eventDate: string
  organizerLegalName: string
  trackName: string
  bookedAt: string
  basePriceAtPurchase: number
}

export interface EventBookingCheckoutPayload {
  eventId: number
  eventServiceIds: number[]
}

export interface EventBookedService {
  id: number
  eventBookingId: number
  userId: number
  eventId: number
  eventServiceId: number
  priceAtPurchase: number
}
