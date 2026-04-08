<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import BookingSidebarCard from '@/components/BookingSidebarCard.vue'
import DetailInfoCard from '@/components/DetailInfoCard.vue'
import SectionCard from '@/components/SectionCard.vue'
import { RouterLink, useRoute } from 'vue-router'
import { getFutureEvents } from '@/services/eventService'
import { getTrackById } from '@/services/trackService'
import type { Event } from '@/types/event'
import type { Track } from '@/types/track'
import { formatCurrency, formatDisplayDate } from '@/utils/format'
import { getTrackMedia } from '@/utils/trackMedia'
import { createVisualStyle, eventVisualPalettes } from '@/utils/visualPalettes'

const route = useRoute()

const event = ref<Event | null>(null)
const track = ref<Track | null>(null)
const loading = ref(true)
const error = ref('')

const eventId = computed(() => Number(route.params.id))

const formattedDate = computed(() =>
  event.value ? formatDisplayDate(event.value.eventDate) : '',
)

const formattedPrice = computed(() =>
  event.value ? formatCurrency(event.value.basePrice) : '',
)

function getHeroStyle(eventId: number) {
  const coverImage = track.value ? getTrackMedia(track.value.name).coverImage : undefined

  return createVisualStyle(
    eventId,
    eventVisualPalettes,
    '--detail-start',
    '--detail-end',
    {
      '--detail-photo-image': coverImage
        ? `url("${coverImage}")`
        : 'linear-gradient(135deg, var(--detail-start), var(--detail-end))',
    },
  )
}

const schedule = computed(() => [
  { label: 'Sign-on and briefing', time: '07:30' },
  { label: 'Sighting laps', time: '09:00' },
  { label: 'Track live morning', time: '09:30' },
  { label: 'Lunch break', time: '12:30' },
  { label: 'Track live afternoon', time: '13:30' },
])

const includedItems = computed(() => [
  'Professional track marshals included',
  'Technical support team on-site',
  'Driver welcome briefing',
  'Paddock access throughout the event day',
])

const optionalExtras = computed(() => [
  { name: 'Additional driver pass', price: '+95 EUR' },
  { name: 'Garage space upgrade', price: '+50 EUR' },
  { name: 'Coaching add-on', price: '+65 EUR' },
])

const includedBookingItems = computed(() => [
  { name: 'Track access management', price: 'Included' },
  { name: 'Event operations and staffing', price: 'Included' },
])

const optionalBookingItems = computed(() => optionalExtras.value)

function getAvailabilityTone(remainingCapacity: number): string {
  if (remainingCapacity <= 3) {
    return 'Last spots available'
  }
  if (remainingCapacity <= 10) {
    return 'Limited availability'
  }

  return 'Booking open'
}

function getRemainingLabel(remainingCapacity: number): string {
  return `${remainingCapacity} spots remaining for this event`
}

