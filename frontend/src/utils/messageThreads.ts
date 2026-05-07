import type { MessageContact, MessageItem, MessageThread } from '@/types/message'

// Uso una clave estable por persona + asunto para agrupar mensajes en un mismo hilo.
export function buildMessageThreadKey(counterpartId: number, subject: string): string {
  return `${counterpartId}::${subject.trim()}`
}

type MessageThreadContext = {
  isOutgoing: boolean
  counterpartId: number
  counterpartDisplayName: string
  key: string
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
    const context = getMessageThreadContext(message, currentUserId)
    const existingThread = threadsByKey.get(context.key)

    if (!existingThread) {
      threadsByKey.set(context.key, createMessageThread(message, context, contactsById))
      continue
    }

    appendMessageToThread(existingThread, message, context, contactsById)
  }

  // Ordeno los mensajes dentro de cada hilo y luego pongo arriba el hilo más reciente.
  return [...threadsByKey.values()]
    .map(sortThreadMessages)
    .sort((left, right) => right.lastMessageAt.localeCompare(left.lastMessageAt))
}

function getMessageThreadContext(
  message: MessageItem,
  currentUserId: number,
): MessageThreadContext {
  // Para cada mensaje calculo quién es "la otra persona" del hilo.
  const isOutgoing = message.senderId === currentUserId
  const counterpartId = isOutgoing ? message.receiverId : message.senderId

  return {
    isOutgoing,
    counterpartId,
    counterpartDisplayName: isOutgoing ? message.receiverDisplayName : message.senderDisplayName,
    key: buildMessageThreadKey(counterpartId, message.subject),
  }
}

function createMessageThread(
  message: MessageItem,
  context: MessageThreadContext,
  contactsById: Record<number, MessageContact>,
): MessageThread {
  // Si es el primer mensaje de ese hilo, creo la conversación desde cero.
  return {
    key: context.key,
    counterpartId: context.counterpartId,
    counterpartDisplayName: context.counterpartDisplayName,
    roleName: contactsById[context.counterpartId]?.roleName ?? null,
    subject: message.subject,
    lastMessageAt: message.sentAt,
    unreadCount: !context.isOutgoing && !message.isRead ? 1 : 0,
    messages: [message],
  }
}

function appendMessageToThread(
  thread: MessageThread,
  message: MessageItem,
  context: MessageThreadContext,
  contactsById: Record<number, MessageContact>,
) {
  thread.messages.push(message)

  if (!context.isOutgoing && !message.isRead) {
    thread.unreadCount += 1
  }

  if (message.sentAt > thread.lastMessageAt) {
    thread.lastMessageAt = message.sentAt
    thread.counterpartDisplayName = context.counterpartDisplayName
    thread.roleName = contactsById[context.counterpartId]?.roleName ?? thread.roleName
  }
}

function sortThreadMessages(thread: MessageThread): MessageThread {
  return {
    ...thread,
    messages: [...thread.messages].sort((left, right) => left.sentAt.localeCompare(right.sentAt)),
  }
}
