<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  position: number
}>()

// Muestro 'P1', 'P2', etc. a partir de la posición numérica.
const label = computed(() => `P${props.position}`)
</script>

<template>
  <span
    class="ranking-position-badge"
    :class="`ranking-position-badge--${position}`"
  >
    {{ label }}
  </span>
</template>

<style scoped>
.ranking-position-badge {
  min-width: 48px;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-pill);
  text-align: center;
  font-family: var(--font-heading);
  font-weight: 900;
  letter-spacing: 0.08em;
  background: var(--surface-glass);
  color: var(--text-strong);
}

.ranking-position-badge--1 {
  background: var(--accent-emphasis);
  color: var(--text-strong);
}

.ranking-position-badge--2 {
  background: var(--racing-amber-soft);
  color: var(--racing-amber);
}

.ranking-position-badge--3 {
  background: var(--racing-blue-soft);
  color: var(--racing-blue);
}
</style>
