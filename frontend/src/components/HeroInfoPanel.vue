<script setup lang="ts">
// Panel auxiliar para meter información destacada dentro de un hero.
defineProps<{
  label: string
  caption: string
}>()
</script>

<template>
  <div class="hero-info-panel panel panel-glass panel-pad-md panel-stack-md">
    <p class="ui-eyebrow ui-eyebrow--muted">{{ label }}</p>
    <div class="hero-info-panel__value">
      <slot />
    </div>
    <p class="hero-info-panel__caption ui-copy-muted">{{ caption }}</p>
  </div>
</template>

<style scoped>
.hero-info-panel {
  width: 100%;
  min-height: 100%;
  align-content: start;
}

.hero-info-panel__value {
  display: grid;
  gap: var(--space-xs);
}

.hero-info-panel__value :deep(strong) {
  color: var(--text-strong);
  font-family: var(--font-heading);
  font-size: var(--fs-title-highlight);
  line-height: 1;
}

.hero-info-panel__value :deep(span) {
  color: var(--text-body);
  font-size: var(--fs-body);
}

.hero-info-panel__caption {
  font-size: var(--fs-body);
}

@media (max-width: 640px) {
  .hero-info-panel__value :deep(strong) {
    font-size: 2.2rem;
  }
}
</style>
