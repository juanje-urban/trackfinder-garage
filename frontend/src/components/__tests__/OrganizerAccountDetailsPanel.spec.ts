import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import OrganizerAccountDetailsPanel from '@/components/profile/OrganizerAccountDetailsPanel.vue'
import type { OrganizerProfile } from '@/types/organizer'
import type { ProfileFormState } from '@/types/profile'

const organizerProfile: OrganizerProfile = {
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

function createForm(): ProfileFormState {
  return {
    name: 'Track',
    surname: 'Events',
    email: 'trackevents@example.com',
    address: 'Calle Motor 1',
    phone: '600000000',
    legalName: 'TrackEvents S.L.',
    cif: 'B12345678',
    password: '',
    passwordConfirmation: '',
  }
}

describe('OrganizerAccountDetailsPanel', () => {
  it('renders organizer details and emits edit', async () => {
    const wrapper = mount(OrganizerAccountDetailsPanel, {
      props: {
        organizerProfile,
        profileForm: createForm(),
        editMode: false,
        saving: false,
        errorMessage: '',
      },
    })

    await wrapper.get('button[aria-label="Editar perfil"]').trigger('click')

    expect(wrapper.text()).toContain('Datos de organizador')
    expect(wrapper.text()).toContain('TrackEvents S.L.')
    expect(wrapper.text()).toContain('Pendiente de validación')
    expect(wrapper.emitted('edit')).toHaveLength(1)
  })

  it('updates editable form fields and emits cancel/save', async () => {
    const profileForm = createForm()
    const wrapper = mount(OrganizerAccountDetailsPanel, {
      props: {
        organizerProfile: {
          ...organizerProfile,
          created: null,
          userEnabled: false,
          organizerEnabled: true,
        },
        profileForm,
        editMode: true,
        saving: false,
        errorMessage: 'No se pudo guardar',
      },
    })

    await wrapper.get('input[autocomplete="given-name"]').setValue('Nuevo')
    await wrapper.get('input[autocomplete="family-name"]').setValue('Organizador')
    await wrapper.get('input[autocomplete="email"]').setValue('nuevo@example.com')
    await wrapper.get('input[autocomplete="tel"]').setValue('611111111')
    await wrapper.get('input[autocomplete="address-line1"]').setValue('Calle Nueva 2')
    const textInputs = wrapper.findAll('input[type="text"]')
    const legalNameInput = textInputs.at(-2)
    const cifInput = textInputs.at(-1)
    expect(legalNameInput).toBeDefined()
    expect(cifInput).toBeDefined()
    await legalNameInput!.setValue('Nueva Razón S.L.')
    await cifInput!.setValue('B87654321')
    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('nueva-clave')
    await passwordInputs[1].setValue('nueva-clave')
    await wrapper.findAll('.section-header__actions button')[0].trigger('click')
    await wrapper.findAll('.section-header__actions button')[1].trigger('click')

    expect(wrapper.text()).toContain('No se pudo guardar')
    expect(wrapper.text()).toContain('Sin fecha')
    expect(profileForm.name).toBe('Nuevo')
    expect(profileForm.surname).toBe('Organizador')
    expect(profileForm.email).toBe('nuevo@example.com')
    expect(profileForm.phone).toBe('611111111')
    expect(profileForm.address).toBe('Calle Nueva 2')
    expect(profileForm.legalName).toBe('Nueva Razón S.L.')
    expect(profileForm.cif).toBe('B87654321')
    expect(profileForm.passwordConfirmation).toBe('nueva-clave')
    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('save')).toHaveLength(1)
  })
})
