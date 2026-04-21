<script setup lang="ts">
import { MessageSquare } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MessageComposePanel from '@/components/messages/MessageComposePanel.vue'
import MessageConversationPanel from '@/components/messages/MessageConversationPanel.vue'
import MessageThreadSidebar from '@/components/messages/MessageThreadSidebar.vue'
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
import { formatMessageRoleLabel, formatMessageTimestamp } from '@/utils/messageFormatting'
import { buildMessageThreadKey, buildMessageThreads } from '@/utils/messageThreads'

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

const threads = computed(() =>
  buildMessageThreads(messages.value, currentUserId.value, contactsById.value),
)

const activeThread = computed(
  () => threads.value.find((thread) => thread.key === selectedThreadKey.value) ?? null,
)

onMounted(async () => {
  if (!isAuthenticated.value) {
    loading.value = false
    error.value = 'Necesitas iniciar sesión para acceder a tus mensajes.'
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
        401: 'Tu sesión ha caducado. Inicia sesión de nuevo.',
      },
    })
  } finally {
    loading.value = false
  }
}

function syncSelectedThread() {
  if (threads.value.some((thread) => thread.key === selectedThreadKey.value)) {
    return
  }

  selectedThreadKey.value = threads.value[0]?.key ?? null
}

function openCompose() {
  startCompose('')
}

function startCompose(receiverId: string) {
  isComposeMode.value = true
  selectedThreadKey.value = null
  replyBody.value = ''
  sendError.value = ''
  composeForm.receiverId = receiverId
  composeForm.subject = ''
  composeForm.message = ''
}

function openThread(thread: { key: string }) {
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
  syncSelectedThread()
}

function applyComposeIntentFromRoute() {
  const receiverIdRaw = route.query.receiverId
  const receiverId = typeof receiverIdRaw === 'string' ? Number(receiverIdRaw) : Number.NaN
  const hasValidReceiver =
    Number.isFinite(receiverId) &&
    contacts.value.some((contact) => contact.id === receiverId)

  if (hasValidReceiver) {
    startCompose(String(receiverId))
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

    selectedThreadKey.value = buildMessageThreadKey(payload.receiverId, createdMessage.subject)
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
        401: 'Tu sesión ha caducado. Inicia sesión de nuevo.',
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
      <h1 class="ui-title-section">No se pudo cargar la mensajería</h1>
      <p class="ui-copy-muted">{{ error }}</p>
      <button class="action-button" type="button" @click="loadMessagesPage">Reintentar</button>
    </section>

    <template v-else>
      <PageHero
        eyebrow="Mensajes"
        title="Bandeja de entrada"
        description="Contacta con otros usuarios de la platadorma."
      />

      <section class="messages-layout panel panel-pad-lg">
        <MessageThreadSidebar
          :threads="threads"
          :selected-thread-key="selectedThreadKey"
          :is-compose-mode="isComposeMode"
          @compose="openCompose"
          @open-thread="openThread"
        />

        <section class="messages-main panel-stack-lg">
          <div v-if="isComposeMode" class="panel-copy">
            <p class="ui-eyebrow">Nuevo mensaje</p>
            <h2 class="ui-title-card">Iniciar una conversación</h2>
            <p class="ui-copy-muted">Selecciona el destinatario, define un asunto y escribe tu mensaje.</p>
          </div>

          <div v-else-if="activeThread" class="panel-copy">
            <p class="ui-eyebrow">
              {{ formatMessageRoleLabel(activeThread.roleName) || 'Conversación' }}
            </p>
            <h2 class="ui-title-card">{{ activeThread.subject }}</h2>
            <p class="ui-copy-muted">
              {{ activeThread.counterpartDisplayName }} · Último mensaje el
              {{ formatMessageTimestamp(activeThread.lastMessageAt) }}
            </p>
          </div>

          <div v-else class="panel-copy">
            <p class="ui-eyebrow">Mensajes</p>
            <h2 class="ui-title-card">Selecciona un hilo</h2>
            <p class="ui-copy-muted">O pulsa en "Nuevo mensaje" para empezar una conversación.</p>
          </div>

          <MessageComposePanel
            v-if="isComposeMode"
            :contacts="contacts"
            :receiver-id="composeForm.receiverId"
            :subject="composeForm.subject"
            :message="composeForm.message"
            :send-error="sendError"
            :sending="sending"
            @update:receiver-id="composeForm.receiverId = $event"
            @update:subject="composeForm.subject = $event"
            @update:message="composeForm.message = $event"
            @cancel="cancelCompose"
            @submit="sendNewMessage"
          />

          <MessageConversationPanel
            v-else-if="activeThread"
            :thread="activeThread"
            :current-user-id="currentUserId"
            :reply-body="replyBody"
            :send-error="sendError"
            :sending="sending"
            @update:reply-body="replyBody = $event"
            @submit="sendReply"
          />

          <div v-else class="messages-empty panel panel-pad-lg panel-stack-sm">
            <MessageSquare :size="20" aria-hidden="true" />
            <p class="ui-copy-muted">
              No tienes ningún hilo seleccionado. Pulsa en "Nuevo mensaje" para empezar uno.
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

@media (max-width: 980px) {
  .messages-layout {
    grid-template-columns: 1fr;
  }
}
</style>
