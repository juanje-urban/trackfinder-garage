import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MessageThreadSidebar from '@/components/messages/MessageThreadSidebar.vue'
import type { MessageThread } from '@/types/message'

const thread: MessageThread = {
  key: '2::Jarama',
  counterpartId: 2,
  counterpartDisplayName: 'trackevents',
  roleName: 'ORGANIZER',
  subject: 'Jarama',
  lastMessageAt: '2026-07-12T08:00:00',
  unreadCount: 2,
  messages: [],
}

describe('MessageThreadSidebar', () => {
  it('renders an empty state and emits compose', async () => {
    const wrapper = mount(MessageThreadSidebar, {
      props: {
        threads: [],
        selectedThreadKey: null,
        isComposeMode: false,
      },
    })

    expect(wrapper.text()).toContain('Todavía no tienes hilos iniciados.')
    await wrapper.get('.messages-sidebar__compose').trigger('click')
    expect(wrapper.emitted('compose')).toHaveLength(1)
  })

  it('renders active and unread thread state', async () => {
    const wrapper = mount(MessageThreadSidebar, {
      props: {
        threads: [thread],
        selectedThreadKey: thread.key,
        isComposeMode: false,
      },
    })

    const threadButton = wrapper.get('.messages-thread-card')
    expect(threadButton.classes()).toContain('messages-thread-card--active')
    expect(threadButton.classes()).toContain('messages-thread-card--unread')
    expect(wrapper.text()).toContain('Jarama')
    expect(wrapper.text()).toContain('trackevents')
    expect(wrapper.text()).toContain('2')

    await threadButton.trigger('click')
    expect(wrapper.emitted('openThread')).toEqual([[thread]])
  })
})
