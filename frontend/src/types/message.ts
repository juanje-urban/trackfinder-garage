export interface MessageItem {
  id: number
  senderId: number
  senderDisplayName: string
  receiverId: number
  receiverDisplayName: string
  sentAt: string
  isRead: boolean
  subject: string
  message: string
}

export interface MessageContact {
  id: number
  displayName: string
  roleName: string | null
}

export interface MessageThread {
  key: string
  counterpartId: number
  counterpartDisplayName: string
  roleName: string | null
  subject: string
  lastMessageAt: string
  unreadCount: number
  messages: MessageItem[]
}

export interface CreateMessagePayload {
  receiverId: number
  subject: string
  message: string
}
