<script setup lang="ts">
import type { AuthSession } from '@/types/auth'

defineProps<{
  session: AuthSession | null
  dialogTitle: string
  dialogSubtitle: string
  profileMonogram: string
}>()

defineEmits<{
  close: []
  logout: []
}>()
</script>

<template>
  <div class="auth-dialog__content auth-dialog__content--account">
    <div class="auth-account__avatar">{{ profileMonogram }}</div>
    <p class="ui-eyebrow">Acceso activo</p>
    <h2 id="auth-dialog-title" class="ui-title-section">{{ dialogTitle }}</h2>
    <p class="ui-copy-muted">{{ dialogSubtitle }}</p>

    <div class="auth-account__summary">
      <p><strong>Alias:</strong> {{ session?.displayName }}</p>
      <p><strong>Correo:</strong> {{ session?.email }}</p>
    </div>

    <div class="auth-dialog__actions">
      <button class="action-button" type="button" @click="$emit('close')">Seguir navegando</button>
      <button class="action-button action-button--ghost" type="button" @click="$emit('logout')">
        Cerrar sesion
      </button>
    </div>
  </div>
</template>

<style scoped>
.auth-dialog__content {
  display: grid;
  gap: var(--space-2xl);
}

.auth-dialog__content--account {
  justify-items: start;
}

.auth-account__avatar {
  width: 74px;
  height: 74px;
  display: grid;
  place-items: center;
  border: 1px solid var(--line-strong);
  border-radius: 22px;
  color: var(--text-strong);
  font-size: 1.4rem;
  font-weight: 800;
  background: var(--accent-gradient-horizontal);
  box-shadow: var(--accent-shadow);
}

.auth-account__summary {
  width: 100%;
  display: grid;
  gap: var(--space-sm);
  padding: var(--space-xl);
  border: 1px solid var(--line-faint);
  border-radius: 18px;
  background: var(--surface-glass);
}

.auth-account__summary p {
  margin: 0;
  color: var(--text-body);
}

.auth-dialog__actions {
  width: 100%;
  display: grid;
  gap: var(--space-md);
}
</style>
