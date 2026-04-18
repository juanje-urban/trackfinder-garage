<script setup lang="ts">
import { SquarePen } from 'lucide-vue-next'
import type { MessageThread } from '@/types/message'
import { formatMessageTimestamp } from '@/utils/messageFormatting'

defineProps<{
  threads: MessageThread[]
  selectedThreadKey: string | null
  isComposeMode: boolean
}>()

defineEmits<{
  compose: []
  openThread: [thread: MessageThread]
}>()
</script>

<template>
  <aside class="messages-sidebar panel-stack-md">
    <button class="action-button messages-sidebar__compose" type="button" @click="$emit('compose')">
      <SquarePen :size="16" aria-hidden="true" />
      Nuevo mensaje
    </button>

    <p v-if="threads.length === 0" class="ui-copy-muted">
      Todavía no tienes hilos iniciados.
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
        @click="$emit('openThread', thread)"
      >
        <div class="messages-thread-card__header">
          <strong class="messages-thread-card__subject">{{ thread.subject }}</strong>
          <span v-if="thread.unreadCount > 0" class="messages-thread-card__badge">
            {{ thread.unreadCount }}
          </span>
        </div>

        <div class="messages-thread-card__meta">
          <span class="messages-thread-card__name">{{ thread.counterpartDisplayName }}</span>
          <span class="messages-thread-card__time">{{ formatMessageTimestamp(thread.lastMessageAt) }}</span>
        </div>
      </button>
    </div>
  </aside>
</template>

<style scoped>
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
.messages-thread-card__meta {
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

.messages-thread-card__time {
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

@media (max-width: 720px) {
  .messages-thread-card__header,
  .messages-thread-card__meta {
    flex-direction: column;
    align-items: start;
  }
}
</style>
