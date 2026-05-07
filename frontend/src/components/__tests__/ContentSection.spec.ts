import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ContentSection from '@/components/ContentSection.vue'

describe('ContentSection', () => {
  it('renders heading, action slot and content', () => {
    const wrapper = mount(ContentSection, {
      props: {
        eyebrow: 'Garage',
        title: 'Eventos destacados',
        hint: 'Próximas tandas',
      },
      slots: {
        action: '<a href="/events">Ver todos</a>',
        default: '<article>Jarama</article>',
      },
    })

    expect(wrapper.text()).toContain('Garage')
    expect(wrapper.text()).toContain('Eventos destacados')
    expect(wrapper.text()).toContain('Próximas tandas')
    expect(wrapper.get('a').text()).toBe('Ver todos')
    expect(wrapper.get('article').text()).toBe('Jarama')
  })

  it('prioritizes loading, error and empty states before content', () => {
    expect(mount(ContentSection, {
      props: { loading: true, loadingMessage: 'Cargando...' },
      slots: { default: '<article>Contenido</article>' },
    }).text()).toBe('Cargando...')

    expect(mount(ContentSection, {
      props: { error: 'No se pudo cargar' },
      slots: { default: '<article>Contenido</article>' },
    }).text()).toBe('No se pudo cargar')

    expect(mount(ContentSection, {
      props: { empty: true, emptyMessage: 'Sin resultados' },
      slots: { default: '<article>Contenido</article>' },
    }).text()).toBe('Sin resultados')
  })

  it('renders plain content without heading when no heading data is provided', () => {
    const wrapper = mount(ContentSection, {
      slots: {
        default: '<article>Contenido directo</article>',
      },
    })

    expect(wrapper.find('.section-heading').exists()).toBe(false)
    expect(wrapper.get('article').text()).toBe('Contenido directo')
  })
})
