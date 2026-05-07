import { mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import AppToast from '@/components/AppToast.vue'
import { useToast } from '@/composables/useToast'

describe('AppToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    useToast().hideToast()
  })

  afterEach(() => {
    useToast().hideToast()
    vi.useRealTimers()
  })

  it('renders the active toast message and tone', () => {
    useToast().showToast('Perfil actualizado', {
      tone: 'neutral',
      durationMs: 1000,
    })

    const wrapper = mount(AppToast)

    expect(wrapper.text()).toContain('Perfil actualizado')
    expect(wrapper.find('.app-toast').classes()).toContain('app-toast--neutral')
  })

  it('closes the toast from the close button', async () => {
    useToast().showToast('Reserva creada', {
      tone: 'success',
      durationMs: 1000,
    })

    const wrapper = mount(AppToast)
    await wrapper.get('button').trigger('click')
    await nextTick()

    expect(wrapper.find('.app-toast').exists()).toBe(false)
  })

  it('uses default success tone and hides automatically', async () => {
    useToast().showToast('Cambios guardados')

    const wrapper = mount(AppToast)

    expect(wrapper.find('.app-toast').classes()).toContain('app-toast--success')
    expect(wrapper.text()).toContain('Cambios guardados')

    vi.advanceTimersByTime(3200)
    await nextTick()

    expect(wrapper.find('.app-toast').exists()).toBe(false)
  })
})
