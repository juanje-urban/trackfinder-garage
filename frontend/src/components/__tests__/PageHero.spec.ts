import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PageHero from '@/components/PageHero.vue'

const props = {
  eyebrow: 'Garage',
  title: 'Encuentra tu próxima tanda',
  description: 'Circuitos, eventos y tiempos en un mismo sitio.',
}

describe('PageHero', () => {
  it('renders the standard hero with aside content', () => {
    const wrapper = mount(PageHero, {
      props,
      slots: {
        aside: '<aside data-test="aside">Dato destacado</aside>',
      },
    })

    expect(wrapper.classes()).toContain('page-hero--with-aside')
    expect(wrapper.classes()).not.toContain('page-hero--immersive')
    expect(wrapper.text()).toContain('Encuentra tu próxima tanda')
    expect(wrapper.get('[data-test="aside"]').text()).toBe('Dato destacado')
  })

  it('renders the immersive hero when an image is provided', () => {
    const wrapper = mount(PageHero, {
      props: {
        ...props,
        imageUrl: '/tracks/jarama.jpg',
        mediaAlt: 'Circuito del Jarama',
      },
    })

    expect(wrapper.classes()).toContain('page-hero--immersive')
    expect(wrapper.get('img').attributes('src')).toBe('/tracks/jarama.jpg')
    expect(wrapper.get('img').attributes('alt')).toBe('Circuito del Jarama')
  })
})
