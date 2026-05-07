import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AuthSession } from '@/types/auth'

vi.mock('@/services/authService', () => ({
  getCurrentSession: vi.fn(),
}))

const storageKey = 'trackfinder-garage.auth'

const storedSession: AuthSession = {
  userId: 1,
  displayName: 'Juanje',
  email: 'juanje@example.com',
  roleName: 'USER',
  authorizationHeader: 'Basic token',
}

describe('useAuth stored session bootstrap', () => {
  beforeEach(() => {
    vi.resetModules()
    globalThis.window.localStorage.clear()
  })

  it('loads a valid stored session when the composable module starts', async () => {
    globalThis.window.localStorage.setItem(storageKey, JSON.stringify(storedSession))

    const { getAuthorizationHeader, useAuth } = await import('@/composables/useAuth')
    const auth = useAuth()

    expect(auth.session.value).toEqual(storedSession)
    expect(auth.isAuthenticated.value).toBe(true)
    expect(getAuthorizationHeader()).toBe('Basic token')
  })

  it('removes stored sessions with an invalid shape', async () => {
    globalThis.window.localStorage.setItem(storageKey, JSON.stringify({
      userId: '1',
      displayName: 'Juanje',
      email: 'juanje@example.com',
      authorizationHeader: 'Basic token',
    }))

    const { useAuth } = await import('@/composables/useAuth')

    expect(useAuth().session.value).toBeNull()
    expect(globalThis.window.localStorage.getItem(storageKey)).toBeNull()
  })

  it('removes stored sessions that are not valid JSON', async () => {
    globalThis.window.localStorage.setItem(storageKey, '{bad json')

    const { useAuth } = await import('@/composables/useAuth')

    expect(useAuth().session.value).toBeNull()
    expect(globalThis.window.localStorage.getItem(storageKey)).toBeNull()
  })
})
