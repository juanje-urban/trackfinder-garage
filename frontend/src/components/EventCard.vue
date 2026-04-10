<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import EventAvailabilityBadge from '@/components/EventAvailabilityBadge.vue'
import type { Event } from '@/types/event'
import { getEventAvailabilityState } from '@/utils/eventAvailability'
import { formatCurrency, formatDisplayDate } from '@/utils/format'
import { getTrackMedia } from '@/utils/trackMedia'
import { createVisualStyle, eventVisualPalettes } from '@/utils/visualPalettes'

const props = defineProps<{
  event: Event
}>()

const mediaStyle = computed(() => {
  const trackMedia = getTrackMedia(props.event.trackName)

  return createVisualStyle(
    props.event.id,
    eventVisualPalettes,
    '--event-start',
    '--event-end',
    trackMedia.coverImage
      ? {
          '--event-photo-image': `url("${trackMedia.coverImage}")`,
        }
      : {
          '--event-photo-image': 'linear-gradient(135deg, var(--event-start), var(--event-end))',
        },
  )
})

const formattedDate = computed(() => formatDisplayDate(props.event.eventDate))
const formattedPrice = computed(() => formatCurrency(props.event.basePrice))

const availabilityState = computed(() =>
  getEventAvailabilityState(props.event.remainingCapacity),
)

const capacityFill = computed(() => {
  const booked = props.event.maxParticipants - props.event.remainingCapacity
  return (booked / props.event.maxParticipants) * 100
})

const remainingText = computed(() => {
  return `${props.event.remainingCapacity} plazas disponibles`
})
</script>

<template>
  <article class="event-card media-card panel" :class="`event-card--${availabilityState}`">
    <div class="event-card__media" :style="mediaStyle">
      <div class="media-card__badges">
        <EventAvailabilityBadge :remaining-capacity="event.remainingCapacity" />
      </div>
    </div>

    <div class="media-card__body event-card__body">
      <div class="media-card__headline">
        <div>
          <p class="event-card__date">{{ formattedDate }}</p>
          <h3 class="ui-title-card event-card__title">{{ event.trackName }}</h3>
        </div>

        <div class="event-card__price">
          <span class="ui-stat-label event-card__price-label">desde</span>
          <strong class="ui-title-inline-price">{{ formattedPrice }}</strong>
        </div>
      </div>

      <p class="event-card__host ui-copy-body">{{ event.organizerLegalName }}</p>

      <div class="media-card__meta event-card__meta">
        <span>Aforo {{ event.maxParticipants }}</span>
        <span class="event-card__remaining">{{ remainingText }}</span>
      </div>

      <div class="event-card__progress">
        <span :style="{ width: `${capacityFill}%` }"></span>
      </div>

      <div class="media-card__footer">
        <RouterLink class="action-button" :to="`/events/${event.id}`">
          Ver evento
        </RouterLink>
      </div>
    </div>
  </article>
</template>

<style scoped>
.event-card {
  --event-state-surface: rgba(17, 92, 55, 0.86);
  --event-state-border: rgba(122, 246, 184, 0.58);
  --event-state-text: #d6ffe6;
  --event-state-accent: #5adf97;
  --event-state-glow: rgba(58, 215, 134, 0.24);
  display: grid;
  grid-template-rows: 210px 1fr;
  min-height: 500px;
  position: relative;
  overflow: hidden;
  border: 1px solid var(--event-state-border);
  box-shadow:
    var(--shadow-panel),
    0 0 0 1px var(--event-state-glow);
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.event-card::after {
  content: "";
  position: absolute;
  inset: auto 0 0 0;
  height: 3px;
  background: linear-gradient(90deg, transparent 0%, var(--event-state-accent) 28%, transparent 100%);
  opacity: 0.96;
}

.event-card--urgent {
  --event-state-surface: rgba(122, 22, 22, 0.88);
  --event-state-border: rgba(255, 128, 118, 0.6);
  --event-state-text: #fff0ed;
  --event-state-accent: #ff7165;
  --event-state-glow: rgba(255, 45, 32, 0.3);
}

.event-card--limited {
  --event-state-surface: rgba(104, 67, 14, 0.88);
  --event-state-border: rgba(255, 205, 92, 0.56);
  --event-state-text: #ffe5a5;
  --event-state-accent: #ffbf3c;
  --event-state-glow: rgba(255, 191, 60, 0.24);
}

.event-card__media {
  min-height: 210px;
  padding: var(--space-lg);
  display: flex;
  align-items: start;
  justify-content: space-between;
  background:
    linear-gradient(180deg, var(--surface-glass-strong), var(--media-overlay-bottom)),
    var(--event-photo-image);
  position: relative;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.event-card__body {
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.event-card__media::before {
  content: "";
  position: absolute;
  inset: auto 0 0 0;
  height: 88px;
  background:
    linear-gradient(180deg, transparent, var(--media-fade)),
    repeating-linear-gradient(
      -14deg,
      var(--line-soft) 0,
      var(--line-soft) 2px,
      transparent 2px,
      transparent 24px
    );
  mix-blend-mode: screen;
}

.event-card__date {
  margin: 0 0 6px;
  color: var(--text-muted);
  font-size: var(--fs-meta);
}

.event-card__price {
  text-align: right;
}

.event-card__price-label {
  display: block;
}

.event-card__title {
  display: -webkit-box;
  min-height: 3.24em;
  overflow: hidden;
  line-height: 1.08;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.event-card__meta {
  color: var(--text-muted);
  font-size: var(--fs-meta);
}

.event-card__host {
  display: -webkit-box;
  min-height: 3.4em;
  overflow: hidden;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.event-card__remaining {
  color: var(--event-state-text);
  font-weight: 700;
  text-shadow: 0 0 14px var(--event-state-glow);
}

.event-card__progress {
  width: 100%;
  height: 10px;
  overflow: hidden;
  border-radius: var(--radius-pill);
  border: 1px solid var(--event-state-border);
  background: var(--event-state-surface);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.06);
}

.event-card__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--event-state-accent) 0%, var(--event-state-text) 100%);
  box-shadow: 0 0 18px var(--event-state-glow);
}

.event-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  margin-top: auto;
}

@media (max-width: 560px) {
  .event-card__price {
    text-align: left;
  }
}
</style>
