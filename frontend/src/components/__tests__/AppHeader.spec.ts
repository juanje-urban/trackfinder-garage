import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { useAuth } from '@/composables/useAuth'
import { useMessageInbox } from '@/composables/useMessageInbox'
import { getOwnMessages } from '@/services/messageService'
import type { AuthSession } from '@/types/auth'
import type { MessageItem } from '@/types/message'

vi.mock('@/services/messageService', () => ({
  getOwnMessages: vi.fn(),
}))

const getOwnMessagesMock = vi.mocked(getOwnMessages)
const auth = useAuth()
const inbox = useMessageInbox()

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
    { path: '/messages', component: { template: '<div />' } },
    { path: '/profile', component: { template: '<div />' } },
  ],
})

function session(roleName: string): AuthSession {
  return {
    userId: 1,
    displayName: 'Juanje Urban',
    email: 'juanje@example.com',
    roleName,
    authorizationHeader: 'Basic token',
  }
}

function unreadMessage(id: number): MessageItem {
  return {
    id,
    senderId: 2,
    senderDisplayName: 'trackevents',
    receiverId: 1,
    receiverDisplayName: 'Juanje Urban',
    sentAt: '2026-07-12T08:00:00',
    isRead: false,
    subject: 'Jarama',
    message: 'Hola',
  }
}

async function mountHeader() {
  await router.push('/')
  await router.isReady()

  return mount(AppHeader, {
    global: {
      plugins: [router],
      stubs: {
        RouterLink: RouterLinkStub,
      },
    },
  })
}

describe('AppHeader', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getOwnMessagesMock.mockResolvedValue([])
    auth.clearSession()
    auth.closeAuthDialog()
    inbox.clearUnreadCount()
  })

  afterEach(() => {
    auth.clearSession()
    auth.closeAuthDialog()
    inbox.clearUnreadCount()
  })

  it('renders public navigation and opens the auth dialog from profile action', async () => {
    const wrapper = await mountHeader()

    await wrapper.get('button[aria-label="Acceso de usuario"]').trigger('click')

    expect(wrapper.text()).toContain('Circuitos')
    expect(wrapper.text()).toContain('Eventos')
    expect(wrapper.text()).not.toContain('Mensajes')
    expect(auth.isDialogOpen.value).toBe(true)
  })

  it('renders organizer navigation, monogram and unread badge', async () => {
    auth.setSession(session('ORGANIZER'))
    getOwnMessagesMock.mockResolvedValue(Array.from({ length: 105 }, (_, index) => unreadMessage(index + 1)))

    const wrapper = await mountHeader()
    await flushPromises()

    expect(wrapper.text()).toContain('Organizador')
    expect(wrapper.text()).toContain('Organización')
    expect(wrapper.text()).toContain('Mi perfil')
    expect(wrapper.get('.header-action__monogram').text()).toBe('JU')
    expect(wrapper.get('.header-action__badge').text()).toBe('99+')

    window.dispatchEvent(new Event('focus'))
    await flushPromises()

    expect(getOwnMessagesMock).toHaveBeenCalled()
  })

  it('renders admin navigation and refreshes unread count when the tab becomes visible', async () => {
    auth.setSession(session('ADMIN'))
    getOwnMessagesMock.mockResolvedValue([unreadMessage(1), unreadMessage(2)])

    const wrapper = await mountHeader()
    await flushPromises()

    expect(wrapper.text()).toContain('Administrador')
    expect(wrapper.text()).toContain('Administración')
    expect(wrapper.text()).not.toContain('Mi perfil')
    expect(wrapper.get('.header-action__badge').text()).toBe('2')

    const previousRefreshCalls = getOwnMessagesMock.mock.calls.length
    Object.defineProperty(document, 'visibilityState', {
      value: 'visible',
      configurable: true,
    })
    document.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()

    expect(getOwnMessagesMock.mock.calls.length).toBeGreaterThan(previousRefreshCalls)
  })
})
