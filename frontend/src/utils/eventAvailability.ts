export type EventAvailabilityState = 'closed' | 'full' | 'urgent' | 'limited' | 'open'

// Traduzco plazas y fecha a un estado visual fácil de pintar en tarjetas y badges.
export function getEventAvailabilityState(
  remainingCapacity: number,
  isPastEvent = false,
): EventAvailabilityState {
  if (isPastEvent) {
    return 'closed'
  }

  if (remainingCapacity <= 0) {
    return 'full'
  }

  if (remainingCapacity <= 3) {
    return 'urgent'
  }
  if (remainingCapacity <= 10) {
    return 'limited'
  }

  return 'open'
}

export function getEventAvailabilityLabel(remainingCapacity: number, isPastEvent = false): string {
  const state = getEventAvailabilityState(remainingCapacity, isPastEvent)

  if (state === 'closed') {
    return 'Evento finalizado'
  }
  if (state === 'full') {
    return 'Aforo completo'
  }
  if (state === 'urgent') {
    return '\u00DAltimas plazas'
  }
  if (state === 'limited') {
    return 'Plazas limitadas'
  }

  return 'Reservas abiertas'
}

export function getEventRemainingLabel(remainingCapacity: number, isPastEvent = false): string {
  if (isPastEvent) {
    return 'Reservas cerradas para este evento'
  }

  if (remainingCapacity <= 0) {
    return 'No quedan plazas disponibles para este evento'
  }

  return `${remainingCapacity} plazas disponibles para este evento`
}
