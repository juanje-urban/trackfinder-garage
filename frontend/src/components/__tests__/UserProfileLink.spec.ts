import { mount, RouterLinkStub } from '@vue/test-utils'
import { afterEach, describe, expect, it } from 'vitest'
import UserProfileLink from '@/components/UserProfileLink.vue'
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()

function mountLink(displayName: string) {
  return mount(UserProfileLink, {
    props: { displayName },
    global: {
      stubs: {
        RouterLink: RouterLinkStub,
      },
    },
  })
}

describe('UserProfileLink', () => {
  afterEach(() => {
    auth.clearSession()
  })

  it('links to the private profile when the displayed user is the current standard user', () => {
    auth.setSession({
      userId: 1,
      displayName: 'juanje',
      email: 'juanje@example.com',
      roleName: 'USER',
      authorizationHeader: 'Basic token',
    })

    const wrapper = mountLink('juanje')

    expect(wrapper.getComponent(RouterLinkStub).props('to')).toBe('/profile')
  })

  it('links to the public profile for other display names', () => {
    auth.clearSession()
    const wrapper = mountLink('Track Events')

    expect(wrapper.getComponent(RouterLinkStub).props('to')).toBe('/profiles/Track%20Events')
    expect(wrapper.text()).toBe('Track Events')
  })
})
