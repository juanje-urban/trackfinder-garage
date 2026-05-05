<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import CatalogPage from '@/components/CatalogPage.vue'
import EventSearchToolbar from '@/components/EventSearchToolbar.vue'
import EventCard from '@/components/EventCard.vue'
import { getFutureEvents } from '@/services/eventService'
import type { Event } from '@/types/event'
import { getEventRemainingLabel } from '@/utils/eventAvailability'
import { formatCurrency, formatDisplayDate } from '@/utils/format'

const route = useRoute()

// Filtros ligados a inputs. Los guardo como string porque así trabajan los formularios HTML.
const events = ref<Event[]>([])
const loading = ref(true)
const error = ref('')
const selectedStartDate = ref('')
const selectedEndDate = ref('')
const selectedTrackId = ref('')
const maxBasePrice = ref('')

const filteredEvents = computed(() => {
  // Derivo la lista visible desde 'events' + filtros; así nunca mantengo dos listas sincronizadas a mano.
  const selectedTrackValue = selectedTrackId.value.trim()
  const maxPriceValue = maxBasePrice.value.trim()
  const parsedMaxPrice = maxPriceValue ? Number(maxPriceValue) : null

  return events.value.filter((event) => {
    if (selectedStartDate.value && event.eventDate < selectedStartDate.value) {
      return false
    }

    if (selectedEndDate.value && event.eventDate > selectedEndDate.value) {
      return false
    }

    if (selectedTrackValue && String(event.trackId) !== selectedTrackValue) {
      return false
    }

    if (parsedMaxPrice !== null && !Number.isNaN(parsedMaxPrice) && event.basePrice > parsedMaxPrice) {
      return false
    }

    return true
  })
})

const firstCatalogEvent = computed(() => events.value[0])

const trackOptions = computed(() => {
  // Construyo las opciones del selector a partir de los eventos disponibles.
  const uniqueTracks = new Map<number, string>()

  events.value.forEach((event) => {
    if (!uniqueTracks.has(event.trackId)) {
      uniqueTracks.set(event.trackId, event.trackName)
    }
  })

  return Array.from(uniqueTracks.entries())
    .map(([id, name]) => ({ id, name }))
    .sort((left, right) => left.name.localeCompare(right.name, 'es'))
})

const remainingCapacity = computed(() =>
  events.value.reduce((sum, event) => sum + event.remainingCapacity, 0),
)

const emptyMessage = computed(() =>
  events.value.length === 0
    ? 'No hay eventos públicos futuros disponibles en este momento.'
    : 'No hay eventos que coincidan con los filtros actuales.',
)

onMounted(async () => {
  // Si llego desde una tarjeta de circuito, la URL puede traer '?trackId=...'.
  applyTrackFilterFromRoute(route.query.trackId)

  try {
    events.value = await getFutureEvents()
  } catch {
    error.value = 'No se pudieron cargar los proximos eventos.'
  } finally {
    loading.value = false
  }
})

watch(
  () => route.query.trackId,
  (trackId) => {
    // 'watch' escucha cambios posteriores en la URL sin recrear toda la vista.
    applyTrackFilterFromRoute(trackId)
  },
)

function applyTrackFilterFromRoute(trackId: unknown) {
  selectedTrackId.value = typeof trackId === 'string' ? trackId : ''
}

function resetFilters() {
  selectedStartDate.value = ''
  selectedEndDate.value = ''
  selectedTrackId.value = ''
  maxBasePrice.value = ''
}

function formatAvailabilitySummary(remainingCapacity: number): string {
  if (remainingCapacity <= 0) {
    return 'Aforo completo'
  }

  return getEventRemainingLabel(remainingCapacity).replace(' para este evento', '').replace('disponibles', 'libres')
}
</script>

