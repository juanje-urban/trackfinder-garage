import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventAvailabilityBadge from '@/components/EventAvailabilityBadge.vue'

describe('EventAvailabilityBadge', () => {
  it('renders the full state', () => {
    const wrapper = mount(EventAvailabilityBadge, {
      props: { remainingCapacity: 0 },
    })

    expect(wrapper.text()).toBe('Aforo completo')
    expect(wrapper.classes()).toContain('event-availability-badge--full')
  })

  it('renders the urgent state', () => {
    const wrapper = mount(EventAvailabilityBadge, {
      props: { remainingCapacity: 2 },
    })

    expect(wrapper.text()).toBe('Últimas plazas')
    expect(wrapper.classes()).toContain('event-availability-badge--urgent')
  })

  it('renders the open state by default', () => {
    const wrapper = mount(EventAvailabilityBadge, {
      props: { remainingCapacity: 30 },
    })

    expect(wrapper.text()).toBe('Reservas abiertas')
    expect(wrapper.classes()).toContain('event-availability-badge--open')
  })

  it('renders the closed state for past events', () => {
    const wrapper = mount(EventAvailabilityBadge, {
      props: { remainingCapacity: 30, isPastEvent: true },
    })

    expect(wrapper.text()).toBe('Evento finalizado')
    expect(wrapper.classes()).toContain('event-availability-badge--closed')
  })
})
