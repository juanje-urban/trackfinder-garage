<script setup lang="ts">
import DetailInfoCard from '@/components/DetailInfoCard.vue'
import EventAvailabilityBadge from '@/components/EventAvailabilityBadge.vue'
import type { Event } from '@/types/event'
import type { EventAvailabilitySummary } from '@/types/eventDetail'
import type { Track } from '@/types/track'

// Hero del detalle. Recibe los datos ya preparados desde la vista padre.
defineProps<{
  event: Event
  track: Track
  availability: EventAvailabilitySummary
  formattedDate: string
  heroStyle: Record<string, string>
  layoutImage?: string
}>()

defineEmits<{
  // Abre el mapa ampliado desde EventDetailView.
  openLayout: []
}>()
</script>

<template>
  <section class="event-detail__hero-layout">
    <article class="event-detail__hero panel">
      <div class="event-detail__hero-media" :style="heroStyle">
        <div class="media-card__badges">
          <EventAvailabilityBadge :remaining-capacity="event.remainingCapacity" />
        </div>

        <div class="event-detail__hero-copy">
          <h1 class="ui-title-hero">{{ event.trackName }}</h1>
          <p class="event-detail__hero-location">{{ track.location }}</p>
        </div>
      </div>

      <div class="event-detail__hero-description panel-copy">
        <p class="ui-eyebrow">Evento</p>
        <p class="ui-copy-body">{{ event.description }}</p>
      </div>
    </article>

    <aside class="event-detail__hero-side">
      <DetailInfoCard
        eyebrow="Disponibilidad"
        :title="availability.label"
        :subtitle="availability.remainingLabel"
        :tone="availability.state"
      />
      <DetailInfoCard eyebrow="Fecha" :title="formattedDate" />
      <DetailInfoCard eyebrow="Organiza" :title="event.organizerLegalName" />

      <article
        v-if="layoutImage"
        class="event-detail__track-map panel panel-pad-lg panel-stack-sm"
      >
        <p class="ui-eyebrow">Trazado</p>
        <button
          class="event-detail__track-map-button"
          type="button"
          :aria-label="`Ampliar trazado de ${event.trackName}`"
          @click="$emit('openLayout')"
        >
          <img
            class="event-detail__track-map-image"
            :src="layoutImage"
            :alt="`Trazado de ${event.trackName}`"
          />
        </button>
        <span class="event-detail__track-map-hint">Pulsa para ampliar</span>
      </article>
    </aside>
  </section>
</template>

<style scoped>
.event-detail__hero-layout {
  display: grid;
  gap: var(--space-xl);
  grid-template-columns: minmax(0, 1.85fr) minmax(280px, 0.95fr);
  align-items: start;
}

.event-detail__hero {
  overflow: hidden;
}

.event-detail__hero-media {
  --event-detail-start: #4c1717;
  --event-detail-end: #24365a;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: clamp(20px, 3vw, 32px);
  background-image:
    radial-gradient(circle at top right, rgba(255, 191, 60, 0.18), transparent 32%),
    linear-gradient(140deg, var(--event-detail-start), var(--event-detail-end));
  background-position: center;
  background-size: cover;
}

.event-detail__hero-copy {
  display: grid;
  gap: var(--space-sm);
  max-width: min(640px, 100%);
}

.event-detail__hero-location {
  margin: 0;
  color: rgba(255, 246, 242, 0.92);
  font-size: var(--fs-body);
}

.event-detail__hero-description {
  padding: var(--space-xl);
}

.event-detail__hero-side {
  display: grid;
  gap: var(--space-lg);
}

.event-detail__track-map {
  overflow: hidden;
  border-color: rgba(48, 17, 15, 0.12);
  background: var(--surface-light);
  color: var(--text-on-light);
}

.event-detail__track-map .ui-eyebrow {
  color: var(--accent);
}

.event-detail__track-map-button {
  padding: 0;
  border: 0;
  background: transparent;
  width: 100%;
  cursor: zoom-in;
}

.event-detail__track-map-hint {
  color: rgba(48, 17, 15, 0.62);
  font-size: var(--fs-caption);
}

.event-detail__track-map-image {
  display: block;
  width: 100%;
  max-height: 220px;
  object-fit: contain;
  border-radius: var(--radius-inner);
  background: var(--surface-light);
}

@media (max-width: 980px) {
  .event-detail__hero-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .event-detail__hero-media {
    min-height: 320px;
    padding: var(--space-lg);
  }

  .event-detail__hero-description {
    padding: var(--space-lg);
  }
}
</style>
