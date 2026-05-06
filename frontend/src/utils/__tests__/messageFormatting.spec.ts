import { describe, expect, it } from 'vitest'
import {
  formatMessageRoleLabel,
  formatMessageTimestamp,
} from '@/utils/messageFormatting'

function normalizeSpaces(value: string): string {
  return value.replace(/[\u00a0\u202f]/gu, ' ')
}

describe('formatMessageTimestamp', () => {
  it('formats timestamps as Spanish date and time', () => {
    const formatted = normalizeSpaces(formatMessageTimestamp('2026-07-12T08:05:00'))

    expect(formatted).toContain('12/07/2026')
    expect(formatted).toContain('08:05')
  })
})

describe('formatMessageRoleLabel', () => {
  it.each([
    ['ADMIN', 'Administrador'],
    [' organizer ', 'Organizador'],
    ['user', 'Usuario'],
    [null, ''],
    ['UNKNOWN', ''],
  ] as const)('maps %s to %s', (roleName, expectedLabel) => {
    expect(formatMessageRoleLabel(roleName)).toBe(expectedLabel)
  })
})
