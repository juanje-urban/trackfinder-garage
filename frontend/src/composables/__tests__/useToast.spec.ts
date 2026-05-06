import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useToast } from '@/composables/useToast'

describe('useToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    useToast().hideToast()
  })

  afterEach(() => {
    useToast().hideToast()
    vi.useRealTimers()
  })

  it('opens a toast with message and tone', () => {
    const toast = useToast()

    toast.showToast('Reserva confirmada', { tone: 'success', durationMs: 1000 })

    expect(toast.isOpen.value).toBe(true)
    expect(toast.message.value).toBe('Reserva confirmada')
    expect(toast.tone.value).toBe('success')
  })

  it('auto-hides after the configured duration', () => {
    const toast = useToast()

    toast.showToast('Algo pasó', { tone: 'error', durationMs: 500 })
    vi.advanceTimersByTime(499)
    expect(toast.isOpen.value).toBe(true)

    vi.advanceTimersByTime(1)
    expect(toast.isOpen.value).toBe(false)
  })

  it('clears the previous timeout when showing a new toast', () => {
    const toast = useToast()

    toast.showToast('Primer mensaje', { durationMs: 1000 })
    vi.advanceTimersByTime(700)
    toast.showToast('Segundo mensaje', { durationMs: 1000 })
    vi.advanceTimersByTime(700)

    expect(toast.isOpen.value).toBe(true)
    expect(toast.message.value).toBe('Segundo mensaje')
  })
})