onMounted(async () => {
  try {
    const events = await getFutureEvents()
    const selectedEvent = events.find((item) => item.id === eventId.value)

    if (!selectedEvent) {
      throw new Error('Event not found')
    }

    event.value = selectedEvent
    track.value = await getTrackById(selectedEvent.trackId)
  } catch {
    error.value = 'No se pudo cargar la ficha del evento.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="page-shell section-stack">
    <p v-if="loading" class="status-message">Loading event details...</p>
    <p v-else-if="error" class="status-message status-message--error">{{ error }}</p>

    <template v-else-if="event && track">
      <nav class="event-detail__breadcrumbs">
        <RouterLink to="/events">Events</RouterLink>
        <span>/</span>
        <span>{{ event.trackName }}</span>
      </nav>

      <section class="event-detail__hero-layout">
        <article class="event-detail__hero panel">
          <div class="event-detail__hero-media" :style="getHeroStyle(event.id)">
            <div class="media-card__badges">
              <span class="badge badge--accent">{{ getAvailabilityTone(event.remainingCapacity) }}</span>
              <span class="badge badge--soft">Public session</span>
            </div>

            <div class="event-detail__hero-copy">
              <p class="event-detail__hero-date">{{ formattedDate }}</p>
              <h1 class="ui-title-hero">{{ event.trackName }} Track Day</h1>
              <p class="event-detail__hero-location">{{ track.location }}</p>
            </div>
          </div>
        </article>

        <aside class="event-detail__hero-side">
          <DetailInfoCard eyebrow="Date and time" :title="formattedDate" subtitle="08:00 AM - 05:00 PM" />

          <DetailInfoCard
            eyebrow="Hosted by"
            :title="event.organizerLegalName"
            subtitle="Track day organizer"
          />

          <DetailInfoCard eyebrow="Track map" title="Layout preview" subtitle="Concept visualization">
            <img
              v-if="getTrackMedia(track.name).layoutImage"
              :src="getTrackMedia(track.name).layoutImage"
              :alt="`Trazado de ${track.name}`"
              class="track-map-image"
            />
            <div v-else class="track-map-placeholder">
              <span class="track-map-placeholder__line"></span>
            </div>
          </DetailInfoCard>
        </aside>
      </section>

      <section class="event-detail__content">
        <div class="event-detail__main">
          <SectionCard
            eyebrow="Event description"
            title="Built for a premium public catalog"
            description="This public detail page now follows the premium racing layout language from your references."
          >
            <p class="ui-copy-body">
              {{ track.description }}
            </p>
            <p class="ui-copy-body">
              The current frontend uses the real event and circuit data already exposed by the backend, wrapped in a more polished event-detail presentation inspired by your references.
            </p>

            <div class="detail-panel__bullet-grid">
              <p v-for="item in includedItems" :key="item">{{ item }}</p>
            </div>
          </SectionCard>

          <SectionCard
            eyebrow="Daily schedule"
            title="Expected on-track flow"
            description="Illustrative timetable for the public track-day presentation."
          >
            <div class="schedule-list list-divider">
              <div
                v-for="entry in schedule"
                :key="entry.label"
                class="schedule-list__item list-divider__item"
              >
                <span>{{ entry.label }}</span>
                <strong class="ui-title-time">{{ entry.time }}</strong>
              </div>
            </div>
          </SectionCard>

          <SectionCard
            eyebrow="Track dossier"
            title="Venue, organizer and access overview"
            description="A reusable section that can later consume richer public metadata from the backend."
          >
            <div class="dossier-grid info-grid info-grid--two">
              <div class="info-tile">
                <span class="info-tile__label">Venue</span>
                <strong class="info-tile__title">{{ event.trackName }}</strong>
                <p class="info-tile__body">{{ track.location }}</p>
              </div>

              <div class="info-tile">
                <span class="info-tile__label">Organizer</span>
                <strong class="info-tile__title">{{ event.organizerLegalName }}</strong>
                <p class="info-tile__body">Public event organizer listing</p>
              </div>

              <div class="info-tile">
                <span class="info-tile__label">Capacity</span>
                <strong class="info-tile__title">{{ event.maxParticipants }}</strong>
                <p class="info-tile__body">{{ getRemainingLabel(event.remainingCapacity) }}</p>
              </div>

              <div class="info-tile">
                <span class="info-tile__label">Catalog state</span>
                <strong class="info-tile__title">{{ getAvailabilityTone(event.remainingCapacity) }}</strong>
                <p class="info-tile__body">Booking button intentionally protected for now</p>
              </div>
            </div>
          </SectionCard>
        </div>

        <aside class="event-detail__booking">
          <BookingSidebarCard
            :price-label="formattedPrice"
            :included-items="includedBookingItems"
            :optional-items="optionalBookingItems"
            caption="Public catalog open. Booking flow can be connected later once auth and reservations are exposed in the frontend."
          />

          <DetailInfoCard
            eyebrow="Availability"
            :title="getAvailabilityTone(event.remainingCapacity)"
            :subtitle="getRemainingLabel(event.remainingCapacity)"
            tone="success"
          />
        </aside>
      </section>
    </template>
  </main>
</template>

<style scoped>
.event-detail__breadcrumbs {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--text-muted);
  font-size: var(--fs-meta);
}

.event-detail__hero-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.8fr);
  gap: var(--space-2xl);
}

