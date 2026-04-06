<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    description?: string
  }>(),
  {
    description: '',
  },
)
</script>

<template>
  <section class="section-card panel panel-pad-lg panel-stack-lg">
    <header class="panel-copy">
      <p class="ui-eyebrow">{{ eyebrow }}</p>
      <h2 class="ui-title-section">{{ title }}</h2>
      <p v-if="description" class="ui-copy-muted">{{ description }}</p>
    </header>

    <div class="section-card__body">
      <slot />
    </div>
  </section>
</template>
