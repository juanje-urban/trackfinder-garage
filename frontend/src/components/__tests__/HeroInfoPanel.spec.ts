import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import HeroInfoPanel from '@/components/HeroInfoPanel.vue'

describe('HeroInfoPanel', () => {
  it('renders label, highlighted slot content and caption', () => {
    const wrapper = mount(HeroInfoPanel, {
      props: {
        label: 'Próxima cita',
        caption: 'Inscripciones abiertas',
      },
      slots: {
        default: '<strong>Jarama</strong><span>12 julio</span>',
      },
    })

    expect(wrapper.text()).toContain('Próxima cita')
    expect(wrapper.text()).toContain('Jarama')
    expect(wrapper.text()).toContain('12 julio')
    expect(wrapper.text()).toContain('Inscripciones abiertas')
  })
})
