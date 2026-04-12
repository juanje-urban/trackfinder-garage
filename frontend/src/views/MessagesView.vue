<script setup lang="ts">
import { MessageSquare, Send, SquarePen } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import PageHero from '@/components/PageHero.vue'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'
import {
  createOwnMessage,
  getMessageContacts,
  getOwnMessages,
  markOwnMessageAsRead,
} from '@/services/messageService'
import type { MessageContact, MessageItem } from '@/types/message'
import { resolveApiErrorMessage } from '@/utils/apiErrors'
import { formatDisplayDate } from '@/utils/format'

type ConversationSummary = {
  counterpartId: number
  counterpartDisplayName: string
  roleName: string | null
  lastMessageAt: string
  lastMessageSubject: string
  lastMessagePreview: string
  unreadCount: number
  lastMessageFromCurrentUser: boolean
}

const auth = useAuth()
const toast = useToast()

const loading = ref(true)
const error = ref('')
const sendError = ref('')
const isComposeMode = ref(false)
const sending = ref(false)
const selectedConversationId = ref<number | null>(null)

const messages = ref<MessageItem[]>([])
const contacts = ref<MessageContact[]>([])

const composer = reactive({
  receiverId: '',
  subject: '',
  message: '',
})

const currentUserId = computed(() => auth.session.value?.userId ?? null)
const isAuthenticated = computed(() => auth.isAuthenticated.value)

const contactsById = computed(() =>
  Object.fromEntries(contacts.value.map((contact) => [contact.id, contact])),
)

const conversations = computed<ConversationSummary[]>(() => {
  if (currentUserId.value === null) {
    return []
  }

  const summaries = new Map<number, ConversationSummary>()

  for (const message of messages.value) {
    const isOutgoing = message.senderId === currentUserId.value
    const counterpartId = isOutgoing ? message.receiverId : message.senderId
    const counterpartDisplayName = isOutgoing
      ? message.receiverDisplayName
      : message.senderDisplayName

    const existingSummary = summaries.get(counterpartId)
    if (!existingSummary) {
      summaries.set(counterpartId, {
        counterpartId,
        counterpartDisplayName,
        roleName: contactsById.value[counterpartId]?.roleName ?? null,
        lastMessageAt: message.sentAt,
        lastMessageSubject: message.subject,
        lastMessagePreview: message.message,
        unreadCount: !isOutgoing && !message.isRead ? 1 : 0,
        lastMessageFromCurrentUser: isOutgoing,
      })
      continue
    }

    if (!isOutgoing && !message.isRead) {
      existingSummary.unreadCount += 1
    }

    if (message.sentAt > existingSummary.lastMessageAt) {
      existingSummary.lastMessageAt = message.sentAt
      existingSummary.lastMessageSubject = message.subject
      existingSummary.lastMessagePreview = message.message
      existingSummary.lastMessageFromCurrentUser = isOutgoing
      existingSummary.counterpartDisplayName = counterpartDisplayName
      existingSummary.roleName = contactsById.value[counterpartId]?.roleName ?? existingSummary.roleName
    }
  }

  return [...summaries.values()].sort((left, right) =>
    right.lastMessageAt.localeCompare(left.lastMessageAt),
  )
})

const activeConversation = computed(
  () =>
    conversations.value.find(
      (conversation) => conversation.counterpartId === selectedConversationId.value,
    ) ?? null,
)

const activeMessages = computed(() => {
  if (selectedConversationId.value === null || currentUserId.value === null) {
    return []
  }

  return messages.value
    .filter((message) => {
      const isOutgoingToSelected =
        message.senderId === currentUserId.value &&
        message.receiverId === selectedConversationId.value

      const isIncomingFromSelected =
        message.senderId === selectedConversationId.value &&
        message.receiverId === currentUserId.value

      return isOutgoingToSelected || isIncomingFromSelected
    })
    .sort((left, right) => left.sentAt.localeCompare(right.sentAt))
})

onMounted(async () => {
  if (!isAuthenticated.value) {
    loading.value = false
    error.value = 'Necesitas iniciar sesion para acceder a tus mensajes.'
    return
  }

  await loadMessagesPage()
})

watch(
  () => selectedConversationId.value,
  async () => {
    await markSelectedConversationAsRead()
  },
)

async function loadMessagesPage() {
  loading.value = true
  error.value = ''

  try {
    const [ownMessages, availableContacts] = await Promise.all([
      getOwnMessages(),
      getMessageContacts(),
    ])

    messages.value = ownMessages
    contacts.value = availableContacts

    const firstConversation = conversations.value[0]

    if (selectedConversationId.value === null && firstConversation) {
      openConversation(firstConversation)
    }
  } catch (requestError) {
    error.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo cargar tu bandeja de mensajes.',
      statusMessages: {
        401: 'Tu sesion ha caducado. Inicia sesion de nuevo.',
      },
    })
  } finally {
    loading.value = false
  }
}

