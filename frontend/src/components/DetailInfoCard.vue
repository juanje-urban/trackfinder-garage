<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    subtitle?: string
    tone?: 'default' | 'accent' | 'success'
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

.detail-info-card--success {
  border-color: var(--success-border);
}

.detail-info-card--success .detail-info-card__eyebrow {
  color: var(--success-text);
}
</style>
