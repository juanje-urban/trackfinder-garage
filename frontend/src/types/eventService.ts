// Servicio ofertado dentro de un evento concreto, ya sea del circuito o del organizador.
export interface EventServiceItem {
  id: number
  eventId: number
  trackServiceId: number | null
  trackServiceName: string | null
  organizerServiceId: number | null
  organizerServiceName: string | null
  price: number
  hasBookings: boolean
}