function openCompose() {
  isComposeMode.value = true
  composer.receiverId = ''
  composer.subject = ''
  composer.message = ''
  sendError.value = ''
}

function openConversation(conversation: ConversationSummary) {
  isComposeMode.value = false
  selectedConversationId.value = conversation.counterpartId
  composer.receiverId = String(conversation.counterpartId)
  composer.subject = conversation.lastMessageSubject
  composer.message = ''
  sendError.value = ''
}

function cancelCompose() {
  if (activeConversation.value) {
    openConversation(activeConversation.value)
    return
  }

  isComposeMode.value = false
  composer.receiverId = ''
  composer.subject = ''
  composer.message = ''
  sendError.value = ''
}

async function markSelectedConversationAsRead() {
  if (isComposeMode.value || selectedConversationId.value === null || currentUserId.value === null) {
    return
  }

  const unreadIncomingMessages = activeMessages.value.filter(
    (message) => message.receiverId === currentUserId.value && !message.isRead,
  )

  if (unreadIncomingMessages.length === 0) {
    return
  }

  try {
    const updatedMessages = await Promise.all(
      unreadIncomingMessages.map((message) => markOwnMessageAsRead(message.id)),
    )

    const updatedMessagesById = new Map(
      updatedMessages.map((message) => [message.id, message] as const),
    )

    messages.value = messages.value.map(
      (message) => updatedMessagesById.get(message.id) ?? message,
    )
  } catch {
    // The conversation stays usable even if the read sync fails.
  }
}

async function sendMessage() {
  sendError.value = ''

  const receiverId = Number(composer.receiverId)
  if (!receiverId) {
    sendError.value = 'Selecciona un destinatario.'
    return
  }

  if (composer.subject.trim() === '') {
    sendError.value = 'El asunto es obligatorio.'
    return
  }

  if (composer.message.trim() === '') {
    sendError.value = 'Escribe un mensaje antes de enviarlo.'
    return
  }

  sending.value = true

  try {
    const createdMessage = await createOwnMessage({
      receiverId,
      subject: composer.subject.trim(),
      message: composer.message.trim(),
    })

    messages.value = [...messages.value, createdMessage]
    contacts.value = contacts.value.some((contact) => contact.id === receiverId)
      ? contacts.value
      : [
          ...contacts.value,
          {
            id: createdMessage.receiverId,
            displayName: createdMessage.receiverDisplayName,
            roleName: null,
          },
        ]

    selectedConversationId.value = receiverId
    isComposeMode.value = false
    composer.receiverId = String(receiverId)
    composer.subject = createdMessage.subject
    composer.message = ''
    toast.showToast('Mensaje enviado correctamente.')
  } catch (requestError) {
    sendError.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo enviar el mensaje.',
      statusMessages: {
        401: 'Tu sesion ha caducado. Inicia sesion de nuevo.',
      },
      matches: [
        { includes: 'themselves', message: 'No puedes enviarte mensajes a ti mismo.' },
        {
          includes: 'must be active to receive',
          message: 'Solo puedes escribir a cuentas activas.',
        },
        { includes: 'Subject is required', message: 'El asunto es obligatorio.' },
        { includes: 'Message text is required', message: 'El mensaje es obligatorio.' },
        {
          includes: 'Subject must not be longer than 255',
          message: 'El asunto no puede superar los 255 caracteres.',
        },
        {
          includes: 'Message text must not be longer than 500',
          message: 'El mensaje no puede superar los 500 caracteres.',
        },
      ],
    })
  } finally {
    sending.value = false
  }
}

