import { describe, expect, it } from 'vitest'
import { toIsoDate } from '@/utils/date'

describe('toIsoDate', () => {
  it('returns local date values in YYYY-MM-DD format', () => {
    expect(toIsoDate(new Date(2026, 6, 2))).toBe('2026-07-02')
  })

  it('pads month and day with zeroes', () => {
    expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05')
  })
})