.event-detail__hero {
  overflow: hidden;
}

.event-detail__hero-media {
  min-height: 420px;
  padding: var(--space-xl);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background:
    linear-gradient(180deg, var(--media-overlay-top), var(--media-overlay-bottom-strong)),
    var(--detail-photo-image);
  position: relative;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.event-detail__hero-media::before {
  content: "";
  position: absolute;
  inset: auto 0 0 0;
  height: 120px;
  background:
    linear-gradient(180deg, transparent, var(--media-fade-strong)),
    repeating-linear-gradient(
      -14deg,
      var(--line-soft) 0,
      var(--line-soft) 2px,
      transparent 2px,
      transparent 26px
    );
}

.event-detail__hero-copy {
  position: relative;
  z-index: 1;
}

.event-detail__hero-copy {
  display: grid;
  gap: var(--space-sm);
}

.event-detail__hero-date {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: var(--fs-badge);
  color: var(--text-on-media-soft);
}

.event-detail__hero-location {
  margin: 0;
  color: var(--text-on-media-strong);
  font-size: var(--fs-body-lg);
}

.event-detail__hero-side {
  display: grid;
  gap: var(--space-lg);
}

.track-map-placeholder {
  min-height: 170px;
  border-radius: var(--radius-inner);
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(90deg, var(--map-surface-start), var(--map-surface-end)),
    linear-gradient(180deg, var(--map-paper-start), var(--map-paper-end));
}

.track-map-image {
  width: 100%;
  min-height: 170px;
  border-radius: var(--radius-inner);
  object-fit: cover;
  display: block;
  background: var(--surface-light);
}

.track-map-placeholder::before,
.track-map-placeholder::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent 0, var(--map-grid-line) 32%, transparent 33%, transparent 66%, var(--map-grid-line) 67%, transparent 68%);
}

.track-map-placeholder__line {
  position: absolute;
  inset: 20px 34px;
  border: 5px solid var(--map-track-line);
  border-radius: 42% 48% 36% 52% / 36% 44% 56% 44%;
  transform: rotate(14deg) scale(0.8);
  box-shadow:
    54px -6px 0 -14px var(--map-track-line),
    -42px 30px 0 -18px var(--map-track-line);
}

.event-detail__content {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(300px, 0.82fr);
  gap: var(--space-2xl);
}

.event-detail__main,
.event-detail__booking {
  display: grid;
  gap: var(--space-2xl);
  align-content: start;
}

.detail-panel__bullet-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-lg);
}

.detail-panel__bullet-grid p {
  margin: 0;
  padding-left: 20px;
  position: relative;
}

.detail-panel__bullet-grid p::before {
  content: "";
  position: absolute;
  top: 0.58rem;
  left: 0;
  width: 8px;
  height: 8px;
  border-radius: var(--radius-pill);
  background: var(--accent-strong);
  box-shadow: 0 0 0 5px var(--accent-soft);
}

.schedule-list__item span {
  color: var(--text-body);
}

.schedule-list__item strong {
  color: var(--accent-strong);
}

@media (max-width: 980px) {
  .event-detail__hero-layout,
  .event-detail__content {
    grid-template-columns: 1fr;
  }

  .event-detail__booking {
    position: static;
  }
}

@media (max-width: 680px) {
  .detail-panel__bullet-grid {
    grid-template-columns: 1fr;
  }

  .info-grid--two {
    grid-template-columns: 1fr;
  }

  .event-detail__hero .ui-title-hero {
    max-width: none;
    font-size: 2.7rem;
  }
}
</style>
