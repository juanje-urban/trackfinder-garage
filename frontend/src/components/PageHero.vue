<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    description: string
    imageUrl?: string
    mediaAlt?: string
  }>(),
  {
    imageUrl: '',
    mediaAlt: '',
  },
)

const slots = useSlots()

// Los slots son huecos opcionales que el padre puede rellenar con contenido propio.
const hasAside = computed(() => Boolean(slots.aside))
const hasImage = computed(() => Boolean(props.imageUrl))

// Cambio clases según el tipo de hero sin duplicar plantillas enteras.
const heroClasses = computed(() => ({
  'page-hero--with-aside': hasAside.value,
  'page-hero--immersive': hasImage.value,
}))
</script>

<template>
  <section class="page-hero panel panel-pad-xl" :class="heroClasses">
    <template v-if="hasImage">
      <p class="ui-eyebrow page-hero__eyebrow">{{ props.eyebrow }}</p>

      <div class="page-hero__media">
        <div class="page-hero__image">
          <img :src="props.imageUrl" :alt="props.mediaAlt" />
        </div>

        <div class="page-hero__copy page-hero__copy--immersive panel-copy">
          <h1 class="ui-title-hero">{{ props.title }}</h1>
          <p class="page-hero__description ui-copy-lead">{{ props.description }}</p>
        </div>

        <aside v-if="$slots.aside" class="page-hero__aside page-hero__aside--floating">
          <slot name="aside" />
        </aside>
      </div>
    </template>

    <template v-else>
      <div class="page-hero__main">
        <div class="page-hero__copy panel-copy">
          <p class="ui-eyebrow">{{ props.eyebrow }}</p>
          <h1 class="ui-title-hero">{{ props.title }}</h1>
          <p class="page-hero__description ui-copy-lead">{{ props.description }}</p>
        </div>
      </div>

      <aside v-if="$slots.aside" class="page-hero__aside">
        <slot name="aside" />
      </aside>
    </template>
  </section>
</template>

<style scoped>
.page-hero {
  display: grid;
  gap: var(--space-2xl);
  position: relative;
}

.page-hero--with-aside:not(.page-hero--immersive) {
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.85fr);
  align-items: start;
}

.page-hero:not(.page-hero--immersive)::before {
  content: "";
  position: absolute;
  inset: auto -80px -120px auto;
  width: 260px;
  height: 260px;
  border-radius: 999px;
  background: var(--accent-soft);
  filter: blur(24px);
}

.page-hero--immersive {
  gap: var(--space-lg);
  overflow: visible;
}

.page-hero__eyebrow {
  position: relative;
  z-index: 1;
}

.page-hero__media {
  position: relative;
  min-height: clamp(360px, 52vw, 520px);
  border-radius: var(--radius-inner);
  overflow: hidden;
  isolation: isolate;
  background: color-mix(in srgb, var(--surface-strong) 86%, black);
}

.page-hero__main {
  display: grid;
  gap: var(--space-xl);
  min-width: 0;
  align-content: start;
  position: relative;
  z-index: 1;
}

.page-hero__copy {
  position: relative;
  min-width: 0;
}

.page-hero__copy--immersive {
  position: absolute;
  left: clamp(20px, 3vw, 34px);
  bottom: clamp(20px, 4vw, 34px);
  z-index: 2;
  max-width: min(58ch, calc(100% - 470px));
}

.page-hero__copy--immersive::before {
  content: "";
  position: absolute;
  inset: -16px -10px -20px -14px;
  z-index: -1;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(10, 16, 26, 0) 0%, rgba(10, 16, 26, 0.16) 18%, rgba(10, 16, 26, 0.42) 42%, rgba(10, 16, 26, 0.86) 100%);
  pointer-events: none;
  filter: blur(10px);
}

.page-hero__copy--immersive :deep(.ui-title-hero),
.page-hero__copy--immersive :deep(.ui-copy-lead) {
  color: white;
  text-shadow: 0 6px 24px rgba(0, 0, 0, 0.44);
}

.page-hero__description {
  max-width: 58ch;
}

.page-hero__aside {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: stretch;
  min-width: 0;
}

.page-hero__aside--floating {
  position: absolute;
  top: clamp(18px, 3vw, 28px);
  right: clamp(18px, 3vw, 28px);
  width: min(420px, calc(100% - 36px));
  z-index: 3;
}

.page-hero__aside--floating :deep(.hero-info-panel) {
  min-height: auto;
  padding: var(--space-md) var(--space-lg);
  gap: var(--space-sm);
  background:
    linear-gradient(135deg, rgba(54, 149, 92, 0.24), rgba(54, 149, 92, 0) 38%),
    linear-gradient(180deg, rgba(18, 26, 38, 0.88), rgba(11, 18, 29, 0.94));
  border: 1px solid rgba(125, 230, 168, 0.24);
  box-shadow: 0 24px 52px rgba(3, 8, 16, 0.34);
}

.page-hero__aside--floating :deep(.ui-eyebrow--muted) {
  color: color-mix(in srgb, var(--success-text) 82%, white);
}

.page-hero__aside--floating :deep(.hero-info-panel__value strong) {
  font-size: clamp(1.55rem, 2.3vw, 1.92rem);
  line-height: 1.12;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-wrap: balance;
}

.page-hero__aside--floating :deep(.hero-info-panel__value span) {
  font-size: 0.94rem;
  line-height: 1.15;
}

.page-hero__aside--floating :deep(.hero-info-panel__caption) {
  font-size: 0.82rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.page-hero__image {
  position: absolute;
  inset: 0;
  min-height: 260px;
}

.page-hero__image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

@media (max-width: 860px) {
  .page-hero,
  .page-hero--with-aside:not(.page-hero--immersive) {
    grid-template-columns: 1fr;
  }

  .page-hero__copy--immersive {
    bottom: clamp(18px, 4vw, 26px);
    max-width: min(58ch, calc(100% - 40px));
  }

  .page-hero__aside--floating {
    width: min(360px, calc(100% - 36px));
  }
}

@media (max-width: 640px) {
  .page-hero__media {
    min-height: 420px;
  }

  .page-hero__aside--floating {
    top: 16px;
    right: 16px;
    width: min(290px, calc(100% - 32px));
  }

  .page-hero__copy--immersive {
    left: 16px;
    right: 16px;
    bottom: 16px;
    max-width: none;
  }
}
</style>
