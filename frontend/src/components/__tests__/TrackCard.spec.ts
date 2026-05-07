import { mount, RouterLinkStub } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import TrackCard from '@/components/TrackCard.vue'
import type { Track } from '@/types/track'

const baseTrack: Track = {
  id: 7,
  name: 'Circuito de prueba',
  shortName: 'prueba',
  location: 'Madrid',
  description: 'Trazado usado para validar assets locales.',
}

function mountCard(track: Track = baseTrack) {
  return mount(TrackCard, {
    props: { track },
    global: {
      stubs: {
        RouterLink: RouterLinkStub,
      },
    },
  })
}

describe('TrackCard', () => {
  it('renders track information and links to filtered events', () => {
    const wrapper = mountCard()

    expect(wrapper.text()).toContain('Circuito de prueba')
    expect(wrapper.text()).toContain('Madrid')
    expect(wrapper.text()).toContain('Trazado usado para validar assets locales.')
    expect(wrapper.getComponent(RouterLinkStub).props('to')).toEqual({
      path: '/events',
      query: { trackId: '7' },
    })
  })

  it('falls back to generated visual styles when the track has no asset', () => {
    const wrapper = mountCard({
      ...baseTrack,
      id: 8,
      shortName: 'sin_asset',
    })

    expect(wrapper.get('.track-card__visual').attributes('style')).toContain('--track-start')
  })
})
