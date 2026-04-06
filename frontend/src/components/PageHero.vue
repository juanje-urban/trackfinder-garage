<script setup lang="ts">
import { computed, useSlots } from 'vue'

withDefaults(
  defineProps<{
  eyebrow: string
  title: string
  description: string
    imageUrl?: string
    imageAlt?: string
  }>(),
  {
    imageUrl: '',
    imageAlt: '',
  },
)

const slots = useSlots()

const hasAside = computed(() => Boolean(slots.aside))

const heroClasses = computed(() => ({
  'page-hero--with-aside': hasAside.value,
}))
</script>

<template>
  <section class="page-hero panel panel-pad-xl" :class="heroClasses">
    <div class="page-hero__copy panel-copy">
      <p class="ui-eyebrow">{{ eyebrow }}</p>
      <h1 class="ui-title-hero">{{ title }}</h1>
      <p class="page-hero__description ui-copy-lead">{{ description }}</p>
    </div>

    <aside v-if="$slots.aside" class="page-hero__aside">
      <slot name="aside" />
    </aside>

    <div v-if="imageUrl" class="page-hero__image">
      <img :src="imageUrl" :alt="imageAlt" />
    </div>
  </section>
</template>

<style scoped>
.page-hero {
  display: grid;
  gap: var(--space-2xl);
  overflow: hidden;
  position: relative;
}

.page-hero--with-aside {
  grid-template-columns: minmax(0, 1.4fr) minmax(250px, 0.8fr);
}

.page-hero::before {
  content: "";
  position: absolute;
  inset: auto -80px -120px auto;
  width: 260px;
  height: 260px;
  border-radius: 999px;
  background: var(--accent-soft);
  filter: blur(24px);
}

.page-hero__copy {
  position: relative;
  z-index: 1;
}

.page-hero__description {
  max-width: 58ch;
}

.page-hero__aside {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: stretch;
}

.page-hero__image {
  grid-column: 1 / -1;
  min-height: 260px;
  border-radius: var(--radius-inner);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.page-hero__image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

@media (max-width: 860px) {
  .page-hero {
    grid-template-columns: 1fr;
  }
}
</style>
