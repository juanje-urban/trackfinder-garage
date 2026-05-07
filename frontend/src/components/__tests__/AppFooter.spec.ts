import { mount, RouterLinkStub } from '@vue/test-utils'
import { afterEach, describe, expect, it } from 'vitest'
import AppFooter from '@/components/AppFooter.vue'
import { useAuth } from '@/composables/useAuth'
import type { AuthSession } from '@/types/auth'

const auth = useAuth()

const session = (roleName: string): AuthSession => ({
  userId: 1,
  displayName: 'juanje',
  email: 'juanje@example.com',
  roleName,
  authorizationHeader: 'Basic token',
})

function mountFooter() {
  return mount(AppFooter, {
    global: {
      stubs: {
        RouterLink: RouterLinkStub,
      },
    },
  })
}

describe('AppFooter', () => {
  afterEach(() => {
    auth.clearSession()
  })

  it('renders public navigation for anonymous visitors', () => {
    auth.clearSession()
    const wrapper = mountFooter()

    expect(wrapper.text()).toContain('Circuitos')
    expect(wrapper.text()).toContain('Eventos')
    expect(wrapper.text()).not.toContain('Mensajes')
    expect(wrapper.text()).not.toContain('Administración')
  })

  it('adds organizer links for organizer sessions', () => {
    auth.setSession(session('ORGANIZER'))
    const wrapper = mountFooter()

    expect(wrapper.text()).toContain('Mensajes')
    expect(wrapper.text()).toContain('Organización')
    expect(wrapper.text()).toContain('Mi perfil')
    expect(wrapper.text()).not.toContain('Administración')
  })

  it('adds admin links without private profile access', () => {
    auth.setSession(session('ADMIN'))
    const wrapper = mountFooter()

    expect(wrapper.text()).toContain('Mensajes')
    expect(wrapper.text()).toContain('Administración')
    expect(wrapper.text()).not.toContain('Mi perfil')
  })
})
