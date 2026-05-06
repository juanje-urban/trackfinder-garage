import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AdminUserCard from '@/components/admin/AdminUserCard.vue'

describe('AdminUserCard', () => {
  it('renders user details and emits toggle', async () => {
    const wrapper = mount(AdminUserCard, {
      props: {
        displayName: 'trackevents',
        realName: 'Track Events',
        summary: 'trackevents@example.com',
        detail: 'TrackEvents S.L.',
        enabled: true,
      },
    })

    expect(wrapper.text()).toContain('trackevents')
    expect(wrapper.text()).toContain('Track Events')
    expect(wrapper.text()).toContain('TrackEvents S.L.')
    expect(wrapper.get('button').text()).toContain('Deshabilitar')

    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('toggle')).toHaveLength(1)
  })

  it('renders protected label and disables the action', () => {
    const wrapper = mount(AdminUserCard, {
      props: {
        displayName: 'admin',
        summary: 'admin@example.com',
        enabled: true,
        disabled: true,
        protectedLabel: 'Protegido',
        noteLabel: 'Cuenta demo',
        noteTone: 'warning',
      },
    })

    expect(wrapper.get('button').text()).toContain('Protegido')
    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
    expect(wrapper.find('.admin-user-card__note--warning').text()).toBe('Cuenta demo')
  })
})
