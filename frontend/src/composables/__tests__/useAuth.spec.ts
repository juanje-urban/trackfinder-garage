import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getCurrentSession } from '@/services/authService'
import { getAuthorizationHeader, useAuth } from '@/composables/useAuth'
import type { AuthSession } from '@/types/auth'

vi.mock('@/services/authService', () => ({
  getCurrentSession: vi.fn(),
}))

const getCurrentSessionMock = vi.mocked(getCurrentSession)

const session: AuthSession = {
  userId: 1,
  displayName: 'Juanje',
  email: 'juanje@example.com',
  roleName: 'USER',
  authorizationHeader: 'Basic token',
}

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    useAuth().clearSession()
    useAuth().closeAuthDialog()
    globalThis.window.localStorage.clear()
  })

  it('opens and closes the shared auth dialog', () => {
    const auth = useAuth()

    auth.openAuthDialog()
    expect(auth.isDialogOpen.value).toBe(true)

    auth.closeAuthDialog()
    expect(auth.isDialogOpen.value).toBe(false)
  })

  it('stores, exposes and clears the current session', () => {
    const auth = useAuth()

    auth.setSession(session)

    expect(auth.isAuthenticated.value).toBe(true)
    expect(auth.session.value).toEqual(session)
    expect(getAuthorizationHeader()).toBe('Basic token')
    expect(globalThis.window.localStorage.getItem('trackfinder-garage.auth')).toContain('Juanje')

    auth.clearSession()

    expect(auth.isAuthenticated.value).toBe(false)
    expect(getAuthorizationHeader()).toBeNull()
    expect(globalThis.window.localStorage.getItem('trackfinder-garage.auth')).toBeNull()
  })

  it('refreshes the identity while preserving the current authorization header', async () => {
    const auth = useAuth()
    auth.setSession(session)
    getCurrentSessionMock.mockResolvedValue({
      userId: 1,
      displayName: 'Juanje Actualizado',
      email: 'nuevo@example.com',
      roleName: 'ADMIN',
    })

    await auth.refreshSession()

    expect(auth.session.value).toEqual({
      userId: 1,
      displayName: 'Juanje Actualizado',
      email: 'nuevo@example.com',
      roleName: 'ADMIN',
      authorizationHeader: 'Basic token',
    })
  })

  it('clears the session when refresh fails or when there is no active session', async () => {
    const auth = useAuth()

    await auth.refreshSession()
    expect(getCurrentSessionMock).not.toHaveBeenCalled()

    auth.setSession(session)
    getCurrentSessionMock.mockRejectedValue(new Error('expired'))

    await auth.refreshSession()

    expect(auth.session.value).toBeNull()
  })
})
