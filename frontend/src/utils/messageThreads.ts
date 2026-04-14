import type { MessageContact, MessageItem, MessageThread } from '@/types/message'

export function buildMessageThreadKey(counterpartId: number, subject: string): string {
  return `${counterpartId}::${subject.trim()}`
}

export function buildMessageThreads(
  messages: MessageItem[],
  currentUserId: number | null,
  contactsById: Record<number, MessageContact>,
): MessageThread[] {
  if (currentUserId === null) {
    return []
  }

  const threadsByKey = new Map<string, MessageThread>()

  for (const message of messages) {
    const isOutgoing = message.senderId === currentUserId
    const counterpartId = isOutgoing ? message.receiverId : message.senderId
    const counterpartDisplayName = isOutgoing
      ? message.receiverDisplayName
      : message.senderDisplayName
    const key = buildMessageThreadKey(counterpartId, message.subject)
    const existingThread = threadsByKey.get(key)

    if (!existingThread) {
      threadsByKey.set(key, {
        key,
        counterpartId,
        counterpartDisplayName,
        roleName: contactsById[counterpartId]?.roleName ?? null,
        subject: message.subject,
        lastMessageAt: message.sentAt,
        unreadCount: !isOutgoing && !message.isRead ? 1 : 0,
        messages: [message],
      })
      continue
    }

    existingThread.messages.push(message)

    if (!isOutgoing && !message.isRead) {
      existingThread.unreadCount += 1
    }

    if (message.sentAt > existingThread.lastMessageAt) {
      existingThread.lastMessageAt = message.sentAt
      existingThread.counterpartDisplayName = counterpartDisplayName
      existingThread.roleName = contactsById[counterpartId]?.roleName ?? existingThread.roleName
    }
  }

  return [...threadsByKey.values()]
    .map((thread) => ({
      ...thread,
      messages: [...thread.messages].sort((left, right) => left.sentAt.localeCompare(right.sentAt)),
    }))
    .sort((left, right) => right.lastMessageAt.localeCompare(left.lastMessageAt))
}
