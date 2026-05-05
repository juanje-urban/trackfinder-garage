<script setup lang="ts">
import { Send } from 'lucide-vue-next'
import type { MessageThread } from '@/types/message'
import { formatMessageTimestamp } from '@/utils/messageFormatting'

// Conversación abierta: pinta burbujas y el formulario de respuesta.
const props = defineProps<{
  thread: MessageThread
  currentUserId: number | null
  replyBody: string
  sendError: string
  sending: boolean
}>()

defineEmits<{
  // El texto de respuesta vive en MessagesView para centralizar el envío.
  'update:replyBody': [value: string]
  submit: []
}>()

function isOutgoingMessage(senderId: number): boolean {
  // Distingo mis mensajes para alinearlos y colorearlos distinto.
  return senderId === props.currentUserId
}
</script>

<template>
  <div class="panel-stack-lg">
    <p v-if="sendError" class="status-message status-message--error">{{ sendError }}</p>

    <div class="messages-conversation panel panel-pad-lg panel-stack-md">
      <article
        v-for="message in thread.messages"
        :key="message.id"
        class="messages-bubble"
        :class="{ 'messages-bubble--outgoing': isOutgoingMessage(message.senderId) }"
      >
        <div class="messages-bubble__meta">
          <strong>{{ isOutgoingMessage(message.senderId) ? 'Tu' : message.senderDisplayName }}</strong>
          <span>{{ formatMessageTimestamp(message.sentAt) }}</span>
        </div>
        <p class="messages-bubble__body">{{ message.message }}</p>
      </article>
    </div>

    <form class="messages-reply panel panel-pad-lg panel-stack-md" @submit.prevent="$emit('submit')">
      <label class="surface-field">
        <span class="surface-field__label">Responder</span>
        <textarea
          :value="replyBody"
          rows="5"
          maxlength="500"
          placeholder="Escribe tu respuesta"
          @input="$emit('update:replyBody', ($event.target as HTMLTextAreaElement).value)"
        ></textarea>
      </label>

      <div class="action-row action-row--end">
        <button class="action-button" type="submit" :disabled="sending">
          <Send :size="16" aria-hidden="true" />
          {{ sending ? 'Enviando...' : 'Enviar respuesta' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.messages-conversation,
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

.messages-bubble__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-sm);
}

.messages-bubble__meta span {
  white-space: nowrap;
}

.messages-bubble__body {
  margin: 0;
  color: var(--text-body);
  line-height: 1.6;
  white-space: pre-wrap;
}

@media (max-width: 720px) {
  .messages-bubble {
    max-width: 100%;
  }

  .messages-bubble__meta,
  .action-row {
    flex-direction: column;
    align-items: start;
  }

  .action-row .action-button {
    width: 100%;
  }
}
</style>
