import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { nextTick } from 'vue'
import AuthDialog from '@/components/AuthDialog.vue'
import { useAuth } from '@/composables/useAuth'
import { login, register, registerOrganizer } from '@/services/authService'
import type { AuthSession } from '@/types/auth'

vi.mock('@/services/authService', () => ({
  getCurrentSession: vi.fn(),
  login: vi.fn(),
  register: vi.fn(),
  registerOrganizer: vi.fn(),
}))

const auth = useAuth()
const loginMock = vi.mocked(login)
const registerMock = vi.mocked(register)
const registerOrganizerMock = vi.mocked(registerOrganizer)

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
  ],
})

const session: AuthSession = {
  userId: 1,
  displayName: 'Juanje Urban',
  email: 'juanje@example.com',
  roleName: 'USER',
  authorizationHeader: 'Basic token',
}

function mountDialog() {
  return mount(AuthDialog, {
    global: {
      plugins: [router],
      stubs: {
        Teleport: true,
      },
    },
  })
}

describe('AuthDialog', () => {
  beforeEach(async () => {
    vi.clearAllMocks()
    auth.clearSession()
    auth.closeAuthDialog()
    document.body.style.overflow = ''
    await router.push('/')
    await router.isReady()
  })

  afterEach(() => {
    auth.clearSession()
    auth.closeAuthDialog()
    document.body.style.overflow = ''
  })

  it('does not render while the auth dialog is closed', () => {
    const wrapper = mountDialog()

    expect(wrapper.find('dialog.auth-dialog').exists()).toBe(false)
  })

  it('validates login form before calling the backend', async () => {
    auth.openAuthDialog()
    const wrapper = mountDialog()

    await wrapper.get('form').trigger('submit')

    expect(wrapper.text()).toContain('Introduce tu correo electrónico.')
    expect(loginMock).not.toHaveBeenCalled()
    expect(document.body.style.overflow).toBe('hidden')
  })

  it('closes the dialog with Escape and resets body scroll', async () => {
    auth.openAuthDialog()
    const wrapper = mountDialog()

    await wrapper.get('.auth-overlay').trigger('keydown', { key: 'Escape' })
    await nextTick()

    expect(auth.isDialogOpen.value).toBe(false)
    expect(document.body.style.overflow).toBe('')
  })

  it('logs in, stores the session and closes the dialog', async () => {
    loginMock.mockResolvedValue(session)
    auth.openAuthDialog()
    const wrapper = mountDialog()

    await wrapper.get('input[type="email"]').setValue('JUANJE@EXAMPLE.COM')
    await wrapper.get('input[type="password"]').setValue('secreto')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(loginMock).toHaveBeenCalledWith({
      email: 'juanje@example.com',
      password: 'secreto',
    })
    expect(auth.session.value?.displayName).toBe('Juanje Urban')
    expect(auth.isDialogOpen.value).toBe(false)
  })

  it('renders backend login failures', async () => {
    loginMock.mockRejectedValue(new Error('network'))
    auth.openAuthDialog()
    const wrapper = mountDialog()

    await wrapper.get('input[type="email"]').setValue('juanje@example.com')
    await wrapper.get('input[type="password"]').setValue('secreto')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('No se pudo iniciar sesión.')
    expect(auth.isAuthenticated.value).toBe(false)
  })

  it('validates malformed email addresses before calling the backend', async () => {
    auth.openAuthDialog()
    const wrapper = mountDialog()

    const malformedEmails = [
      'juanje.example.com',
      'juanje@@example.com',
      'juanje@exa mple.com',
      'juanje@example',
      'juanje@example.',
    ]

    for (const malformedEmail of malformedEmails) {
      await wrapper.get('input[type="email"]').setValue(malformedEmail)
      await wrapper.get('input[type="password"]').setValue('secreto')
      await wrapper.get('form').trigger('submit')
      await nextTick()

      expect(wrapper.text()).toContain('Introduce un correo electrónico válido.')
    }

    expect(loginMock).not.toHaveBeenCalled()
  })

  it('registers standard users with trimmed fields', async () => {
    registerMock.mockResolvedValue(session)
    auth.openAuthDialog()
    const wrapper = mountDialog()

    await wrapper.findAll('.auth-tab')[1].trigger('click')
    await wrapper.get('input[autocomplete="nickname"]').setValue(' juanje ')
    await wrapper.get('input[autocomplete="given-name"]').setValue(' Juan ')
    await wrapper.get('input[autocomplete="family-name"]').setValue(' Urbán ')
    await wrapper.get('input[autocomplete="tel"]').setValue(' 600000000 ')
    await wrapper.get('input[autocomplete="address-line1"]').setValue(' Calle Motor 1 ')
    await wrapper.get('input[type="email"]').setValue('JUANJE@EXAMPLE.COM')
    await wrapper.get('input[type="password"]').setValue('secreto')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(registerMock).toHaveBeenCalledWith({
      email: 'juanje@example.com',
      password: 'secreto',
      displayName: 'juanje',
      name: 'Juan',
      surname: 'Urbán',
      address: 'Calle Motor 1',
      phone: '600000000',
    })
    expect(auth.isAuthenticated.value).toBe(true)
  })

  it('registers organizer accounts with trimmed fields', async () => {
    registerOrganizerMock.mockResolvedValue({
      ...session,
      roleName: 'ORGANIZER',
    })
    auth.openAuthDialog()
    const wrapper = mountDialog()

    await wrapper.get('.auth-dialog__switch-action').trigger('click')
    await wrapper.get('input[autocomplete="nickname"]').setValue(' trackevents ')
    await wrapper.get('input[autocomplete="given-name"]').setValue(' Track ')
    await wrapper.get('input[autocomplete="family-name"]').setValue(' Events ')
    await wrapper.get('input[autocomplete="tel"]').setValue(' 600000000 ')
    await wrapper.get('input[autocomplete="address-line1"]').setValue(' Calle Motor 1 ')
    await wrapper.get('input[autocomplete="organization"]').setValue(' TrackEvents S.L. ')
    await wrapper.get('input[autocomplete="off"]').setValue(' B12345678 ')
    await wrapper.get('input[type="email"]').setValue('ORGANIZER@EXAMPLE.COM')
    await wrapper.get('input[type="password"]').setValue('secreto')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(registerOrganizerMock).toHaveBeenCalledWith({
      email: 'organizer@example.com',
      password: 'secreto',
      displayName: 'trackevents',
      name: 'Track',
      surname: 'Events',
      address: 'Calle Motor 1',
      phone: '600000000',
      legalName: 'TrackEvents S.L.',
      cif: 'B12345678',
    })
    expect(auth.isAuthenticated.value).toBe(true)
  })

  it('logs out from the active session panel', async () => {
    auth.setSession(session)
    auth.openAuthDialog()
    const wrapper = mountDialog()

    await wrapper.get('.auth-dialog__content--account .action-button--ghost').trigger('click')
    await flushPromises()

    expect(auth.session.value).toBeNull()
    expect(auth.isDialogOpen.value).toBe(false)
    expect(router.currentRoute.value.path).toBe('/')
  })
})
