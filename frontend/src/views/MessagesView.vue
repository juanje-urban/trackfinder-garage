<script setup lang="ts">
import { MessageSquare, Send, SquarePen } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHero from '@/components/PageHero.vue'
import { useAuth } from '@/composables/useAuth'
import { useMessageInbox } from '@/composables/useMessageInbox'
import { useToast } from '@/composables/useToast'
import {
  createOwnMessage,
  getMessageContacts,
  getOwnMessages,
  markOwnMessageAsRead,
} from '@/services/messageService'
import type { MessageContact, MessageItem } from '@/types/message'
import { resolveApiErrorMessage } from '@/utils/apiErrors'

type MessageThread = {
  key: string
  counterpartId: number
  counterpartDisplayName: string
  roleName: string | null
  subject: string
  lastMessageAt: string
  unreadCount: number
  messages: MessageItem[]
}

const auth = useAuth()
const messageInbox = useMessageInbox()
const toast = useToast()
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const sendError = ref('')
const sending = ref(false)
const isComposeMode = ref(false)
const selectedThreadKey = ref<string | null>(null)
const replyBody = ref('')

const messages = ref<MessageItem[]>([])
const contacts = ref<MessageContact[]>([])

const composeForm = reactive({
  receiverId: '',
  subject: '',
  message: '',
})

const currentUserId = computed(() => auth.session.value?.userId ?? null)
const isAuthenticated = computed(() => auth.isAuthenticated.value)

const contactsById = computed<Record<number, MessageContact>>(() =>
  Object.fromEntries(contacts.value.map((contact) => [contact.id, contact])),
)

