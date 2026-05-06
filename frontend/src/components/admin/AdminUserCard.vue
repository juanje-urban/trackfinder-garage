<script setup lang="ts">
import { Power, PowerOff } from 'lucide-vue-next'

// Tarjeta reutilizable para admins, organizadores y usuarios estándar.
const props = defineProps<{
  displayName: string
  realName?: string
  summary: string
  detail?: string
  noteLabel?: string
  noteTone?: 'warning'
  enabled: boolean
  disabled?: boolean
  protectedLabel?: string
}>()

defineEmits<{
  // Solo emito 'toggle'; el padre decide si habilita o deshabilita.
  toggle: []
}>()
</script>

<template>
  <article class="admin-user-card">
    <div class="panel-copy admin-user-card__copy">
      <h4 class="ui-title-card admin-user-card__heading">
        <span class="admin-user-card__alias">{{ props.displayName }}</span>
        <span v-if="props.realName" class="admin-user-card__real-name">{{ props.realName }}</span>
      </h4>
      <span
        v-if="props.noteLabel"
        class="badge admin-user-card__note"
        :class="props.noteTone ? `admin-user-card__note--${props.noteTone}` : 'badge--soft'"
      >
        {{ props.noteLabel }}
      </span>
      <p class="ui-copy-muted admin-user-card__meta">{{ props.summary }}</p>
      <p v-if="props.detail" class="ui-copy-muted admin-user-card__meta">{{ props.detail }}</p>
    </div>

    <button
      class="action-button admin-user-card__button"
      :class="props.enabled ? 'admin-user-card__button--danger' : 'admin-user-card__button--success'"
      type="button"
      :disabled="props.disabled"
      @click="$emit('toggle')"
    >
      <component :is="props.enabled ? PowerOff : Power" :size="16" aria-hidden="true" />
      {{ props.protectedLabel ?? (props.enabled ? 'Deshabilitar' : 'Habilitar') }}
    </button>
  </article>
</template>

<style scoped>
.admin-user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.admin-user-card__copy {
  min-width: 0;
  gap: 6px;
}

.admin-user-card__heading {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 6px 12px;
}

.admin-user-card__alias {
  color: var(--text-strong);
}

.admin-user-card__real-name {
  color: var(--text-muted);
  font-size: var(--fs-copy);
  font-weight: 600;
}

.admin-user-card__meta {
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-user-card__note {
  width: fit-content;
}

.admin-user-card__note--warning {
  background: rgba(184, 114, 21, 0.24);
  color: #ffdca7;
  border: 1px solid rgba(255, 196, 92, 0.34);
}

.admin-user-card__button {
  min-width: 136px;
}

.admin-user-card__button--success {
  background: linear-gradient(135deg, rgba(20, 150, 78, 0.94), rgba(11, 98, 49, 0.94));
  color: #f2fff5;
  box-shadow: 0 14px 30px rgba(11, 98, 49, 0.24);
}

.admin-user-card__button--danger {
  background: linear-gradient(135deg, rgba(214, 31, 31, 0.94), rgba(146, 12, 12, 0.94));
  color: #fff4f4;
  box-shadow: 0 14px 30px rgba(146, 12, 12, 0.24);
}

@media (max-width: 720px) {
  .admin-user-card {
    flex-direction: column;
    align-items: stretch;
  }

  .admin-user-card__meta {
    white-space: normal;
  }

  .admin-user-card__button {
    width: 100%;
  }
}
</style>
