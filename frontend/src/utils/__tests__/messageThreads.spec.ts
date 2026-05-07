import { describe, expect, it } from 'vitest'
import type { MessageContact, MessageItem } from '@/types/message'
import { buildMessageThreadKey, buildMessageThreads } from '@/utils/messageThreads'

function message(overrides: Partial<MessageItem>): MessageItem {
  return {
    id: 1,
    senderId: 1,
    senderDisplayName: 'juanje',
    receiverId: 2,
    receiverDisplayName: 'trackevents',
    sentAt: '2026-07-12T08:00:00',
    isRead: true,
    subject: 'Jarama',
    message: 'Hola',
    ...overrides,
  }
}

const contactsById: Record<number, MessageContact> = {
  2: { id: 2, displayName: 'trackevents', roleName: 'ORGANIZER' },
  3: { id: 3, displayName: 'admin', roleName: 'ADMIN' },
}

describe('buildMessageThreadKey', () => {
  it('builds a stable key with trimmed subject', () => {
    expect(buildMessageThreadKey(2, ' Jarama ')).toBe('2::Jarama')
  })
})

describe('buildMessageThreads', () => {
  it('returns no threads without current user', () => {
    expect(buildMessageThreads([message({})], null, contactsById)).toEqual([])
  })

  it('groups messages by counterpart and subject', () => {
    const threads = buildMessageThreads(
      [
        message({ id: 1, sentAt: '2026-07-12T08:00:00' }),
        message({
          id: 2,
          senderId: 2,
          senderDisplayName: 'trackevents',
          receiverId: 1,
          receiverDisplayName: 'juanje',
          sentAt: '2026-07-12T09:00:00',
          isRead: false,
        }),
      ],
      1,
      contactsById,
    )

    expect(threads).toHaveLength(1)
    expect(threads[0]).toMatchObject({
      key: '2::Jarama',
      counterpartId: 2,
      counterpartDisplayName: 'trackevents',
      roleName: 'ORGANIZER',
      unreadCount: 1,
      lastMessageAt: '2026-07-12T09:00:00',
    })
    expect(threads[0]?.messages.map(({ id }) => id)).toEqual([1, 2])
  })

  it('orders threads by most recent message first', () => {
    const threads = buildMessageThreads(
      [
        message({ id: 1, subject: 'Jarama', sentAt: '2026-07-12T08:00:00' }),
        message({
          id: 2,
          subject: 'Calafat',
          senderId: 3,
          senderDisplayName: 'admin',
          receiverId: 1,
          receiverDisplayName: 'juanje',
          sentAt: '2026-07-13T08:00:00',
        }),
      ],
      1,
      contactsById,
    )

    expect(threads.map(({ subject }) => subject)).toEqual(['Calafat', 'Jarama'])
  })

  it('ignores outgoing unread messages and updates the displayed counterpart name', () => {
    const threads = buildMessageThreads(
      [
        message({
          id: 1,
          senderId: 1,
          receiverId: 2,
          receiverDisplayName: 'trackevents',
          sentAt: '2026-07-12T08:00:00',
          isRead: false,
        }),
        message({
          id: 2,
          senderId: 2,
          senderDisplayName: 'TrackEvents Actualizado',
          receiverId: 1,
          sentAt: '2026-07-12T09:00:00',
          isRead: true,
        }),
      ],
      1,
      {},
    )

    expect(threads[0]).toMatchObject({
      counterpartDisplayName: 'TrackEvents Actualizado',
      roleName: null,
      unreadCount: 0,
      lastMessageAt: '2026-07-12T09:00:00',
    })
  })
})