function formatConversationTimestamp(value: string): string {
  const date = new Date(value)
  return date.toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatRoleLabel(roleName: string | null): string {
  switch ((roleName ?? '').trim().toUpperCase()) {
    case 'ADMIN':
      return 'Administrador'
    case 'ORGANIZER':
      return 'Organizador'
    case 'USER':
      return 'Usuario'
    default:
      return ''
  }
}

function isOutgoingMessage(message: MessageItem): boolean {
  return message.senderId === currentUserId.value
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Mensajes</p>
      <h1 class="ui-title-section">Preparando tu bandeja</h1>
      <p class="ui-copy-muted">Estamos cargando tus conversaciones y destinatarios.</p>
    </section>

    <section v-else-if="error" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Mensajes</p>
      <h1 class="ui-title-section">No se pudo cargar la mensajeria</h1>
      <p class="ui-copy-muted">{{ error }}</p>
      <button class="action-button" type="button" @click="loadMessagesPage">Reintentar</button>
    </section>

    <template v-else>
      <PageHero
        eyebrow="Mensajes"
        title="Bandeja compartida"
        description="Habla con otros usuarios, organizadores y administradores desde un unico espacio."
      />

      <section class="messages-layout panel panel-pad-lg">
        <aside class="messages-sidebar panel-stack-md">
          <div class="messages-sidebar__header">
            <div class="panel-copy">
              <h2 class="ui-title-card">Conversaciones</h2>
              <p class="ui-copy-muted">
                {{ conversations.length }} hilo<span v-if="conversations.length !== 1">s</span>
                activo<span v-if="conversations.length !== 1">s</span>
              </p>
            </div>

            <button class="icon-button icon-button--success" type="button" title="Nuevo mensaje" @click="openCompose">
              <SquarePen :size="16" aria-hidden="true" />
            </button>
          </div>

          <p v-if="conversations.length === 0" class="ui-copy-muted">
            Todavia no tienes conversaciones iniciadas.
          </p>

          <div v-else class="messages-conversation-list">
            <button
              v-for="conversation in conversations"
              :key="conversation.counterpartId"
              class="messages-conversation"
              :class="{
                'messages-conversation--active':
                  !isComposeMode && selectedConversationId === conversation.counterpartId,
              }"
              type="button"
              @click="openConversation(conversation)"
            >
              <div class="messages-conversation__copy">
                <div class="messages-conversation__title-row">
                  <strong class="messages-conversation__title">
                    {{ conversation.counterpartDisplayName }}
                  </strong>
                  <span class="messages-conversation__time">
                    {{ formatConversationTimestamp(conversation.lastMessageAt) }}
                  </span>
                </div>

                <p v-if="formatRoleLabel(conversation.roleName)" class="ui-copy-muted">
                  {{ formatRoleLabel(conversation.roleName) }}
                </p>

                <p class="messages-conversation__subject">{{ conversation.lastMessageSubject }}</p>
                <p class="messages-conversation__preview">
                  <span v-if="conversation.lastMessageFromCurrentUser">Tu: </span>
                  {{ conversation.lastMessagePreview }}
                </p>
              </div>

              <span v-if="conversation.unreadCount > 0" class="messages-conversation__badge">
                {{ conversation.unreadCount }}
              </span>
            </button>
          </div>
        </aside>

        <section class="messages-main panel-stack-lg">
          <div class="messages-main__header">
            <div v-if="isComposeMode" class="panel-copy">
              <p class="ui-eyebrow">Nuevo mensaje</p>
              <h2 class="ui-title-card">Iniciar conversacion</h2>
            </div>

            <div v-else-if="activeConversation" class="panel-copy">
              <p class="ui-eyebrow">{{ formatRoleLabel(activeConversation.roleName) || 'Conversacion' }}</p>
              <h2 class="ui-title-card">{{ activeConversation.counterpartDisplayName }}</h2>
              <p class="ui-copy-muted">
                Ultimo movimiento el {{ formatDisplayDate(activeConversation.lastMessageAt.slice(0, 10)) }}
              </p>
            </div>

            <div v-else class="panel-copy">
              <p class="ui-eyebrow">Mensajes</p>
              <h2 class="ui-title-card">Selecciona una conversacion</h2>
              <p class="ui-copy-muted">
                Elige un hilo existente o inicia uno nuevo desde la columna izquierda.
              </p>
            </div>
          </div>

          <p v-if="sendError" class="status-message status-message--error">{{ sendError }}</p>

          <div v-if="!isComposeMode && activeConversation" class="messages-thread panel panel-pad-lg panel-stack-md">
            <p v-if="activeMessages.length === 0" class="ui-copy-muted">
              Todavia no hay mensajes en esta conversacion.
            </p>

            <article
              v-for="message in activeMessages"
              :key="message.id"
              class="messages-bubble"
              :class="{ 'messages-bubble--outgoing': isOutgoingMessage(message) }"
            >
              <div class="messages-bubble__meta">
                <strong>{{ isOutgoingMessage(message) ? 'Tú' : message.senderDisplayName }}</strong>
                <span>{{ formatConversationTimestamp(message.sentAt) }}</span>
              </div>
              <strong class="messages-bubble__subject">{{ message.subject }}</strong>
              <p class="messages-bubble__body">{{ message.message }}</p>
            </article>
          </div>

          <div v-else-if="!isComposeMode && !activeConversation" class="messages-empty panel panel-pad-lg panel-stack-sm">
            <MessageSquare :size="20" aria-hidden="true" />
            <p class="ui-copy-muted">
              No tienes una conversacion seleccionada. Puedes empezar una nueva cuando quieras.
            </p>
            <button class="action-button" type="button" @click="openCompose">Nuevo mensaje</button>
          </div>

          <form class="messages-composer panel panel-pad-lg panel-stack-md" @submit.prevent="sendMessage">
            <label v-if="isComposeMode" class="messages-field">
              <span>Destinatario</span>
              <select v-model="composer.receiverId">
                <option value="">Selecciona un usuario</option>
                <option v-for="contact in contacts" :key="contact.id" :value="String(contact.id)">
                  {{ contact.displayName }}
                  {{
                    formatRoleLabel(contact.roleName)
                      ? ` · ${formatRoleLabel(contact.roleName)}`
                      : ''
                  }}
                </option>
              </select>
            </label>

            <label class="messages-field">
              <span>Asunto</span>
              <input v-model="composer.subject" type="text" maxlength="255" placeholder="Asunto del mensaje" />
            </label>

            <label class="messages-field">
              <span>Mensaje</span>
              <textarea
                v-model="composer.message"
                rows="6"
                maxlength="500"
                placeholder="Escribe tu mensaje"
              ></textarea>
            </label>

            <div class="messages-composer__actions">
              <button
                v-if="isComposeMode"
                class="action-button action-button--ghost"
                type="button"
                @click="cancelCompose"
              >
                Cancelar
              </button>
              <button class="action-button" type="submit" :disabled="sending">
                <Send :size="16" aria-hidden="true" />
                {{ sending ? 'Enviando...' : 'Enviar mensaje' }}
              </button>
            </div>
          </form>
        </section>
      </section>
    </template>
  </main>
</template>

<style scoped>
.messages-layout {
  display: grid;
  gap: var(--space-lg);
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
}

.messages-sidebar__header,
.messages-main__header,
.messages-composer__actions {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-md);
}

