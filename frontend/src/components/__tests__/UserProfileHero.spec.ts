import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import UserProfileHero from '@/components/UserProfileHero.vue'

const props = {
  displayName: 'juanje',
  completedEvents: 6,
  visitedCircuits: 3,
  topFiveLapTimes: 4,
  poleCount: 2,
}

describe('UserProfileHero', () => {
  it('renders profile identity and stats', () => {
    const wrapper = mount(UserProfileHero, {
      props,
    })

    expect(wrapper.text()).toContain('juanje')
    expect(wrapper.text()).toContain('Eventos completados')
    expect(wrapper.text()).toContain('6')
    expect(wrapper.text()).toContain('Circuitos visitados')
    expect(wrapper.find('.profile-hero__contact-button').exists()).toBe(false)
  })

  it('renders optional email action and emits it', async () => {
    const wrapper = mount(UserProfileHero, {
      props: {
        ...props,
        showEmailAction: true,
      },
    })

    await wrapper.get('.profile-hero__contact-button').trigger('click')

    expect(wrapper.emitted('email-action')).toHaveLength(1)
  })
})
