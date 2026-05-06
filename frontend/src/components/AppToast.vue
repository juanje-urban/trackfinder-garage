<script setup lang="ts">
import { useToast } from '@/composables/useToast'

// El toast se alimenta del composable global; cualquier vista puede dispararlo.
const { isOpen, message, tone, hideToast } = useToast()
</script>

<template>
  <Transition name="app-toast">
    <div
      v-if="isOpen"
      class="app-toast"
      :class="`app-toast--${tone}`"
      role="status"
      aria-live="polite"
    >
      <span class="app-toast__message">{{ message }}</span>
      <button class="app-toast__close" type="button" @click="hideToast">
        Cerrar
      </button>
    </div>
  </Transition>
</template>

<style scoped>
.app-toast {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 80;
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  max-width: min(420px, calc(100vw - 32px));
  padding: 14px 16px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: rgba(13, 19, 29, 0.94);
  color: var(--text-strong);
  box-shadow: var(--shadow-panel);
  backdrop-filter: blur(14px);
}

.app-toast--success {
  border-color: var(--success-border);
  background:
    linear-gradient(180deg, rgba(17, 31, 24, 0.96) 0%, rgba(11, 22, 18, 0.96) 100%);
}

.app-toast--error {
  border-color: var(--error-border);
  background:
    linear-gradient(180deg, rgba(52, 17, 17, 0.96) 0%, rgba(32, 12, 12, 0.96) 100%);
  color: var(--error-text);
}

.app-toast--neutral {
  background:
    linear-gradient(180deg, rgba(24, 16, 16, 0.96) 0%, rgba(17, 12, 12, 0.96) 100%);
}

.app-toast__message {
  flex: 1;
  min-width: 0;
  font-weight: 600;
  line-height: 1.45;
}

.app-toast__close {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font-size: var(--fs-caption);
  font-weight: 700;
}

.app-toast-enter-active,
.app-toast-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.app-toast-enter-from,
.app-toast-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 640px) {
  .app-toast {
    right: 16px;
    left: 16px;
    bottom: 16px;
    max-width: none;
  }
}
</style>
