<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    isOpen: boolean
    ariaLabel: string
    eyebrow?: string
    title?: string
    closeLabel?: string
    width?: string
    light?: boolean
  }>(),
  {
    eyebrow: '',
    title: '',
    closeLabel: 'Cerrar',
    width: '720px',
    light: false,
  },
)

defineEmits<{
  close: []
}>()

const panelStyle = computed(() => ({
  width: `min(${props.width}, calc(100vw - 32px))`,
}))
</script>

<template>
  <div
    v-if="props.isOpen"
    class="app-modal"
    role="dialog"
    aria-modal="true"
    :aria-label="props.ariaLabel"
    @click.self="$emit('close')"
  >
    <section
      class="app-modal__panel panel panel-pad-lg panel-stack-lg"
      :class="{ 'app-modal__panel--light': props.light }"
      :style="panelStyle"
    >
      <header v-if="props.eyebrow || props.title || $slots.header" class="app-modal__header">
        <slot name="header">
          <div class="panel-copy">
            <p v-if="props.eyebrow" class="ui-eyebrow">{{ props.eyebrow }}</p>
            <h2 v-if="props.title" class="ui-title-section">{{ props.title }}</h2>
          </div>
        </slot>

        <button class="app-modal__close" type="button" @click="$emit('close')">
          {{ props.closeLabel }}
        </button>
      </header>

      <slot />
    </section>
  </div>
</template>

<style scoped>
.app-modal {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(8, 8, 10, 0.74);
  backdrop-filter: blur(10px);
}

.app-modal__panel {
  max-height: calc(100vh - 32px);
  overflow: auto;
}

.app-modal__panel--light {
  border-color: rgba(48, 17, 15, 0.12);
  background: var(--surface-light);
  color: var(--text-on-light);
}

.app-modal__header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-lg);
}

.app-modal__close {
  padding: 10px 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-pill);
  color: var(--text-body);
  background: var(--surface-glass);
  font-weight: 700;
}

.app-modal__panel--light .app-modal__close {
  border-color: rgba(48, 17, 15, 0.14);
  background: rgba(48, 17, 15, 0.06);
  color: var(--text-on-light);
}

@media (max-width: 720px) {
  .app-modal {
    padding: 16px;
  }

  .app-modal__header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