.messages-conversation-list {
  display: grid;
  gap: var(--space-sm);
}

.messages-conversation {
  width: 100%;
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
  text-align: left;
}

.messages-conversation--active {
  border-color: rgba(255, 76, 58, 0.4);
  background: rgba(255, 76, 58, 0.08);
}

.messages-conversation__copy {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.messages-conversation__title-row,
.messages-bubble__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-sm);
}

.messages-conversation__title,
.messages-bubble__subject {
  color: var(--text-strong);
}

.messages-conversation__time,
.messages-bubble__meta span {
  color: var(--text-muted);
  font-size: var(--fs-caption);
  white-space: nowrap;
}

.messages-conversation__subject {
  color: var(--text-strong);
  font-size: var(--fs-body-sm);
  font-weight: 700;
}

.messages-conversation__preview {
  color: var(--text-muted);
  font-size: var(--fs-body-sm);
  line-height: 1.45;
}

.messages-conversation__badge {
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: var(--radius-pill);
  background: var(--accent);
  color: var(--text-on-light);
  display: inline-grid;
  place-items: center;
  font-size: var(--fs-caption);
  font-weight: 800;
}

.messages-thread,
.messages-empty {
  min-height: 280px;
}

.messages-bubble {
  max-width: min(88%, 720px);
  display: grid;
  gap: var(--space-xs);
  padding: var(--space-md);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.messages-bubble--outgoing {
  margin-left: auto;
  border-color: rgba(255, 76, 58, 0.32);
  background: rgba(255, 76, 58, 0.08);
}

.messages-bubble__body {
  margin: 0;
  color: var(--text-body);
  line-height: 1.6;
  white-space: pre-wrap;
}

.messages-field {
  display: grid;
  gap: var(--space-xs);
}

.messages-field span {
  color: var(--text-muted);
  font-size: var(--fs-caption);
  font-weight: 700;
}

.messages-field input,
.messages-field select,
.messages-field textarea {
  width: 100%;
  min-height: 50px;
  padding: 12px 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  color: var(--text-body);
  background: var(--surface-glass);
}

.messages-field select {
  appearance: none;
  background:
    linear-gradient(180deg, rgba(39, 16, 16, 0.98) 0%, rgba(24, 10, 10, 0.98) 100%);
}

.messages-field select option {
  color: var(--text-strong);
  background: #1b0c0c;
}

.messages-field textarea {
  resize: vertical;
  min-height: 160px;
}

.messages-field input:focus,
.messages-field select:focus,
.messages-field textarea:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

@media (max-width: 980px) {
  .messages-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .messages-sidebar__header,
  .messages-main__header,
  .messages-composer__actions,
  .messages-conversation__title-row,
  .messages-bubble__meta {
    flex-direction: column;
    align-items: start;
  }

  .messages-bubble {
    max-width: 100%;
  }

  .messages-composer__actions .action-button {
    width: 100%;
  }
}
</style>
