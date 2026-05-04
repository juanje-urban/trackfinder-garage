// Tipos de reservas. Separamos la reserva base de los servicios extra comprados.
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
  isVisible: boolean
}

export interface EventBookingCheckoutPayload {
  eventId: number
  eventServiceIds: number[]
  isVisible: boolean
}

export interface UpdateEventBookingVisibilityPayload {
  isVisible: boolean
}

export interface EventBookedService {
  id: number
  eventBookingId: number
  userId: number
  eventId: number
  eventServiceId: number
  priceAtPurchase: number
}
