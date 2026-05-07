import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MessageConversationPanel from '@/components/messages/MessageConversationPanel.vue'
import type { MessageThread } from '@/types/message'

const thread: MessageThread = {
  key: '2::Jarama',
  counterpartId: 2,
  counterpartDisplayName: 'trackevents',
  roleName: 'ORGANIZER',
  subject: 'Jarama',
  lastMessageAt: '2026-07-12T08:00:00',
  unreadCount: 0,
  messages: [
    {
      id: 1,
      senderId: 1,
      senderDisplayName: 'juanje',
      receiverId: 2,
      receiverDisplayName: 'trackevents',
      sentAt: '2026-07-12T08:00:00',
      isRead: true,
      subject: 'Jarama',
      message: 'Hola',
    },
    {
      id: 2,
      senderId: 2,
      senderDisplayName: 'trackevents',
      receiverId: 1,
      receiverDisplayName: 'juanje',
      sentAt: '2026-07-12T08:05:00',
      isRead: false,
      subject: 'Jarama',
      message: 'Buenas',
    },
  ],
}

function mountPanel(overrides = {}) {
  return mount(MessageConversationPanel, {
    props: {
      thread,
      currentUserId: 1,
      replyBody: '',
      sendError: '',
      sending: false,
      ...overrides,
    },
  })
}

describe('MessageConversationPanel', () => {
  it('marks outgoing messages and emits reply updates/submission', async () => {
    const wrapper = mountPanel()

    await wrapper.get('textarea').setValue('Gracias')
    await wrapper.get('form').trigger('submit')

    expect(wrapper.findAll('.messages-bubble')[0].classes()).toContain('messages-bubble--outgoing')
    expect(wrapper.findAll('.messages-bubble')[1].classes()).not.toContain('messages-bubble--outgoing')
    expect(wrapper.text()).toContain('Tu')
    expect(wrapper.text()).toContain('trackevents')
    expect(wrapper.emitted('update:replyBody')).toEqual([['Gracias']])
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })

  it('renders send error and disabled sending state', () => {
    const wrapper = mountPanel({
      sendError: 'No se pudo responder',
      sending: true,
    })

    expect(wrapper.text()).toContain('No se pudo responder')
    expect(wrapper.get('button').text()).toContain('Enviando...')
    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
  })
})
