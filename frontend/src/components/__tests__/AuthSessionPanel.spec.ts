import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AuthSessionPanel from '@/components/auth/AuthSessionPanel.vue'
import type { AuthSession } from '@/types/auth'

const session: AuthSession = {
  userId: 1,
  displayName: 'juanje',
  email: 'juanje@example.com',
  roleName: 'USER',
  authorizationHeader: 'Basic token',
}

describe('AuthSessionPanel', () => {
  it('renders the active session and emits close/logout actions', async () => {
    const wrapper = mount(AuthSessionPanel, {
      props: {
        session,
        dialogTitle: 'Sesión iniciada',
        dialogSubtitle: 'Ya tienes acceso al garage.',
        profileMonogram: 'JU',
      },
    })

    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')
    await buttons[1].trigger('click')

    expect(wrapper.text()).toContain('JU')
    expect(wrapper.text()).toContain('Sesión iniciada')
    expect(wrapper.text()).toContain('juanje')
    expect(wrapper.text()).toContain('juanje@example.com')
    expect(wrapper.emitted('close')).toHaveLength(1)
    expect(wrapper.emitted('logout')).toHaveLength(1)
  })
})
