import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AdminTracksPanel from '@/components/admin/AdminTracksPanel.vue'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'
import type { Track } from '@/types/track'
import type { TrackServiceAssignment } from '@/types/trackService'

const track: Track = {
  id: 1,
  name: 'Circuito del Jarama',
  shortName: 'jarama',
  location: 'Madrid',
  description: 'Trazado histórico.',
}

const service: ServiceCatalogItem = {
  id: 3,
  name: 'Box privado',
  description: 'Uso de box.',
  allowedForTrack: true,
  allowedForOrganizer: false,
  enabled: true,
}

const assignment: TrackServiceAssignment = {
  id: 7,
  trackId: 1,
  trackName: 'Circuito del Jarama',
  serviceId: 3,
  serviceName: 'Box privado',
}

function mountPanel(overrides = {}) {
  return mount(AdminTracksPanel, {
    props: {
      trackError: '',
      serviceError: '',
      sortedTracks: [track],
      selectedTrackId: 1,
      selectedServiceId: 3,
      assignableServices: [service],
      selectedTrackAssignments: [assignment],
      assignmentSaving: false,
      assignmentDeletingId: null,
      ...overrides,
    },
  })
}

describe('AdminTracksPanel', () => {
  it('renders tracks and assignments and emits main actions', async () => {
    const wrapper = mountPanel()

    await wrapper.get('.admin-tracks__create').trigger('click')
    await wrapper.get('button[aria-label="Editar circuito"]').trigger('click')
    await wrapper.get('.admin-track-assignment-toolbar__button').trigger('click')
    await wrapper.get('button[aria-label="Eliminar servicio del circuito"]').trigger('click')

    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('Assets: jarama')
    expect(wrapper.text()).toContain('Box privado')
    expect(wrapper.emitted('openCreateTrack')).toHaveLength(1)
    expect(wrapper.emitted('editTrack')).toEqual([[track]])
    expect(wrapper.emitted('addAssignment')).toHaveLength(1)
    expect(wrapper.emitted('removeAssignment')).toEqual([[assignment]])
  })

  it('emits selected ids as numbers and empty option as blank value', async () => {
    const wrapper = mountPanel()
    const selects = wrapper.findAll('select')

    await selects[0].setValue('')
    await selects[0].setValue('1')
    await selects[1].setValue('3')

    expect(wrapper.emitted('update:selectedTrackId')).toEqual([[''], [1]])
    expect(wrapper.emitted('update:selectedServiceId')).toEqual([[3]])
  })

  it('renders errors and disables assignment controls when needed', () => {
    const wrapper = mountPanel({
      trackError: 'No se pudieron cargar circuitos',
      serviceError: 'No se pudieron cargar servicios',
      selectedTrackId: '',
      selectedServiceId: '',
      assignableServices: [],
      selectedTrackAssignments: [],
      assignmentSaving: true,
    })

    expect(wrapper.text()).toContain('No se pudieron cargar circuitos')
    expect(wrapper.text()).toContain('No se pudieron cargar servicios')
    expect(wrapper.text()).toContain('Este circuito todavía no tiene servicios vinculados.')
    expect(wrapper.findAll('select')[1].attributes('disabled')).toBeDefined()
    expect(wrapper.get('.admin-track-assignment-toolbar__button').attributes('disabled')).toBeDefined()
  })
})
