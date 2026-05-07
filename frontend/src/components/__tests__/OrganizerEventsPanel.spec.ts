import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import OrganizerEventsPanel from '@/components/organizer/OrganizerEventsPanel.vue'
import type { Event } from '@/types/event'
import type { EventServiceItem } from '@/types/eventService'
import type { OrganizerManagedEvent, OrganizerWorkspaceEventStats } from '@/types/organizerWorkspace'

function event(overrides: Partial<Event> = {}): Event {
  return {
    id: 1,
    organizerId: 2,
    organizerLegalName: 'TrackEvents S.L.',
    trackId: 1,
    trackName: 'Circuito del Jarama',
    trackShortName: 'jarama',
    eventDate: '2026-07-12',
    basePrice: 125,
    maxParticipants: 20,
    remainingCapacity: 20,
    description: 'Tandas libres.',
    ...overrides,
  }
}

function stats(overrides: Partial<OrganizerWorkspaceEventStats> = {}): OrganizerWorkspaceEventStats {
  return {
    eventId: 1,
    trackName: 'Circuito del Jarama',
    eventDate: '2026-07-12',
    bookings: 0,
    soldServices: 0,
    remainingCapacity: 20,
    totalCapacity: 20,
    baseRevenue: 0,
    serviceRevenue: 0,
    grossRevenue: 0,
    ...overrides,
  }
}

function service(overrides: Partial<EventServiceItem> = {}): EventServiceItem {
  return {
    id: 1,
    eventId: 1,
    trackServiceId: 1,
    trackServiceName: 'Box del circuito',
    organizerServiceId: null,
    organizerServiceName: null,
    price: 30,
    hasBookings: false,
    ...overrides,
  }
}

function managed(overrides: Partial<OrganizerManagedEvent> = {}): OrganizerManagedEvent {
  return {
    event: event(),
    services: [service()],
    stats: stats(),
    ...overrides,
  }
}

describe('OrganizerEventsPanel', () => {
  it('renders errors and empty state', () => {
    const wrapper = mount(OrganizerEventsPanel, {
      props: {
        eventError: 'No se pudieron cargar eventos',
        events: [],
        todayIso: '2026-05-07',
      },
    })

    expect(wrapper.text()).toContain('No se pudieron cargar eventos')
    expect(wrapper.text()).toContain('Todavía no has creado ningún evento.')
  })

  it('emits create, edit and delete for future events without bookings', async () => {
    const managedEvent = managed()
    const wrapper = mount(OrganizerEventsPanel, {
      props: {
        eventError: '',
        events: [managedEvent],
        todayIso: '2026-05-07',
      },
    })

    await wrapper.get('.organizer-events__button').trigger('click')
    await wrapper.get('button[aria-label="Editar evento"]').trigger('click')
    await wrapper.get('button[aria-label="Eliminar evento"]').trigger('click')

    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('Box del circuito')
    expect(wrapper.emitted('create')).toHaveLength(1)
    expect(wrapper.emitted('edit')).toEqual([[managedEvent]])
    expect(wrapper.emitted('delete')).toEqual([[managedEvent]])
  })

  it('hides actions for past events and delete for events with bookings', () => {
    const wrapper = mount(OrganizerEventsPanel, {
      props: {
        eventError: '',
        events: [
          managed({
            event: event({ id: 2, eventDate: '2026-01-01' }),
            services: [service({ organizerServiceName: 'Catering', trackServiceName: null })],
            stats: stats({ eventId: 2, bookings: 0, eventDate: '2026-01-01' }),
          }),
          managed({
            event: event({ id: 3, eventDate: '2026-08-01' }),
            stats: stats({ eventId: 3, bookings: 3, eventDate: '2026-08-01' }),
          }),
        ],
        todayIso: '2026-05-07',
      },
    })

    expect(wrapper.text()).toContain('Evento finalizado')
    expect(wrapper.text()).toContain('Catering')
    expect(wrapper.findAll('button[aria-label="Editar evento"]')).toHaveLength(1)
    expect(wrapper.find('button[aria-label="Eliminar evento"]').exists()).toBe(false)
  })
})
