import { describe, expect, it } from 'vitest'
import { getDisplayNameMonogram } from '@/utils/identity'

describe('getDisplayNameMonogram', () => {
  it('uses the first letter of two display-name parts', () => {
    expect(getDisplayNameMonogram('fernando.alonso')).toBe('FA')
  })

  it('uses two first characters when there is only one part', () => {
    expect(getDisplayNameMonogram('juanje')).toBe('JU')
  })

  it('trims whitespace and separators', () => {
    expect(getDisplayNameMonogram('  apex-hunter  ')).toBe('AH')
  })

  it('returns an empty monogram for blank display names', () => {
    expect(getDisplayNameMonogram('   ')).toBe('')
  })
})
