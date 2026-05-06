<script setup lang="ts">
// Métrica pequeña y presentacional. El padre le pasa texto, valor y tono visual.
withDefaults(
  defineProps<{
    label: string
    value: string
    hint?: string
    tone?: 'default' | 'accent' | 'success'
  }>(),
  {
    hint: '',
    tone: 'default',
  },
)
</script>

<template>
  <article class="metric-card panel panel-pad-md panel-stack-md" :class="`metric-card--${tone}`">
    <p class="ui-stat-label">{{ label }}</p>
    <p class="metric-card__value ui-title-metric">{{ value }}</p>
    <p v-if="hint" class="metric-card__hint ui-copy-meta">{{ hint }}</p>
  </article>
</template>

<style scoped>
.metric-card {
  min-height: 132px;
  align-content: start;
}

.metric-card__value {
  color: var(--text-strong);
}

.metric-card--accent {
  border-color: var(--line-strong);
}

.metric-card--accent .metric-card__value {
  color: var(--text-strong);
}

.metric-card--success .metric-card__value,
.metric-card--success .metric-card__hint {
  color: var(--success-text);
}
</style>
