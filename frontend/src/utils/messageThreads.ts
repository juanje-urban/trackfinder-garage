import type { MessageContact, MessageItem, MessageThread } from '@/types/message'

// Uso una clave estable por persona + asunto para agrupar mensajes en un mismo hilo.
export function buildMessageThreadKey(counterpartId: number, subject: string): string {
  return `${counterpartId}::${subject.trim()}`
}

// Convierto una lista plana de mensajes en hilos ordenados, que es mucho más cómodo para la UI.
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
    // Para cada mensaje calculo quién es "la otra persona" del hilo.
    const isOutgoing = message.senderId === currentUserId
    const counterpartId = isOutgoing ? message.receiverId : message.senderId
    const counterpartDisplayName = isOutgoing
      ? message.receiverDisplayName
      : message.senderDisplayName
    const key = buildMessageThreadKey(counterpartId, message.subject)
    const existingThread = threadsByKey.get(key)

    if (!existingThread) {
      // Si es el primer mensaje de ese hilo, creo la conversación desde cero.
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

  // Ordeno los mensajes dentro de cada hilo y luego pongo arriba el hilo más reciente.
  return [...threadsByKey.values()]
    .map((thread) => ({
      ...thread,
      messages: [...thread.messages].sort((left, right) => left.sentAt.localeCompare(right.sentAt)),
    }))
    .sort((left, right) => right.lastMessageAt.localeCompare(left.lastMessageAt))
}
