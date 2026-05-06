import { describe, expect, it } from 'vitest'
import {
  isAdminRole,
  isOrganizerRole,
  isUserRole,
  normalizeRoleName,
} from '@/utils/authRoles'

describe('normalizeRoleName', () => {
  it('trims and uppercases role names', () => {
    expect(normalizeRoleName(' organizer ')).toBe('ORGANIZER')
  })

  it('normalizes missing role names to an empty string', () => {
    expect(normalizeRoleName(undefined)).toBe('')
  })
})

describe('role predicates', () => {
  it('detects admin roles', () => {
    expect(isAdminRole('admin')).toBe(true)
    expect(isAdminRole('user')).toBe(false)
  })

  it('detects user roles', () => {
    expect(isUserRole(' USER ')).toBe(true)
    expect(isUserRole('ORGANIZER')).toBe(false)
  })

  it('detects organizer roles', () => {
    expect(isOrganizerRole('organizer')).toBe(true)
    expect(isOrganizerRole(null)).toBe(false)
  })
})
