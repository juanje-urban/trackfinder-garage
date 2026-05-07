import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import OrganizerServicesPanel from '@/components/organizer/OrganizerServicesPanel.vue'
import type { OrganizerCatalogService } from '@/types/organizerWorkspace'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'

const catalogService: ServiceCatalogItem = {
  id: 5,
  name: 'Catering',
  description: 'Comida durante el evento.',
  allowedForTrack: false,
  allowedForOrganizer: true,
  enabled: true,
}

const organizerService: OrganizerCatalogService = {
  id: 9,
  organizerId: 2,
  organizerLegalName: 'TrackEvents S.L.',
  serviceId: 5,
  serviceName: 'Catering',
}

function mountPanel(overrides = {}) {
  return mount(OrganizerServicesPanel, {
    props: {
      serviceError: '',
      availableCatalogServices: [catalogService],
      selectedAvailableServiceId: '',
      serviceSubmitting: false,
      organizerServices: [organizerService],
      removingOrganizerServiceId: null,
      isOrganizerServiceLocked: (id: number) => id === organizerService.id,
      ...overrides,
    },
  })
}

describe('OrganizerServicesPanel', () => {
  it('emits selected service, add and remove actions', async () => {
    const wrapper = mountPanel({
      selectedAvailableServiceId: catalogService.id,
    })

    await wrapper.get('select').setValue(String(catalogService.id))
    await wrapper.get('.organizer-toolbar__button').trigger('click')
    await wrapper.get('button[aria-label="Retirar servicio del catálogo"]').trigger('click')

    expect(wrapper.text()).toContain('Catering')
    expect(wrapper.text()).toContain('Vinculado a eventos futuros')
    expect(wrapper.emitted('update:selectedAvailableServiceId')).toEqual([[catalogService.id]])
    expect(wrapper.emitted('addService')).toHaveLength(1)
    expect(wrapper.emitted('removeService')).toEqual([[organizerService.id]])
  })

  it('renders empty states and disables add/remove controls when busy', () => {
    const wrapper = mountPanel({
      serviceError: 'No se pudo actualizar el catálogo',
      availableCatalogServices: [],
      selectedAvailableServiceId: '',
      serviceSubmitting: true,
      removingOrganizerServiceId: organizerService.id,
    })

    expect(wrapper.text()).toContain('No se pudo actualizar el catálogo')
    expect(wrapper.text()).toContain('Ya has incorporado todos los servicios disponibles')
    expect(wrapper.get('.organizer-toolbar__button').attributes('disabled')).toBeDefined()
    expect(wrapper.get('button[aria-label="Retirar servicio del catálogo"]').attributes('disabled')).toBeDefined()
  })
})
