import { describe, expect, it } from 'vitest'
import { formatCurrency, formatDisplayDate, formatLapTime } from '@/utils/format'

function normalizeSpaces(value: string): string {
  return value.replace(/\s/u, ' ')
}

describe('formatDisplayDate', () => {
  it('formats ISO dates as Spanish local dates', () => {
    expect(formatDisplayDate('2026-07-12')).toBe('12 de julio de 2026')
  })

  it('returns the original value when the date is not valid', () => {
    expect(formatDisplayDate('sin-fecha')).toBe('sin-fecha')
  })
})

describe('formatCurrency', () => {
  it('formats euro amounts without unnecessary decimals', () => {
    expect(normalizeSpaces(formatCurrency(225))).toBe('225 €')
  })

  it('keeps useful decimal precision', () => {
    expect(normalizeSpaces(formatCurrency(225.5))).toBe('225,5 €')
  })
})

describe('formatLapTime', () => {
  it('formats milliseconds as m:ss.mmm', () => {
    expect(formatLapTime(102315)).toBe('1:42.315')
  })

  it('pads seconds and milliseconds', () => {
    expect(formatLapTime(7)).toBe('0:00.007')
  })
})
