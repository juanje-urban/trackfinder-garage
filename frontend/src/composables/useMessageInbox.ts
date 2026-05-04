import { computed, reactive } from 'vue'
import { getOwnMessages } from '@/services/messageService'
import type { MessageItem } from '@/types/message'

type MessageInboxState = {
  unreadCount: number
}

const state = reactive<MessageInboxState>({
  unreadCount: 0,
})

// Guardo el contador de mensajes sin leer en un composable para compartirlo entre cabecera y bandeja.
export function useMessageInbox() {
  return {
    unreadCount: computed(() => state.unreadCount),
    refreshUnreadCount,
    syncMessages,
    clearUnreadCount,
  }
}

async function refreshUnreadCount(currentUserId: number | null) {
  if (!currentUserId) {
    clearUnreadCount()
    return
  }

  try {
    const messages = await getOwnMessages()
    syncMessages(messages, currentUserId)
  } catch {
    // Mantengo el último contador conocido si falla la recarga.
  }
}

function syncMessages(messages: MessageItem[], currentUserId: number | null) {
  if (!currentUserId) {
    clearUnreadCount()
    return
  }

  // Cuento solo mensajes recibidos por mí que todavía no estén leídos.
  state.unreadCount = messages.filter(
    (message) => message.receiverId === currentUserId && !message.isRead,
  ).length
}

function clearUnreadCount() {
  state.unreadCount = 0
}
