import { describe, expect, it } from 'vitest'
import { resolveApiErrorMessage } from '@/utils/apiErrors'

function axiosError(response?: { status?: number; data?: unknown }) {
  return {
    isAxiosError: true,
    response,
  }
}

describe('resolveApiErrorMessage', () => {
  it('returns backend error messages when available', () => {
    const message = resolveApiErrorMessage(
      axiosError({ data: { error: 'El circuito ya existe' } }),
      { fallback: 'No se pudo guardar' },
    )

    expect(message).toBe('El circuito ya existe')
  })

  it('maps known backend messages to friendlier copy', () => {
    const message = resolveApiErrorMessage(
      axiosError({ data: { error: 'email already exists' } }),
      {
        fallback: 'No se pudo guardar',
        matches: [{ includes: 'email', message: 'Ya existe ese correo.' }],
      },
    )

    expect(message).toBe('Ya existe ese correo.')
  })

  it('uses the first field error when the backend returns validation errors', () => {
    const message = resolveApiErrorMessage(
      axiosError({ data: { email: '', displayName: 'Alias obligatorio' } }),
      { fallback: 'No se pudo registrar' },
    )

    expect(message).toBe('Alias obligatorio')
  })

  it('uses status messages when there is no backend text', () => {
    const message = resolveApiErrorMessage(
      axiosError({ status: 404, data: null }),
      {
        fallback: 'Error inesperado',
        statusMessages: { 404: 'No encontrado' },
      },
    )

    expect(message).toBe('No encontrado')
  })

  it('falls back when backend data has no usable text or known status', () => {
    const message = resolveApiErrorMessage(
      axiosError({ status: 500, data: { email: '   ' } }),
      {
        fallback: 'Error inesperado',
        statusMessages: { 404: 'No encontrado' },
      },
    )

    expect(message).toBe('Error inesperado')
  })

  it('falls back for unknown errors', () => {
    expect(resolveApiErrorMessage(new Error('boom'), { fallback: 'Error genérico' })).toBe(
      'Error genérico',
    )
  })
})
