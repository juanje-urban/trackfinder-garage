import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AuthAccessPanel from '@/components/auth/AuthAccessPanel.vue'
import type { AuthOrganizerRegisterPayload } from '@/types/auth'
import type { AuthMode } from '@/types/authDialog'

function createForm(): AuthOrganizerRegisterPayload {
  return {
    displayName: '',
    email: '',
    password: '',
    name: '',
    surname: '',
    address: '',
    phone: '',
    legalName: '',
    cif: '',
  }
}

function mountPanel(
  overrides: Partial<{
    mode: AuthMode
    isRegistrationMode: boolean
    isOrganizerRegisterMode: boolean
    submitLabel: string
    switchPrompt: string
    switchActionLabel: string
    isSubmitting: boolean
    error: string
    showPassword: boolean
    form: AuthOrganizerRegisterPayload
  }> = {},
) {
  const form = overrides.form ?? createForm()

  return {
    form,
    wrapper: mount(AuthAccessPanel, {
      props: {
        mode: overrides.mode ?? 'login',
        isRegistrationMode: overrides.isRegistrationMode ?? false,
        isOrganizerRegisterMode: overrides.isOrganizerRegisterMode ?? false,
        submitLabel: overrides.submitLabel ?? 'Iniciar sesión',
        switchPrompt: overrides.switchPrompt ?? 'Quieres trabajar con nosotros?',
        switchActionLabel: overrides.switchActionLabel ?? 'Hazte organizador',
        isSubmitting: overrides.isSubmitting ?? false,
        error: overrides.error ?? '',
        showPassword: overrides.showPassword ?? false,
        form,
      },
      slots: {
        title: '<h2>Bienvenido de nuevo</h2>',
        subtitle: '<p>Accede al garage</p>',
      },
    }),
  }
}

describe('AuthAccessPanel', () => {
  it('renders login mode, updates credentials and emits actions', async () => {
    const { form, wrapper } = mountPanel()

    await wrapper.get('input[type="email"]').setValue('juanje@example.com')
    await wrapper.get('input[type="password"]').setValue('secreto')
    await wrapper.get('button[aria-label="Mostrar contraseña"]').trigger('click')
    await wrapper.findAll('.auth-tab')[1].trigger('click')
    await wrapper.get('form').trigger('submit')

    expect(wrapper.text()).toContain('Bienvenido de nuevo')
    expect(form.email).toBe('juanje@example.com')
    expect(form.password).toBe('secreto')
    expect(wrapper.emitted('togglePasswordVisibility')).toHaveLength(1)
    expect(wrapper.emitted('setMode')).toContainEqual(['register'])
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })

  it('renders organizer registration fields and switches back to standard registration', async () => {
    const { form, wrapper } = mountPanel({
      mode: 'organizer-register',
      isRegistrationMode: true,
      isOrganizerRegisterMode: true,
      submitLabel: 'Crear cuenta de organizador',
      switchPrompt: 'Prefieres una cuenta estandar?',
      switchActionLabel: 'Volver al registro',
      error: 'No se pudo crear la cuenta',
      showPassword: true,
    })

    await wrapper.get('input[autocomplete="organization"]').setValue('TrackEvents S.L.')
    await wrapper.get('input[autocomplete="off"]').setValue('B12345678')
    await wrapper.get('.auth-dialog__switch-action').trigger('click')

    expect(wrapper.find('.auth-tabs').exists()).toBe(false)
    expect(wrapper.text()).toContain('Crear cuenta de organizador')
    expect(wrapper.text()).toContain('No se pudo crear la cuenta')
    expect(wrapper.get('input[type="text"][autocomplete="new-password"]').exists()).toBe(true)
    expect(form.legalName).toBe('TrackEvents S.L.')
    expect(form.cif).toBe('B12345678')
    expect(wrapper.emitted('setMode')).toEqual([['register']])
  })

  it('updates all standard registration fields and switches to organizer registration', async () => {
    const { form, wrapper } = mountPanel({
      mode: 'register',
      isRegistrationMode: true,
      submitLabel: 'Crear cuenta',
    })

    await wrapper.get('input[autocomplete="nickname"]').setValue('juanje')
    await wrapper.get('input[autocomplete="given-name"]').setValue('Juan')
    await wrapper.get('input[autocomplete="family-name"]').setValue('Urbán')
    await wrapper.get('input[autocomplete="tel"]').setValue('600000000')
    await wrapper.get('input[autocomplete="address-line1"]').setValue('Calle Motor 1')
    await wrapper.get('.auth-dialog__switch-action').trigger('click')

    expect(wrapper.get('form').classes()).toContain('auth-form--register')
    expect(form.displayName).toBe('juanje')
    expect(form.name).toBe('Juan')
    expect(form.surname).toBe('Urbán')
    expect(form.phone).toBe('600000000')
    expect(form.address).toBe('Calle Motor 1')
    expect(wrapper.emitted('setMode')).toContainEqual(['organizer-register'])
  })
})
