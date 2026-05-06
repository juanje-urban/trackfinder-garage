import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import DetailInfoCard from '@/components/DetailInfoCard.vue'

describe('DetailInfoCard', () => {
  it('renders title, eyebrow and subtitle', () => {
    const wrapper = mount(DetailInfoCard, {
      props: {
        eyebrow: 'Fecha',
        title: '12 de julio de 2026',
        subtitle: 'Domingo',
      },
    })

    expect(wrapper.text()).toContain('Fecha')
    expect(wrapper.text()).toContain('12 de julio de 2026')
    expect(wrapper.text()).toContain('Domingo')
    expect(wrapper.classes()).toContain('detail-info-card--default')
  })

  it('renders custom body content and tone', () => {
    const wrapper = mount(DetailInfoCard, {
      props: {
        eyebrow: 'Disponibilidad',
        title: 'Últimas plazas',
        tone: 'urgent',
      },
      slots: {
        default: '<p class="extra">Quedan 2 plazas</p>',
      },
    })

    expect(wrapper.find('.extra').text()).toBe('Quedan 2 plazas')
    expect(wrapper.classes()).toContain('detail-info-card--urgent')
  })
})
