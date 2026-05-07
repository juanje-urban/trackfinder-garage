import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import App from '@/App.vue'
import { useAuth } from '@/composables/useAuth'

const refreshSession = vi.fn()

vi.mock('@/composables/useAuth', () => ({
  useAuth: vi.fn(() => ({
    refreshSession,
  })),
}))

describe('App', () => {
  it('renders the persistent shell and refreshes the stored session on mount', async () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          AppHeader: { template: '<header>Header</header>' },
          AuthDialog: { template: '<section>Auth</section>' },
          AppToast: { template: '<output>Toast</output>' },
          RouterView: { template: '<main>Vista actual</main>' },
          AppFooter: { template: '<footer>Footer</footer>' },
        },
      },
    })
    await flushPromises()

    expect(useAuth).toHaveBeenCalled()
    expect(refreshSession).toHaveBeenCalled()
    expect(wrapper.text()).toContain('Vista actual')
    expect(wrapper.text()).toContain('Footer')
  })
})
