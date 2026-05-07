import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { MessageItem } from '@/types/message'
import { getOwnMessages } from '@/services/messageService'
import { useMessageInbox } from '@/composables/useMessageInbox'

vi.mock('@/services/messageService', () => ({
  getOwnMessages: vi.fn(),
}))

function message(overrides: Partial<MessageItem>): MessageItem {
  return {
    id: 1,
    senderId: 2,
    senderDisplayName: 'trackevents',
    receiverId: 1,
    receiverDisplayName: 'juanje',
    sentAt: '2026-07-12T08:00:00',
    isRead: false,
    subject: 'Jarama',
    message: 'Hola',
    ...overrides,
  }
}

const getOwnMessagesMock = vi.mocked(getOwnMessages)

describe('useMessageInbox', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    useMessageInbox().clearUnreadCount()
  })

  it('syncs unread messages received by the current user', () => {
    const inbox = useMessageInbox()

    inbox.syncMessages(
      [
        message({ id: 1, receiverId: 1, isRead: false }),
        message({ id: 2, receiverId: 1, isRead: true }),
        message({ id: 3, receiverId: 3, isRead: false }),
      ],
      1,
    )

    expect(inbox.unreadCount.value).toBe(1)
  })

  it('clears unread count without a current user', () => {
    const inbox = useMessageInbox()

    inbox.syncMessages([message({})], 1)
    inbox.syncMessages([message({})], null)

    expect(inbox.unreadCount.value).toBe(0)
  })

  it('refreshes unread count from the service', async () => {
    getOwnMessagesMock.mockResolvedValue([
      message({ id: 1, receiverId: 1, isRead: false }),
      message({ id: 2, receiverId: 1, isRead: false }),
    ])

    const inbox = useMessageInbox()
    await inbox.refreshUnreadCount(1)

    expect(getOwnMessagesMock).toHaveBeenCalledTimes(1)
    expect(inbox.unreadCount.value).toBe(2)
  })

  it('keeps the previous count when refresh fails', async () => {
    const inbox = useMessageInbox()
    inbox.syncMessages([message({})], 1)
    getOwnMessagesMock.mockRejectedValue(new Error('network'))

    await inbox.refreshUnreadCount(1)

    expect(inbox.unreadCount.value).toBe(1)
  })
})
