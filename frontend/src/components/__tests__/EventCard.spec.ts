import { mount, RouterLinkStub } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventCard from '@/components/EventCard.vue'
import type { Event } from '@/types/event'

const baseEvent: Event = {
  id: 12,
  organizerId: 3,
  organizerLegalName: 'TrackEvents S.L.',
  trackId: 1,
  trackName: 'Circuito del Jarama',
  trackShortName: 'prueba',
  eventDate: '2026-07-12',
  basePrice: 125,
  maxParticipants: 20,
  remainingCapacity: 5,
  description: 'Tandas libres de mañana',
}

function mountCard(event: Event = baseEvent) {
  return mount(EventCard, {
    props: { event },
    global: {
      stubs: {
        RouterLink: RouterLinkStub,
      },
    },
  })
}

describe('EventCard', () => {
  it('renders event information, link and occupancy progress', () => {
    const wrapper = mountCard()

    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('TrackEvents S.L.')
    expect(wrapper.text()).toContain('Aforo 20')
    expect(wrapper.text()).toContain('5 plazas disponibles')
    expect(wrapper.text()).toContain('125')
    expect(wrapper.getComponent(RouterLinkStub).props('to')).toBe('/events/12')
    expect(wrapper.get('.event-card__progress span').attributes('style')).toContain('width: 75%;')
  })

  it('marks full events and uses fallback visual styles when there is no track image', () => {
    const wrapper = mountCard({
      ...baseEvent,
      id: 13,
      trackShortName: 'sin_asset',
      remainingCapacity: 0,
    })

    expect(wrapper.classes()).toContain('event-card--full')
    expect(wrapper.text()).toContain('Aforo completo')
    expect(wrapper.get('.event-card__media').attributes('style')).toContain('--event-photo-image')
  })
})