<template>
  <CatalogPage
    hero-eyebrow="Agenda"
    hero-title="Eventos programados"
    hero-description="Encuentra los track days que se ajusten a tus necesidades."
    info-label="Plazas disponibles"
    :info-caption="`Plazas libres en ${events.length} eventos`"
    section-eyebrow="Eventos disponibles"
    :loading="loading"
    loading-message="Cargando proximos eventos..."
    :error="error"
    :empty="filteredEvents.length === 0"
    :empty-message="emptyMessage"
  >
    <template #hero-value>
      <strong>{{ remainingCapacity }}</strong>
    </template>

    <template #summary>
      <section v-if="firstCatalogEvent" class="next-event-summary panel panel-pad-lg">
        <div class="next-event-summary__intro">
          <div class="next-event-summary__status">
            <span class="next-event-summary__pulse" aria-hidden="true"></span>
            <span>Proximo evento</span>
          </div>
          <h2 class="ui-title-section">{{ firstCatalogEvent.trackName }}</h2>
          <p class="ui-copy-muted">{{ firstCatalogEvent.organizerLegalName }}</p>
        </div>

        <div class="next-event-summary__stats">
          <article class="next-event-summary__stat next-event-summary__stat--accent">
            <span class="ui-stat-label">Fecha</span>
            <strong class="ui-title-info">{{ formatDisplayDate(firstCatalogEvent.eventDate) }}</strong>
          </article>
          <article class="next-event-summary__stat next-event-summary__stat--amber">
            <span class="ui-stat-label">Precio</span>
            <strong class="ui-title-info">{{ formatCurrency(firstCatalogEvent.basePrice) }}</strong>
          </article>
          <article class="next-event-summary__stat next-event-summary__stat--success">
            <span class="ui-stat-label">Disponibilidad</span>
            <strong class="ui-title-info next-event-summary__availability">
              {{ formatAvailabilitySummary(firstCatalogEvent.remainingCapacity) }}
            </strong>
          </article>
        </div>
      </section>
    </template>

    <template #toolbar>
      <EventSearchToolbar
        :selected-start-date="selectedStartDate"
        :selected-end-date="selectedEndDate"
        :selected-track-id="selectedTrackId"
        :max-base-price="maxBasePrice"
        :track-options="trackOptions"
        :result-count="filteredEvents.length"
        :total-count="events.length"
        @update:selected-start-date="selectedStartDate = $event"
        @update:selected-end-date="selectedEndDate = $event"
        @update:selected-track-id="selectedTrackId = $event"
        @update:max-base-price="maxBasePrice = $event"
        @reset="resetFilters"
      />
    </template>

    <div class="cards-grid cards-grid--events">
      <EventCard v-for="event in filteredEvents" :key="event.id" :event="event" />
    </div>
  </CatalogPage>
</template>

<style scoped>
.next-event-summary {
  position: relative;
  overflow: hidden;
  display: grid;
  gap: var(--space-xl);
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1.85fr);
  align-items: start;
  border-color: var(--line-strong);
  background:
    radial-gradient(circle at top right, rgba(255, 191, 60, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(44, 17, 17, 0.98) 0%, rgba(23, 9, 9, 0.98) 100%);
}

.next-event-summary::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 4px;
  background:
    linear-gradient(90deg, var(--accent-strong) 0%, var(--racing-amber) 50%, var(--success-text) 100%);
}

.next-event-summary::after {
  content: "";
  position: absolute;
  inset: auto -40px -60px auto;
  width: 200px;
  height: 200px;
  border-radius: 999px;
  background: rgba(255, 45, 32, 0.18);
  filter: blur(48px);
  pointer-events: none;
}

.next-event-summary__intro {
  position: relative;
  z-index: 1;
  display: grid;
  gap: var(--space-xs);
}

.next-event-summary__status {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
  width: fit-content;
  padding: var(--space-2xs) var(--space-md);
  border: 1px solid var(--line-strong);
  border-radius: var(--radius-pill);
  color: var(--text-strong);
  font-size: var(--fs-badge);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: var(--racing-red-soft);
}

.next-event-summary__pulse {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--accent-strong);
  box-shadow: 0 0 0 6px rgba(255, 74, 61, 0.18);
}

.next-event-summary__stats {
  position: relative;
  z-index: 1;
  display: grid;
  gap: var(--space-md);
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.next-event-summary__stat {
  display: grid;
  gap: var(--space-2xs);
  padding: var(--space-lg);
  border: 1px solid var(--line-faint);
  border-radius: var(--radius-sm);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.next-event-summary__stat--accent {
  border-color: var(--line-strong);
  background:
    linear-gradient(180deg, rgba(255, 45, 32, 0.16) 0%, rgba(255, 45, 32, 0.06) 100%);
}

.next-event-summary__stat--amber {
  border-color: rgba(255, 191, 60, 0.22);
  background:
    linear-gradient(180deg, rgba(255, 191, 60, 0.14) 0%, rgba(255, 191, 60, 0.05) 100%);
}

.next-event-summary__stat--success {
  border-color: var(--success-border);
  background:
    linear-gradient(180deg, rgba(58, 215, 134, 0.16) 0%, rgba(58, 215, 134, 0.05) 100%);
}

.next-event-summary__availability {
  color: var(--success-text);
}

@media (max-width: 920px) {
  .next-event-summary {
    grid-template-columns: 1fr;
  }

  .next-event-summary__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .next-event-summary__stats {
    grid-template-columns: 1fr;
  }
}
</style>
