import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AdminOrganizerRequestsPanel from '@/components/admin/AdminOrganizerRequestsPanel.vue'
import type { Organizer } from '@/types/organizer'

const organizer: Organizer = {
  idUser: 2,
  displayName: 'trackevents',
  email: 'trackevents@example.com',
  name: 'Track',
  surname: 'Events',
  address: 'Calle Motor 1',
  phone: '600000000',
  created: '2026-01-01T10:00:00',
  userEnabled: true,
  roleId: 2,
  roleName: 'ORGANIZER',
  legalName: 'TrackEvents S.L.',
  cif: 'B12345678',
  organizerEnabled: false,
}

describe('AdminOrganizerRequestsPanel', () => {
  it('renders empty and error states', () => {
    const wrapper = mount(AdminOrganizerRequestsPanel, {
      props: {
        organizerError: 'No se pudieron cargar solicitudes',
        pendingOrganizers: [],
        organizerBusyId: null,
      },
    })

    expect(wrapper.text()).toContain('No se pudieron cargar solicitudes')
    expect(wrapper.text()).toContain('No hay solicitudes pendientes ahora mismo.')
  })

  it('renders pending organizer details and emits approve/deny', async () => {
    const wrapper = mount(AdminOrganizerRequestsPanel, {
      props: {
        organizerError: '',
        pendingOrganizers: [organizer],
        organizerBusyId: null,
      },
    })

    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')
    await buttons[1].trigger('click')

    expect(wrapper.text()).toContain('TrackEvents S.L.')
    expect(wrapper.text()).toContain('trackevents')
    expect(wrapper.text()).toContain('B12345678')
    expect(wrapper.emitted('approve')).toEqual([[organizer]])
    expect(wrapper.emitted('deny')).toEqual([[organizer]])
  })

  it('disables organizer actions while the request is busy', () => {
    const wrapper = mount(AdminOrganizerRequestsPanel, {
      props: {
        organizerError: '',
        pendingOrganizers: [organizer],
        organizerBusyId: organizer.idUser,
      },
    })

    expect(wrapper.findAll('button').every((button) => button.attributes('disabled') !== undefined)).toBe(true)
  })
})
