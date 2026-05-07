import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventDetailHero from '@/components/event-detail/EventDetailHero.vue'
import type { Event } from '@/types/event'
import type { EventAvailabilitySummary } from '@/types/eventDetail'
import type { Track } from '@/types/track'

const event: Event = {
  id: 12,
  organizerId: 3,
  organizerLegalName: 'TrackEvents S.L.',
  trackId: 1,
  trackName: 'Circuito del Jarama',
  trackShortName: 'jarama',
  eventDate: '2026-07-12',
  basePrice: 125,
  maxParticipants: 20,
  remainingCapacity: 4,
  description: 'Tandas libres de mañana.',
}

const track: Track = {
  id: 1,
  name: 'Circuito del Jarama',
  shortName: 'jarama',
  location: 'Madrid',
  description: 'Trazado histórico.',
}

const availability: EventAvailabilitySummary = {
  state: 'urgent',
  label: 'Últimas plazas',
  remainingLabel: '4 plazas disponibles',
}

describe('EventDetailHero', () => {
  it('renders the event detail hero and emits openLayout from the map', async () => {
    const wrapper = mount(EventDetailHero, {
      props: {
        event,
        track,
        availability,
        formattedDate: '12 de julio de 2026',
        heroStyle: { '--event-detail-start': '#111' },
        layoutImage: '/tracks/jarama-layout.svg',
      },
    })

    await wrapper.get('.event-detail__track-map-button').trigger('click')

    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('Madrid')
    expect(wrapper.text()).toContain('TrackEvents S.L.')
    expect(wrapper.text()).toContain('Últimas plazas')
    expect(wrapper.get('.event-detail__track-map-image').attributes('src')).toBe('/tracks/jarama-layout.svg')
    expect(wrapper.emitted('openLayout')).toHaveLength(1)
  })

  it('does not render the map panel without layout image', () => {
    const wrapper = mount(EventDetailHero, {
      props: {
        event,
        track,
        availability,
        formattedDate: '12 de julio de 2026',
        heroStyle: {},
      },
    })

    expect(wrapper.find('.event-detail__track-map').exists()).toBe(false)
  })
})
