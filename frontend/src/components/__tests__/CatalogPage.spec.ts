import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import CatalogPage from '@/components/CatalogPage.vue'

const props = {
  heroEyebrow: 'Garage',
  heroTitle: 'Eventos',
  heroDescription: 'Encuentra tu próxima tanda.',
  infoLabel: 'Disponibles',
  infoCaption: 'Eventos publicados',
  sectionEyebrow: 'Catálogo',
  sectionTitle: 'Próximos eventos',
  sectionHint: 'Ordenados por fecha',
  loading: false,
  loadingMessage: 'Cargando eventos...',
  error: '',
  empty: false,
  emptyMessage: 'No hay eventos',
}

describe('CatalogPage', () => {
  it('renders the shared catalog layout with optional slots', () => {
    const wrapper = mount(CatalogPage, {
      props,
      slots: {
        'hero-value': '<strong>12</strong><span>eventos</span>',
        metrics: '<article data-test="metric">Métrica</article>',
        summary: '<section data-test="summary">Resumen</section>',
        toolbar: '<section data-test="toolbar">Filtros</section>',
        default: '<article data-test="card">Jarama</article>',
      },
    })

    expect(wrapper.text()).toContain('Eventos')
    expect(wrapper.text()).toContain('Disponibles')
    expect(wrapper.get('[data-test="metric"]').text()).toBe('Métrica')
    expect(wrapper.get('[data-test="summary"]').text()).toBe('Resumen')
    expect(wrapper.get('[data-test="toolbar"]').text()).toBe('Filtros')
    expect(wrapper.get('[data-test="card"]').text()).toBe('Jarama')
  })

  it('passes loading, error and empty states to the content section', () => {
    expect(mount(CatalogPage, {
      props: {
        ...props,
        loading: true,
      },
      slots: { default: '<article>Contenido</article>' },
    }).text()).toContain('Cargando eventos...')

    expect(mount(CatalogPage, {
      props: {
        ...props,
        error: 'No se pudo cargar',
      },
      slots: { default: '<article>Contenido</article>' },
    }).text()).toContain('No se pudo cargar')

    expect(mount(CatalogPage, {
      props: {
        ...props,
        empty: true,
      },
      slots: { default: '<article>Contenido</article>' },
    }).text()).toContain('No hay eventos')
  })
})
