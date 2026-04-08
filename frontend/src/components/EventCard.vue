<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { Event } from '@/types/event'
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

const availabilityLabel = computed(() => {
  const remaining = props.event.remainingCapacity

  if (remaining <= 3) {
    return 'Ultimas plazas'
  }
  if (remaining <= 10) {
    return 'Plazas limitadas'
  }

  return 'Reservas abiertas'
})

const capacityFill = computed(() => {
  const booked = props.event.maxParticipants - props.event.remainingCapacity
  return (booked / props.event.maxParticipants) * 100
})

const remainingText = computed(() => {
  return `${props.event.remainingCapacity} plazas disponibles`
})
</script>

<template>
  <article class="event-card media-card panel">
    <div class="event-card__media" :style="mediaStyle">
      <div class="media-card__badges">
        <span class="badge badge--accent">{{ availabilityLabel }}</span>
      </div>
    </div>

    <div class="media-card__body">
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

      <p class="event-card__host ui-copy-body">Organiza {{ event.organizerLegalName }}</p>

      <div class="media-card__meta event-card__meta">
        <span>Aforo {{ event.maxParticipants }}</span>
        <span>{{ remainingText }}</span>
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

.event-card__meta {
  color: var(--text-muted);
  font-size: var(--fs-meta);
}

.event-card__progress {
  width: 100%;
  height: 8px;
  overflow: hidden;
  border-radius: var(--radius-pill);
  background: var(--info-surface);
}

.event-card__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--accent-gradient-horizontal);
}

.event-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
}

@media (max-width: 560px) {
  .event-card__price {
    text-align: left;
  }
}
</style>
