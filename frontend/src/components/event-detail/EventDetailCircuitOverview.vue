<script setup lang="ts">
import type { Track } from '@/types/track'

defineProps<{
  track: Track
  secondGalleryImage?: string
}>()

defineEmits<{
  openPhoto: []
}>()
</script>

<template>
  <article class="panel panel-pad-lg panel-stack-lg">
    <div
      class="event-detail__circuit-overview"
      :class="{ 'event-detail__circuit-overview--with-photo': secondGalleryImage }"
    >
      <div class="panel-copy event-detail__circuit-copy">
        <p class="ui-eyebrow">Circuito</p>
        <h2 class="ui-title-section">{{ track.name }}</h2>
        <p class="ui-copy-muted">{{ track.location }}</p>
        <p class="ui-copy-body">{{ track.description }}</p>
      </div>

      <button
        v-if="secondGalleryImage"
        class="event-detail__circuit-photo-button"
        type="button"
        :aria-label="`Ampliar imagen de ${track.name}`"
        @click="$emit('openPhoto')"
      >
        <img
          class="event-detail__circuit-photo"
          :src="secondGalleryImage"
          :alt="`Vista del circuito ${track.name}`"
        />
        <span class="event-detail__circuit-photo-hint">Pulsa para ampliar</span>
      </button>
    </div>
  </article>
</template>

<style scoped>
.event-detail__circuit-overview {
  display: grid;
  gap: var(--space-lg);
}

.event-detail__circuit-copy {
  display: grid;
  gap: var(--space-sm);
  align-content: start;
}

.event-detail__circuit-overview--with-photo {
  grid-template-columns: minmax(0, 1.3fr) minmax(240px, 0.9fr);
  align-items: start;
}

.event-detail__circuit-photo-button {
  display: grid;
  gap: var(--space-sm);
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: zoom-in;
}

.event-detail__circuit-photo {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: var(--radius-inner);
  border: 1px solid var(--line-soft);
}

.event-detail__circuit-photo-hint {
  color: var(--text-muted);
  font-size: var(--fs-caption);
}

@media (max-width: 760px) {
  .event-detail__circuit-overview--with-photo {
    grid-template-columns: 1fr;
  }
}
</style>