const threads = computed<MessageThread[]>(() => {
  if (currentUserId.value === null) {
    return []
  }

  const threadsByKey = new Map<string, MessageThread>()

  for (const message of messages.value) {
    const isOutgoing = message.senderId === currentUserId.value
    const counterpartId = isOutgoing ? message.receiverId : message.senderId
    const counterpartDisplayName = isOutgoing
      ? message.receiverDisplayName
      : message.senderDisplayName
    const key = buildThreadKey(counterpartId, message.subject)

    const existingThread = threadsByKey.get(key)
    if (!existingThread) {
      threadsByKey.set(key, {
        key,
        counterpartId,
        counterpartDisplayName,
        roleName: contactsById.value[counterpartId]?.roleName ?? null,
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
      existingThread.roleName = contactsById.value[counterpartId]?.roleName ?? existingThread.roleName
    }
  }

  return [...threadsByKey.values()]
    .map((thread) => ({
      ...thread,
      messages: [...thread.messages].sort((left, right) => left.sentAt.localeCompare(right.sentAt)),
    }))
    .sort((left, right) => right.lastMessageAt.localeCompare(left.lastMessageAt))
})

const activeThread = computed(
  () => threads.value.find((thread) => thread.key === selectedThreadKey.value) ?? null,
)

const activeMessages = computed(() => activeThread.value?.messages ?? [])

onMounted(async () => {
  if (!isAuthenticated.value) {
    loading.value = false
    error.value = 'Necesitas iniciar sesion para acceder a tus mensajes.'
    return
  }

  await loadMessagesPage()
})

watch(
  () => activeThread.value?.key ?? null,
  async () => {
    await markActiveThreadAsRead()
  },
)

watch(
  () => route.query.receiverId,
  () => {
    applyComposeIntentFromRoute()
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
    messageInbox.syncMessages(ownMessages, currentUserId.value)
    applyComposeIntentFromRoute()
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

function buildThreadKey(counterpartId: number, subject: string): string {
  return `${counterpartId}::${subject.trim()}`
}

function syncSelectedThread() {
  const threadStillExists = threads.value.some((thread) => thread.key === selectedThreadKey.value)

  if (threadStillExists) {
    return
  }

  selectedThreadKey.value = threads.value[0]?.key ?? null
}

function openCompose() {
  openComposeForReceiver('')
}

function openComposeForReceiver(receiverId: string) {
  isComposeMode.value = true
  sendError.value = ''
  composeForm.receiverId = receiverId
  composeForm.subject = ''
  composeForm.message = ''
}

function openThread(thread: MessageThread) {
  isComposeMode.value = false
  selectedThreadKey.value = thread.key
  replyBody.value = ''
  sendError.value = ''
  void clearComposeIntent()
}

function cancelCompose() {
  isComposeMode.value = false
  composeForm.receiverId = ''
  composeForm.subject = ''
  composeForm.message = ''
  sendError.value = ''
  void clearComposeIntent()
}

function applyComposeIntentFromRoute() {
  const receiverIdRaw = route.query.receiverId
  const receiverId = typeof receiverIdRaw === 'string' ? Number(receiverIdRaw) : NaN
  const hasValidReceiver = Number.isFinite(receiverId) && contacts.value.some((contact) => contact.id === receiverId)

  if (hasValidReceiver) {
    selectedThreadKey.value = null
    replyBody.value = ''
    openComposeForReceiver(String(receiverId))
    return
  }

  syncSelectedThread()
}

async function clearComposeIntent() {
  if (!('receiverId' in route.query)) {
    return
  }

  const nextQuery = { ...route.query }
  delete nextQuery.receiverId
  await router.replace({ name: 'messages', query: nextQuery })
}

async function markActiveThreadAsRead() {
  if (isComposeMode.value || currentUserId.value === null || activeThread.value === null) {
    return
  }

  const unreadIncomingMessages = activeThread.value.messages.filter(
    (message) => message.receiverId === currentUserId.value && !message.isRead,
  )

  if (unreadIncomingMessages.length === 0) {
    return
  }

  try {
    const updatedMessages = await Promise.all(
      unreadIncomingMessages.map((message) => markOwnMessageAsRead(message.id)),
    )

    const updatedMessagesById = new Map(updatedMessages.map((message) => [message.id, message] as const))
    messages.value = messages.value.map((message) => updatedMessagesById.get(message.id) ?? message)
    messageInbox.syncMessages(messages.value, currentUserId.value)
  } catch {
    // The conversation remains usable even if the read sync fails.
  }
}

async function sendNewMessage() {
  sendError.value = ''

  const receiverId = Number(composeForm.receiverId)
  if (!receiverId) {
    sendError.value = 'Selecciona un destinatario.'
    return
  }

  if (composeForm.subject.trim() === '') {
    sendError.value = 'El asunto es obligatorio.'
    return
  }

  if (composeForm.message.trim() === '') {
    sendError.value = 'Escribe un mensaje antes de enviarlo.'
    return
  }

  await submitMessage({
    receiverId,
    subject: composeForm.subject.trim(),
    message: composeForm.message.trim(),
  })
}

async function sendReply() {
  sendError.value = ''

  if (activeThread.value === null) {
    sendError.value = 'Selecciona un hilo antes de responder.'
    return
  }

  if (replyBody.value.trim() === '') {
    sendError.value = 'Escribe un mensaje antes de enviarlo.'
    return
  }

  await submitMessage({
    receiverId: activeThread.value.counterpartId,
    subject: activeThread.value.subject,
    message: replyBody.value.trim(),
  })
}

async function submitMessage(payload: { receiverId: number; subject: string; message: string }) {
  sending.value = true

  try {
    const createdMessage = await createOwnMessage(payload)

    messages.value = [...messages.value, createdMessage]
    messageInbox.syncMessages(messages.value, currentUserId.value)
    contacts.value = contacts.value.some((contact) => contact.id === payload.receiverId)
      ? contacts.value
      : [
          ...contacts.value,
          {
            id: createdMessage.receiverId,
            displayName: createdMessage.receiverDisplayName,
            roleName: null,
          },
        ]

    selectedThreadKey.value = buildThreadKey(payload.receiverId, createdMessage.subject)
    isComposeMode.value = false
    composeForm.receiverId = ''
    composeForm.subject = ''
    composeForm.message = ''
    replyBody.value = ''
    await clearComposeIntent()
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

function formatThreadTimestamp(value: string): string {
  const date = new Date(value)
  return date.toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
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
        description="Organiza tus conversaciones por hilos y responde desde un unico espacio."
      />

      <section class="messages-layout panel panel-pad-lg">
        <aside class="messages-sidebar panel-stack-md">
          <button class="action-button messages-sidebar__compose" type="button" @click="openCompose">
            <SquarePen :size="16" aria-hidden="true" />
            Nuevo mensaje
          </button>

          <p v-if="threads.length === 0" class="ui-copy-muted">
            Todavia no tienes hilos iniciados.
          </p>

          <div v-else class="messages-thread-list">
            <button
              v-for="thread in threads"
              :key="thread.key"
              class="messages-thread-card"
              :class="{
                'messages-thread-card--active': !isComposeMode && selectedThreadKey === thread.key,
                'messages-thread-card--unread': thread.unreadCount > 0,
              }"
              type="button"
              @click="openThread(thread)"
            >
              <div class="messages-thread-card__header">
                <strong class="messages-thread-card__subject">{{ thread.subject }}</strong>
                <span v-if="thread.unreadCount > 0" class="messages-thread-card__badge">
                  {{ thread.unreadCount }}
                </span>
              </div>

              <div class="messages-thread-card__meta">
                <span class="messages-thread-card__name">{{ thread.counterpartDisplayName }}</span>
                <span class="messages-thread-card__time">{{ formatThreadTimestamp(thread.lastMessageAt) }}</span>
              </div>
            </button>
          </div>
        </aside>

        <section class="messages-main panel-stack-lg">
          <div v-if="isComposeMode" class="panel-copy">
            <p class="ui-eyebrow">Nuevo mensaje</p>
            <h2 class="ui-title-card">Iniciar una conversacion</h2>
            <p class="ui-copy-muted">Selecciona el destinatario, define un asunto y escribe tu mensaje.</p>
          </div>

          <div v-else-if="activeThread" class="panel-copy">
            <p class="ui-eyebrow">
              {{ formatRoleLabel(activeThread.roleName) || 'Conversacion' }}
            </p>
            <h2 class="ui-title-card">{{ activeThread.subject }}</h2>
            <p class="ui-copy-muted">
              {{ activeThread.counterpartDisplayName }} · Ultimo mensaje el
              {{ formatThreadTimestamp(activeThread.lastMessageAt) }}
            </p>
          </div>

          <div v-else class="panel-copy">
            <p class="ui-eyebrow">Mensajes</p>
            <h2 class="ui-title-card">Selecciona un hilo</h2>
            <p class="ui-copy-muted">O pulsa en “Nuevo mensaje” para empezar una conversacion.</p>
          </div>

          <p v-if="sendError" class="status-message status-message--error">{{ sendError }}</p>

          <form
            v-if="isComposeMode"
            class="messages-composer panel panel-pad-lg panel-stack-md"
            @submit.prevent="sendNewMessage"
          >
            <label class="messages-field">
              <span>Destinatario</span>
              <select v-model="composeForm.receiverId">
                <option value="">Selecciona un usuario</option>
                <option v-for="contact in contacts" :key="contact.id" :value="String(contact.id)">
                  {{ contact.displayName }}
                  {{ formatRoleLabel(contact.roleName) ? ` · ${formatRoleLabel(contact.roleName)}` : '' }}
                </option>
              </select>
            </label>

            <label class="messages-field">
              <span>Asunto</span>
              <input
                v-model="composeForm.subject"
                type="text"
                maxlength="255"
                placeholder="Asunto del mensaje"
              />
            </label>

            <label class="messages-field">
              <span>Mensaje</span>
              <textarea
                v-model="composeForm.message"
                rows="8"
                maxlength="500"
                placeholder="Escribe tu mensaje"
              ></textarea>
            </label>

            <div class="messages-composer__actions">
              <button class="action-button action-button--ghost" type="button" @click="cancelCompose">
                Cancelar
              </button>
              <button class="action-button" type="submit" :disabled="sending">
                <Send :size="16" aria-hidden="true" />
                {{ sending ? 'Enviando...' : 'Enviar mensaje' }}
              </button>
            </div>
          </form>

          <template v-else-if="activeThread">
            <div class="messages-conversation panel panel-pad-lg panel-stack-md">
              <article
                v-for="message in activeMessages"
                :key="message.id"
                class="messages-bubble"
                :class="{ 'messages-bubble--outgoing': isOutgoingMessage(message) }"
              >
                <div class="messages-bubble__meta">
                  <strong>{{ isOutgoingMessage(message) ? 'Tú' : message.senderDisplayName }}</strong>
                  <span>{{ formatThreadTimestamp(message.sentAt) }}</span>
                </div>
                <p class="messages-bubble__body">{{ message.message }}</p>
              </article>
            </div>

            <form class="messages-reply panel panel-pad-lg panel-stack-md" @submit.prevent="sendReply">
              <label class="messages-field">
                <span>Responder</span>
                <textarea
                  v-model="replyBody"
                  rows="5"
                  maxlength="500"
                  placeholder="Escribe tu respuesta"
                ></textarea>
              </label>

              <div class="messages-composer__actions">
                <button class="action-button" type="submit" :disabled="sending">
                  <Send :size="16" aria-hidden="true" />
                  {{ sending ? 'Enviando...' : 'Enviar respuesta' }}
                </button>
              </div>
            </form>
          </template>

          <div v-else class="messages-empty panel panel-pad-lg panel-stack-sm">
            <MessageSquare :size="20" aria-hidden="true" />
            <p class="ui-copy-muted">
              No tienes ningun hilo seleccionado. Pulsa en “Nuevo mensaje” para empezar uno.
            </p>
          </div>
        </section>
      </section>
    </template>
  </main>
</template>

<style scoped>
.messages-layout {
  display: grid;
  align-items: start;
  gap: var(--space-lg);
  grid-template-columns: minmax(240px, 280px) minmax(0, 1fr);
}

.messages-sidebar {
  align-content: start;
}

.messages-sidebar__compose {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  min-height: 42px;
  padding: 0 16px;
  align-self: start;
  background: linear-gradient(180deg, #2f8f47 0%, #216835 100%);
  border-color: rgba(121, 231, 155, 0.24);
  box-shadow: 0 18px 32px rgba(14, 44, 19, 0.22);
}

.messages-sidebar__compose:hover {
  filter: brightness(1.04);
}

.messages-composer__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-xs);
}

.messages-thread-list {
  display: grid;
  align-content: start;
  grid-auto-rows: min-content;
  gap: var(--space-sm);
}

.messages-thread-card {
  width: 100%;
  display: grid;
  gap: 6px;
  padding: 10px 12px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
  text-align: left;
}

.messages-thread-card--active {
  border-color: rgba(255, 76, 58, 0.42);
  background: rgba(255, 76, 58, 0.09);
}

.messages-thread-card--unread {
  box-shadow: inset 3px 0 0 rgba(255, 76, 58, 0.82);
}

.messages-thread-card__header,
.messages-thread-card__meta,
.messages-bubble__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-sm);
}

.messages-thread-card__subject {
  color: var(--text-strong);
  font-size: var(--fs-body-sm);
  line-height: 1.4;
}

.messages-thread-card__meta {
  color: var(--text-muted);
  font-size: var(--fs-caption);
}

.messages-thread-card__name {
  font-weight: 600;
}

.messages-thread-card__time,
.messages-bubble__meta span {
  white-space: nowrap;
}

.messages-thread-card__badge {
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

.messages-conversation,
.messages-empty,
.messages-composer,
.messages-reply {
  min-height: 220px;
}

.messages-reply {
  border-color: rgba(255, 76, 58, 0.3);
  background:
    linear-gradient(180deg, rgba(255, 76, 58, 0.08) 0%, rgba(255, 76, 58, 0.02) 100%),
    var(--surface-panel-gradient);
  box-shadow:
    inset 0 0 0 1px rgba(255, 76, 58, 0.06),
    var(--shadow-panel);
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
  .messages-thread-card__header,
  .messages-thread-card__meta,
  .messages-bubble__meta,
  .messages-composer__actions {
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
