import { computed, reactive } from 'vue'
import { getOwnMessages } from '@/services/messageService'
import type { MessageItem } from '@/types/message'

type MessageInboxState = {
  unreadCount: number
}

const state = reactive<MessageInboxState>({
  unreadCount: 0,
})

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
    // Keep the last known counter if refresh fails.
  }
}

function syncMessages(messages: MessageItem[], currentUserId: number | null) {
  if (!currentUserId) {
    clearUnreadCount()
    return
  }

  state.unreadCount = messages.filter(
    (message) => message.receiverId === currentUserId && !message.isRead,
  ).length
}

function clearUnreadCount() {
  state.unreadCount = 0
}
