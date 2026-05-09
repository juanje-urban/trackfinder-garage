<script setup lang="ts">
// Tarjeta pequeña para datos destacados del detalle de evento.
withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    subtitle?: string
    tone?: 'default' | 'accent' | 'success' | 'closed' | 'full' | 'urgent' | 'limited' | 'open'
  }>(),
  {
    subtitle: '',
    tone: 'default',
  },
)
</script>

<template>
  <section class="detail-info-card panel panel-pad-md panel-stack-sm" :class="`detail-info-card--${tone}`">
    <p class="ui-eyebrow">{{ eyebrow }}</p>
    <strong class="ui-title-detail">{{ title }}</strong>
    <span v-if="subtitle" class="ui-copy-muted">{{ subtitle }}</span>
    <div v-if="$slots.default" class="detail-info-card__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.detail-info-card__body {
  margin-top: 6px;
}

.detail-info-card--success,
.detail-info-card--open {
  border-color: var(--success-border);
}

.detail-info-card--limited {
  border-color: rgba(255, 205, 92, 0.42);
  background:
    linear-gradient(180deg, rgba(255, 191, 60, 0.12) 0%, rgba(255, 191, 60, 0.04) 100%),
    var(--surface-panel-gradient);
}

.detail-info-card--urgent {
  border-color: rgba(255, 128, 118, 0.44);
  background:
    linear-gradient(180deg, rgba(255, 45, 32, 0.14) 0%, rgba(255, 45, 32, 0.05) 100%),
    var(--surface-panel-gradient);
}

.detail-info-card--full {
  border-color: rgba(255, 119, 119, 0.5);
  background:
    linear-gradient(180deg, rgba(172, 35, 35, 0.18) 0%, rgba(132, 23, 23, 0.07) 100%),
    var(--surface-panel-gradient);
}

.detail-info-card--closed {
  border-color: rgba(230, 213, 207, 0.28);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.02) 100%),
    var(--surface-panel-gradient);
}

.detail-info-card--success {
  background:
    linear-gradient(180deg, rgba(58, 215, 134, 0.12) 0%, rgba(58, 215, 134, 0.04) 100%),
    var(--surface-panel-gradient);
}

.detail-info-card--success .ui-eyebrow,
.detail-info-card--open .ui-eyebrow {
  color: var(--success-text);
}

.detail-info-card--limited .ui-eyebrow {
  color: var(--racing-amber);
}

.detail-info-card--full .ui-eyebrow {
  color: #ffb0a9;
}

.detail-info-card--urgent .ui-eyebrow {
  color: #ffb0a9;
}

.detail-info-card--closed .ui-eyebrow {
  color: var(--text-muted);
}

.detail-info-card--success .ui-title-detail,
.detail-info-card--open .ui-title-detail {
  color: var(--success-text);
}

.detail-info-card--limited .ui-title-detail {
  color: #ffe5a5;
}

.detail-info-card--full .ui-title-detail {
  color: #fff0ed;
}

.detail-info-card--urgent .ui-title-detail {
  color: #fff0ed;
}

.detail-info-card--closed .ui-title-detail {
  color: var(--text-body);
}
</style>
