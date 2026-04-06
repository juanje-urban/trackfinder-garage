<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    hint: string
    loading?: boolean
    loadingMessage?: string
    error?: string
    empty?: boolean
    emptyMessage?: string
  }>(),
  {
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
    <div class="section-heading">
      <div class="section-heading__copy">
        <p class="section-heading__eyebrow">{{ eyebrow }}</p>
        <h2 class="section-heading__title">{{ title }}</h2>
        <p class="section-heading__hint">{{ hint }}</p>
      </div>

      <slot name="action" />
    </div>

    <p v-if="loading" class="status-message">{{ loadingMessage }}</p>
    <p v-else-if="error" class="status-message status-message--error">{{ error }}</p>
    <p v-else-if="empty" class="status-message">{{ emptyMessage }}</p>
    <slot v-else />
  </section>
</template>
