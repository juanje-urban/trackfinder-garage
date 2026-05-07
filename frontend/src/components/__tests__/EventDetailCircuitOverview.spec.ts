import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventDetailCircuitOverview from '@/components/event-detail/EventDetailCircuitOverview.vue'
import type { Track } from '@/types/track'

const track: Track = {
  id: 1,
  name: 'Circuito del Jarama',
  shortName: 'jarama',
  location: 'Madrid',
  description: 'Trazado histórico con curvas rápidas.',
}

describe('EventDetailCircuitOverview', () => {
  it('renders track information without photo action when no image is available', () => {
    const wrapper = mount(EventDetailCircuitOverview, {
      props: { track },
    })

    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('Madrid')
    expect(wrapper.text()).toContain('Trazado histórico')
    expect(wrapper.find('button').exists()).toBe(false)
  })

  it('renders the second gallery image and emits openPhoto', async () => {
    const wrapper = mount(EventDetailCircuitOverview, {
      props: {
        track,
        secondGalleryImage: '/tracks/jarama-2.jpg',
      },
    })

    await wrapper.get('button').trigger('click')

    expect(wrapper.get('img').attributes('src')).toBe('/tracks/jarama-2.jpg')
    expect(wrapper.get('img').attributes('alt')).toBe('Vista del circuito Circuito del Jarama')
    expect(wrapper.emitted('openPhoto')).toHaveLength(1)
  })
})
