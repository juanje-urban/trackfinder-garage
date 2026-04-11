import { api } from './api'
import type {
  EventBookedService,
  EventBooking,
  EventBookingCheckoutPayload,
  UpdateEventBookingVisibilityPayload,
} from '@/types/eventBooking'

export async function checkoutEventBooking(
  payload: EventBookingCheckoutPayload,
): Promise<EventBooking> {
  const response = await api.post<EventBooking>('/event-bookings/checkout', payload)
  return response.data
}

export async function getEventBookingsByUserId(userId: number): Promise<EventBooking[]> {
  const response = await api.get<EventBooking[]>(`/event-bookings/user/${userId}`)
  return response.data
}

export async function getVisibleEventBookingsByEventId(eventId: number): Promise<EventBooking[]> {
  const response = await api.get<EventBooking[]>(`/event-bookings/event/${eventId}/visible`)
  return response.data
}

export async function getCurrentUserEventBookings(): Promise<EventBooking[]> {
  const response = await api.get<EventBooking[]>('/event-bookings/me')
  return response.data
}

export async function getBookedServicesByEventIdAndUserId(
  eventId: number,
  userId: number,
): Promise<EventBookedService[]> {
  const response = await api.get<EventBookedService[]>(
    `/event-booking-services/event/${eventId}/user/${userId}`,
  )

  return response.data
}

export async function getCurrentUserBookedServicesByEventId(
  eventId: number,
): Promise<EventBookedService[]> {
  const response = await api.get<EventBookedService[]>(`/event-booking-services/event/${eventId}/me`)

  return response.data
}

export async function cancelEventBooking(bookingId: number): Promise<void> {
  await api.delete(`/event-bookings/${bookingId}`)
}

export async function updateOwnEventBookingVisibility(
  bookingId: number,
  payload: UpdateEventBookingVisibilityPayload,
): Promise<EventBooking> {
  const response = await api.patch<EventBooking>(`/event-bookings/${bookingId}/visibility`, payload)
  return response.data
}
