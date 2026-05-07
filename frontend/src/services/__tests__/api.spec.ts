import { describe, expect, it, vi } from 'vitest'
import { api } from '@/services/api'

const mocks = vi.hoisted(() => {
  const requestUse = vi.fn()

  return {
    requestUse,
    axiosCreate: vi.fn(() => ({
      interceptors: {
        request: {
          use: requestUse,
        },
      },
    })),
    getAuthorizationHeader: vi.fn(),
  }
})

vi.mock('axios', () => ({
  default: {
    create: mocks.axiosCreate,
  },
}))

vi.mock('@/composables/useAuth', () => ({
  getAuthorizationHeader: mocks.getAuthorizationHeader,
}))

describe('api', () => {
  it('creates a shared axios instance with the configured base URL', () => {
    expect(api).toBeDefined()
    expect(mocks.axiosCreate.mock.calls[0]?.[0]).toHaveProperty('baseURL')
  })

  it('does not add authorization to public auth endpoints', () => {
    const interceptor = mocks.requestUse.mock.calls[0]?.[0]
    mocks.getAuthorizationHeader.mockReturnValue('Basic token')

    const config = interceptor({ url: '/auth/login', headers: {} })

    expect(config.headers.Authorization).toBeUndefined()
  })

  it('adds the stored authorization header to private requests', () => {
    const interceptor = mocks.requestUse.mock.calls[0]?.[0]
    mocks.getAuthorizationHeader.mockReturnValue('Basic token')

    const config = interceptor({ url: '/tracks' })

    expect(config.headers.Authorization).toBe('Basic token')
  })

  it('keeps private requests untouched when there is no active session', () => {
    const interceptor = mocks.requestUse.mock.calls[0]?.[0]
    mocks.getAuthorizationHeader.mockReturnValue(null)

    const config = interceptor({ url: '/tracks' })

    expect(config.headers).toBeUndefined()
  })
})
