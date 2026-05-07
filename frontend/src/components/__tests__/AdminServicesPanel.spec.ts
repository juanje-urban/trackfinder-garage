import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AdminServicesPanel from '@/components/admin/AdminServicesPanel.vue'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'

const activeService: ServiceCatalogItem = {
  id: 1,
  name: 'Box privado',
  description: 'Uso de box durante el evento.',
  allowedForTrack: true,
  allowedForOrganizer: true,
  enabled: true,
}

const inactiveService: ServiceCatalogItem = {
  id: 2,
  name: 'Catering',
  description: 'Servicio opcional de comida.',
  allowedForTrack: false,
  allowedForOrganizer: true,
  enabled: false,
}

describe('AdminServicesPanel', () => {
  it('renders services and emits create, edit and toggle actions', async () => {
    const wrapper = mount(AdminServicesPanel, {
      props: {
        serviceError: '',
        sortedServices: [activeService, inactiveService],
        serviceBusyId: null,
      },
    })

    await wrapper.get('.admin-services__create').trigger('click')
    await wrapper.get('button[aria-label="Editar servicio"]').trigger('click')
    await wrapper.get('button[aria-label="Deshabilitar servicio"]').trigger('click')

    expect(wrapper.text()).toContain('Box privado')
    expect(wrapper.text()).toContain('Catering')
    expect(wrapper.text()).toContain('Circuito')
    expect(wrapper.text()).toContain('Inactivo')
    expect(wrapper.emitted('openCreateService')).toHaveLength(1)
    expect(wrapper.emitted('editService')).toEqual([[activeService]])
    expect(wrapper.emitted('toggleService')).toEqual([[activeService]])
  })

  it('renders error and disables busy toggle action', () => {
    const wrapper = mount(AdminServicesPanel, {
      props: {
        serviceError: 'No se pudo guardar el servicio',
        sortedServices: [inactiveService],
        serviceBusyId: inactiveService.id,
      },
    })

    expect(wrapper.text()).toContain('No se pudo guardar el servicio')
    expect(wrapper.get('button[aria-label="Habilitar servicio"]').attributes('disabled')).toBeDefined()
  })
})
