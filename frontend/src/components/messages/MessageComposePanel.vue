<script setup lang="ts">
import { Send } from 'lucide-vue-next'
import type { MessageContact } from '@/types/message'
import { formatMessageRoleLabel } from '@/utils/messageFormatting'

defineProps<{
  contacts: MessageContact[]
  receiverId: string
  subject: string
  message: string
  sendError: string
  sending: boolean
}>()

defineEmits<{
  'update:receiverId': [value: string]
  'update:subject': [value: string]
  'update:message': [value: string]
  cancel: []
  submit: []
}>()
</script>

<template>
  <form class="panel panel-pad-lg panel-stack-md" @submit.prevent="$emit('submit')">
    <p v-if="sendError" class="status-message status-message--error">{{ sendError }}</p>

    <label class="surface-field">
      <span class="surface-field__label">Destinatario</span>
      <select :value="receiverId" @change="$emit('update:receiverId', ($event.target as HTMLSelectElement).value)">
        <option value="">Selecciona un usuario</option>
        <option v-for="contact in contacts" :key="contact.id" :value="String(contact.id)">
          {{ contact.displayName }}
          {{ formatMessageRoleLabel(contact.roleName) ? ` · ${formatMessageRoleLabel(contact.roleName)}` : '' }}
        </option>
      </select>
    </label>

    <label class="surface-field">
      <span class="surface-field__label">Asunto</span>
      <input
        :value="subject"
        type="text"
        maxlength="255"
        placeholder="Asunto del mensaje"
        @input="$emit('update:subject', ($event.target as HTMLInputElement).value)"
      />
    </label>

    <label class="surface-field">
      <span class="surface-field__label">Mensaje</span>
      <textarea
        :value="message"
        rows="8"
        maxlength="500"
        placeholder="Escribe tu mensaje"
        @input="$emit('update:message', ($event.target as HTMLTextAreaElement).value)"
      ></textarea>
    </label>

    <div class="action-row action-row--end">
      <button class="action-button action-button--ghost" type="button" @click="$emit('cancel')">
        Cancelar
      </button>
      <button class="action-button" type="submit" :disabled="sending">
        <Send :size="16" aria-hidden="true" />
        {{ sending ? 'Enviando...' : 'Enviar mensaje' }}
      </button>
    </div>
  </form>
</template>
