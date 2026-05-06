import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import SectionCard from '@/components/SectionCard.vue'

describe('SectionCard', () => {
  it('renders heading, optional description and default slot', () => {
    const wrapper = mount(SectionCard, {
      props: {
        eyebrow: 'Servicios',
        title: 'Configura tu reserva',
        description: 'Selecciona extras disponibles.',
      },
      slots: {
        default: '<button>Reservar</button>',
      },
    })

    expect(wrapper.text()).toContain('Servicios')
    expect(wrapper.text()).toContain('Configura tu reserva')
    expect(wrapper.text()).toContain('Selecciona extras disponibles.')
    expect(wrapper.get('button').text()).toBe('Reservar')
  })

  it('omits the description when it is empty', () => {
    const wrapper = mount(SectionCard, {
      props: {
        eyebrow: 'Circuitos',
        title: 'Trazados',
      },
    })

    expect(wrapper.find('.ui-copy-muted').exists()).toBe(false)
  })
})
