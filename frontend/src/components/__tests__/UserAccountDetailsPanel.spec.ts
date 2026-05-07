import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import UserAccountDetailsPanel from '@/components/profile/UserAccountDetailsPanel.vue'
import type { ProfileFormState } from '@/types/profile'
import type { UserProfile } from '@/types/user'

const profile: UserProfile = {
  id: 1,
  displayName: 'juanje',
  email: 'juanje@example.com',
  created: '2026-01-01T10:00:00',
  enabled: true,
  name: 'Juan',
  surname: 'Urbán',
  address: 'Calle Motor 1',
  phone: '600000000',
  roleId: 3,
  roleName: 'USER',
}

function createForm(): ProfileFormState {
  return {
    name: 'Juan',
    surname: 'Urbán',
    email: 'juanje@example.com',
    address: 'Calle Motor 1',
    phone: '600000000',
    legalName: '',
    cif: '',
    password: '',
    passwordConfirmation: '',
  }
}

describe('UserAccountDetailsPanel', () => {
  it('renders readonly profile details and emits edit', async () => {
    const wrapper = mount(UserAccountDetailsPanel, {
      props: {
        profile,
        profileForm: createForm(),
        editMode: false,
        saving: false,
        errorMessage: '',
      },
    })

    await wrapper.get('button[aria-label="Editar perfil"]').trigger('click')

    expect(wrapper.text()).toContain('Datos personales')
    expect(wrapper.text()).toContain('juanje')
    expect(wrapper.text()).toContain('Activa')
    expect(wrapper.text()).toContain('************')
    expect(wrapper.emitted('edit')).toHaveLength(1)
  })

  it('updates editable form fields and emits cancel/save', async () => {
    const profileForm = createForm()
    const wrapper = mount(UserAccountDetailsPanel, {
      props: {
        profile: {
          ...profile,
          enabled: false,
        },
        profileForm,
        editMode: true,
        saving: false,
        errorMessage: 'No se pudo guardar',
      },
    })

    await wrapper.get('input[autocomplete="given-name"]').setValue('Juanjo')
    await wrapper.get('input[autocomplete="family-name"]').setValue('Urbán Nuevo')
    await wrapper.get('input[autocomplete="email"]').setValue('nuevo@example.com')
    await wrapper.get('input[autocomplete="tel"]').setValue('611111111')
    await wrapper.get('input[autocomplete="address-line1"]').setValue('Calle Nueva 2')
    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('nueva-clave')
    await passwordInputs[1].setValue('nueva-clave')
    await wrapper.findAll('.section-header__actions button')[0].trigger('click')
    await wrapper.findAll('.section-header__actions button')[1].trigger('click')

    expect(wrapper.text()).toContain('No se pudo guardar')
    expect(profileForm.name).toBe('Juanjo')
    expect(profileForm.surname).toBe('Urbán Nuevo')
    expect(profileForm.email).toBe('nuevo@example.com')
    expect(profileForm.phone).toBe('611111111')
    expect(profileForm.address).toBe('Calle Nueva 2')
    expect(profileForm.password).toBe('nueva-clave')
    expect(profileForm.passwordConfirmation).toBe('nueva-clave')
    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('save')).toHaveLength(1)
  })
})
