import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventServiceConfigurator from '@/components/event-detail/EventServiceConfigurator.vue'
import type { DisplayServiceGroup } from '@/types/eventDetail'

const serviceGroups: DisplayServiceGroup[] = [
  {
    id: 'track',
    eyebrow: 'Circuito',
    title: 'Servicios del circuito',
    emptyMessage: 'Sin servicios de circuito',
    services: [
      { id: 10, name: 'Box', price: 35, source: 'track' },
      { id: 11, name: 'Cronometraje', price: 18, source: 'track' },
    ],
  },
  {
    id: 'organizer',
    eyebrow: 'Organizador',
    title: 'Servicios del organizador',
    emptyMessage: 'Sin servicios de organizador',
    services: [],
  },
]

function mountConfigurator(overrides = {}) {
  return mount(EventServiceConfigurator, {
    props: {
      isPastEvent: false,
      servicesLoading: false,
      servicesError: '',
      serviceGroups,
      selectedServiceIds: [],
      isLocked: false,
      ...overrides,
    },
  })
}

describe('EventServiceConfigurator', () => {
  it('does not render for past events', () => {
    expect(mountConfigurator({ isPastEvent: true }).text()).toBe('')
  })

  it('renders loading, error and empty states', () => {
    expect(mountConfigurator({ servicesLoading: true }).text()).toContain(
      'Cargando servicios disponibles...',
    )

    expect(mountConfigurator({ servicesError: 'No se pudieron cargar' }).text()).toContain(
      'No se pudieron cargar',
    )

    expect(mountConfigurator({
      serviceGroups: serviceGroups.map((group) => ({ ...group, services: [] })),
    }).text()).toContain('No hay servicios adicionales disponibles para este evento.')
  })

  it('renders available services and selected state', () => {
    const wrapper = mountConfigurator({ selectedServiceIds: [10] })

    expect(wrapper.text()).toContain('Servicios del circuito')
    expect(wrapper.text()).toContain('Box')
    expect(wrapper.text()).toContain('35')
    expect(wrapper.find('.event-detail__service-row--selected').text()).toContain('Box')
    expect(wrapper.text()).toContain('Sin servicios de organizador')
  })

  it('emits the selected service id on checkbox change', async () => {
    const wrapper = mountConfigurator()

    await wrapper.get('input[type="checkbox"]').trigger('change')

    expect(wrapper.emitted('toggleService')).toEqual([[10]])
  })

  it('locks checkboxes when the booking already exists', () => {
    const wrapper = mountConfigurator({ isLocked: true })

    expect(wrapper.get('input[type="checkbox"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('.event-detail__service-row--locked').exists()).toBe(true)
  })
})
