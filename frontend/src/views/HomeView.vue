<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import ContentSection from '@/components/ContentSection.vue'
import EventCard from '@/components/EventCard.vue'
import HeroInfoPanel from '@/components/HeroInfoPanel.vue'
import MetricCard from '@/components/MetricCard.vue'
import PageHero from '@/components/PageHero.vue'
import TrackRecordBoard from '@/components/TrackRecordBoard.vue'
import { getFutureEvents } from '@/services/eventService'
import { getTracks } from '@/services/trackService'
import type { Event } from '@/types/event'
import type { Track } from '@/types/track'
import { formatCurrency, formatDisplayDate } from '@/utils/format'
import heroImage from '@/assets/home/hero_page_jarama.jpg'

const events = ref<Event[]>([])
const tracks = ref<Track[]>([])
const loading = ref(true)
const eventsError = ref('')

const sortedEvents = computed(() =>
  [...events.value].sort((left, right) => left.eventDate.localeCompare(right.eventDate)),
)

const featuredEvent = computed(() => sortedEvents.value[0])
const homeEvents = computed(() => sortedEvents.value.slice(0, 3))
const recordTrackEvents = computed(() => selectUpcomingUniqueTrackEvents(sortedEvents.value, 3))

const openSpots = computed(() =>
  sortedEvents.value.reduce((sum, event) => sum + event.remainingCapacity, 0),
)

const totalLocations = computed(() => new Set(tracks.value.map((track) => track.location)).size)

function selectUpcomingUniqueTrackEvents(sourceEvents: Event[], limit: number): Event[] {
  const seenTrackIds = new Set<number>()

  return sourceEvents
    .filter((event) => {
      if (seenTrackIds.has(event.trackId)) {
        return false
      }

      seenTrackIds.add(event.trackId)
      return true
    })
    .slice(0, limit)
}

onMounted(async () => {
  const [eventsResult, tracksResult] = await Promise.allSettled([
    getFutureEvents(),
    getTracks(),
  ])

  if (eventsResult.status === 'fulfilled') {
    events.value = eventsResult.value
  } else {
    eventsError.value = 'No se pudieron cargar los eventos destacados.'
  }

  if (tracksResult.status === 'fulfilled') {
    tracks.value = tracksResult.value
  }

  loading.value = false
})
</script>

<template>
  <main class="page-shell section-stack">
    <PageHero
      eyebrow="Próximo evento"
      title="Jarama a Fondo"
      description="Prepárate para una jornada brutal de tandas libres en el Circuito del Jarama. Saca todo el potencial de tu coche, rueda al límite en un entorno seguro y vive el auténtico ambiente racing con plazas limitadas."
      :image-url="heroImage"
      image-alt="Circuito del Jarama"
    >
      <template v-if="featuredEvent" #aside>
        <HeroInfoPanel
          label="Semáforo en verde"
          :caption="`${formatDisplayDate(featuredEvent.eventDate)} - ${formatCurrency(featuredEvent.basePrice)} - ${featuredEvent.organizerLegalName}`"
        >
          <strong>{{ featuredEvent.trackName }}</strong>
          <span>{{ featuredEvent.remainingCapacity }} plazas disponibles</span>
        </HeroInfoPanel>
      </template>
    </PageHero>

    <section class="metrics-grid">
      <MetricCard
        label="Sesiones programadas"
        :value="String(sortedEvents.length)"
        hint="Eventos en la agenda"
        tone="accent"
      />
      <MetricCard
        label="Plazas disponibles"
        :value="String(openSpots)"
        hint="Aforo en tiempo real"
        tone="success"
      />
      <MetricCard
        label="Circuitos"
        :value="String(tracks.length)"
        hint="Pistas para nuestros eventos"
      />
      <MetricCard
        label="Localizaciones"
        :value="String(totalLocations)"
        hint="Ciudades europeas diferentes"
      />
    </section>

    <section class="panel launch-strip">
      <div class="panel-copy">
        <p class="section-heading__eyebrow">Gas a fondo</p>
        <h2 class="ui-title-section">Encuentra tu siguiente trackday</h2>
        <p class="launch-strip__hint ui-copy-muted">
          Descubre eventos en los mejores circuitos, compara fechas y servicios, y prepárate para
          vivir una jornada de motor pensada para disfrutar al máximo dentro y fuera de pista.
        </p>
      </div>

      <div class="action-row">
        <RouterLink class="action-button" to="/events">Buscar eventos</RouterLink>
        <RouterLink class="action-button action-button--ghost" to="/tracks">
          Explorar circuitos
        </RouterLink>
      </div>
    </section>

    <section class="panel panel-pad-lg panel-stack-lg">
      <div class="home-records__header">
        <h2 class="ui-title-section">Récords de vuelta</h2>
      </div>

      <p v-if="loading" class="status-message">Cargando récords de vuelta...</p>
      <p v-else-if="recordTrackEvents.length === 0" class="status-message">
        Todavía no hay récords de vuelta publicados para la home.
      </p>

      <div v-else class="records-grid">
        <TrackRecordBoard
          v-for="event in recordTrackEvents"
          :key="event.trackId"
          :track-id="event.trackId"
          :track-name="event.trackName"
        />
      </div>
    </section>

    <ContentSection
      eyebrow="Agenda"
      title="Próximos eventos programados"
      hint="Echa un vistazo a los track days que se celebrarán pronto."
      :loading="loading"
      loading-message="Loading home previews..."
      :error="homeEvents.length === 0 ? eventsError : ''"
      :empty="homeEvents.length === 0"
      empty-message="Sin eventos programados."
    >
      <template #action>
        <RouterLink class="action-button action-button--ghost" to="/events">
          Ver todos los eventos
        </RouterLink>
      </template>

      <div class="cards-grid cards-grid--events">
        <EventCard v-for="event in homeEvents" :key="event.id" :event="event" />
      </div>
    </ContentSection>
  </main>
</template>

<style scoped>
.launch-strip {
  padding: var(--space-2xl) var(--space-3xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2xl);
}

.launch-strip__hint {
  max-width: 62ch;
}

.records-grid {
  display: grid;
  gap: var(--space-xl);
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
}

.home-records__header {
  padding-bottom: var(--space-sm);
  border-bottom: 1px solid var(--line-faint);
}

@media (max-width: 980px) {
  .launch-strip {
    flex-direction: column;
    align-items: start;
  }
}

@media (max-width: 640px) {
  .launch-strip {
    padding: var(--space-xl);
  }
}
</style>
