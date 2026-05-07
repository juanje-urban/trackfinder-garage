import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AdminUsersPanel from '@/components/admin/AdminUsersPanel.vue'
import type { Organizer } from '@/types/organizer'
import type { AdminUser } from '@/types/user'

const adminUser: AdminUser = {
  id: 1,
  displayName: 'admin',
  email: 'admin@example.com',
  created: '2026-01-01T10:00:00',
  enabled: true,
  name: 'Admin',
  surname: 'Principal',
  address: 'Calle Admin 1',
  phone: '600000000',
  roleId: 1,
  roleName: 'ADMIN',
}

const standardUser: AdminUser = {
  ...adminUser,
  id: 3,
  displayName: 'piloto',
  email: 'piloto@example.com',
  name: 'Piloto',
  surname: '',
  address: '',
  phone: '',
  roleName: 'USER',
}

const organizerUser: AdminUser = {
  ...adminUser,
  id: 2,
  displayName: 'trackevents',
  email: 'trackevents@example.com',
  name: 'Track',
  surname: 'Events',
  roleName: 'ORGANIZER',
}

const organizer: Organizer = {
  idUser: 2,
  displayName: 'trackevents',
  email: 'trackevents@example.com',
  name: 'Track',
  surname: 'Events',
  address: 'Calle Motor 1',
  phone: '600000001',
  created: '2026-01-01T10:00:00',
  userEnabled: true,
  roleId: 2,
  roleName: 'ORGANIZER',
  legalName: 'TrackEvents S.L.',
  cif: 'B12345678',
  organizerEnabled: false,
}

function mountPanel(overrides = {}) {
  return mount(AdminUsersPanel, {
    props: {
      userError: '',
      activeUserTab: 'admins',
      userBusyId: null,
      adminUsers: [adminUser],
      organizerUsers: [{ user: organizerUser, organizer }],
      standardUsers: [standardUser],
      isProtectedDefaultAdmin: (user: AdminUser) => user.displayName === 'admin',
      ...overrides,
    },
  })
}

describe('AdminUsersPanel', () => {
  it('renders protected admins and emits tab changes', async () => {
    const wrapper = mountPanel()

    await wrapper.findAll('.pill-tab')[1].trigger('click')

    expect(wrapper.text()).toContain('Gestion general de usuarios')
    expect(wrapper.text()).toContain('admin@example.com')
    expect(wrapper.text()).toContain('Protegido')
    expect(wrapper.emitted('update:activeUserTab')).toEqual([['organizers']])
  })

  it('emits toggle for non protected admins', async () => {
    const wrapper = mountPanel({
      adminUsers: [{ ...adminUser, displayName: 'admin2', id: 4 }],
    })

    await wrapper.get('.admin-user-card__button').trigger('click')

    expect(wrapper.emitted('toggleUser')).toEqual([[
      { ...adminUser, displayName: 'admin2', id: 4 },
    ]])
  })

  it('renders organizer details and pending note', async () => {
    const wrapper = mountPanel({
      activeUserTab: 'organizers',
    })

    await wrapper.get('.admin-user-card__button').trigger('click')

    expect(wrapper.text()).toContain('TrackEvents S.L.')
    expect(wrapper.text()).toContain('CIF: B12345678')
    expect(wrapper.text()).toContain('Solicitud de organizador pendiente')
    expect(wrapper.emitted('toggleUser')).toEqual([[organizerUser]])
  })

  it('renders organizer users without fiscal details when organizer record is missing', () => {
    const wrapper = mountPanel({
      activeUserTab: 'organizers',
      organizerUsers: [{ user: organizerUser, organizer: null }],
    })

    expect(wrapper.text()).toContain('trackevents')
    expect(wrapper.text()).not.toContain('CIF:')
    expect(wrapper.text()).not.toContain('Solicitud de organizador pendiente')
  })

  it('renders standard users and emits toggle', async () => {
    const wrapper = mountPanel({
      activeUserTab: 'users',
    })

    await wrapper.get('.admin-user-card__button').trigger('click')

    expect(wrapper.text()).toContain('piloto@example.com')
    expect(wrapper.emitted('toggleUser')).toEqual([[standardUser]])
  })

  it('renders standard user empty state', () => {
    const wrapper = mountPanel({
      activeUserTab: 'users',
      userError: 'No se pudieron cargar usuarios',
      standardUsers: [],
    })

    expect(wrapper.text()).toContain('No se pudieron cargar usuarios')
    expect(wrapper.text()).toContain('No hay usuarios registrados en este momento.')
  })
})
