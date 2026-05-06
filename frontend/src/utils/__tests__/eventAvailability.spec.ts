import { describe, expect, it } from 'vitest'
import {
  getEventAvailabilityLabel,
  getEventAvailabilityState,
  getEventRemainingLabel,
} from '@/utils/eventAvailability'

describe('getEventAvailabilityState', () => {
  it.each([
    [-1, 'full'],
    [0, 'full'],
    [3, 'urgent'],
    [10, 'limited'],
    [11, 'open'],
  ] as const)('maps %s remaining seats to %s', (remainingCapacity, expectedState) => {
    expect(getEventAvailabilityState(remainingCapacity)).toBe(expectedState)
  })
})

describe('getEventAvailabilityLabel', () => {
  it.each([
    [0, 'Aforo completo'],
    [2, 'Últimas plazas'],
    [8, 'Plazas limitadas'],
    [20, 'Reservas abiertas'],
  ] as const)('returns the label for %s remaining seats', (remainingCapacity, expectedLabel) => {
    expect(getEventAvailabilityLabel(remainingCapacity)).toBe(expectedLabel)
  })
})

describe('getEventRemainingLabel', () => {
  it('explains when the event is full', () => {
    expect(getEventRemainingLabel(0)).toBe('No quedan plazas disponibles para este evento')
  })

  it('includes the number of remaining seats', () => {
    expect(getEventRemainingLabel(7)).toBe('7 plazas disponibles para este evento')
  })
})
