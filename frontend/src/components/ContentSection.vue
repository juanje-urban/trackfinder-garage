<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow?: string
    title?: string
    hint?: string
    loading?: boolean
    loadingMessage?: string
    error?: string
    empty?: boolean
    emptyMessage?: string
  }>(),
  {
    eyebrow: '',
    title: '',
    hint: '',
    loading: false,
    loadingMessage: '',
    error: '',
    empty: false,
    emptyMessage: '',
  },
)
</script>

<template>
  <section class="section-stack">
    <div v-if="eyebrow || title || hint || $slots.action" class="section-heading">
      <div class="section-heading__copy">
        <p v-if="eyebrow" class="section-heading__eyebrow">{{ eyebrow }}</p>
        <h2 v-if="title" class="section-heading__title">{{ title }}</h2>
        <p v-if="hint" class="section-heading__hint">{{ hint }}</p>
      </div>

      <slot name="action" />
    </div>

    <p v-if="loading" class="status-message">{{ loadingMessage }}</p>
    <p v-else-if="error" class="status-message status-message--error">{{ error }}</p>
    <p v-else-if="empty" class="status-message">{{ emptyMessage }}</p>
    <slot v-else />
  </section>
</template>
