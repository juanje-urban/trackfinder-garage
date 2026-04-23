import { api } from '@/services/api'
import type { CreateMessagePayload, MessageContact, MessageItem } from '@/types/message'

export async function getOwnMessages(): Promise<MessageItem[]> {
  const response = await api.get<MessageItem[]>('/messages')
  return response.data
}

export async function getMessageContacts(): Promise<MessageContact[]> {
  const response = await api.get<MessageContact[]>('/messages/contacts')
  return response.data
}

export async function createOwnMessage(payload: CreateMessagePayload): Promise<MessageItem> {
  const response = await api.post<MessageItem>('/messages', payload)
  return response.data
}

export async function markOwnMessageAsRead(messageId: number): Promise<MessageItem> {
  const response = await api.patch<MessageItem>(`/messages/${messageId}/read`)
  return response.data
}
