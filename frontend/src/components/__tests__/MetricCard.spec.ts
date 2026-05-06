import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MetricCard from '@/components/MetricCard.vue'

describe('MetricCard', () => {
  it('renders label and value', () => {
    const wrapper = mount(MetricCard, {
      props: {
        label: 'Reservas',
        value: '24',
      },
    })

    expect(wrapper.text()).toContain('Reservas')
    expect(wrapper.text()).toContain('24')
    expect(wrapper.find('.metric-card__hint').exists()).toBe(false)
  })

  it('renders hint and tone class when provided', () => {
    const wrapper = mount(MetricCard, {
      props: {
        label: 'Ingresos',
        value: '1.200 €',
        hint: 'Base + servicios',
        tone: 'success',
      },
    })

    expect(wrapper.text()).toContain('Base + servicios')
    expect(wrapper.classes()).toContain('metric-card--success')
  })
})
