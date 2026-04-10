export type EventAvailabilityState = 'urgent' | 'limited' | 'open'

export function getEventAvailabilityState(
  remainingCapacity: number,
): EventAvailabilityState {
  if (remainingCapacity <= 3) {
    return 'urgent'
  }
  if (remainingCapacity <= 10) {
    return 'limited'
  }

  return 'open'
}

export function getEventAvailabilityLabel(remainingCapacity: number): string {
  const state = getEventAvailabilityState(remainingCapacity)

  if (state === 'urgent') {
    return '\u00DAltimas plazas'
  }
  if (state === 'limited') {
    return 'Plazas limitadas'
  }

  return 'Reservas abiertas'
}

export function getEventRemainingLabel(remainingCapacity: number): string {
  return `${remainingCapacity} plazas disponibles para este evento`
}
