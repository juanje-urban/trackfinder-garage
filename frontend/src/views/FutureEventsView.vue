<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CatalogPage from '@/components/CatalogPage.vue'
import EventCard from '@/components/EventCard.vue'
import MetricCard from '@/components/MetricCard.vue'
import { getFutureEvents } from '@/services/eventService'
import type { Event } from '@/types/event'
import { formatDisplayDate } from '@/utils/format'

const events = ref<Event[]>([])
const loading = ref(true)
const error = ref('')
const firstEvent = computed(() => events.value[0] ?? null)

const nextEventLabel = computed(() =>
  firstEvent.value ? formatDisplayDate(firstEvent.value.eventDate) : 'Pending',
)

const totalCapacity = computed(() =>
  events.value.reduce((sum, event) => sum + event.maxParticipants, 0),
)

const remainingCapacity = computed(() =>
  events.value.reduce((sum, event) => sum + event.remainingCapacity, 0),
)

onMounted(async () => {
  try {
    events.value = await getFutureEvents()
  } catch {
    error.value = 'No se pudieron cargar los proximos eventos.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <CatalogPage
    hero-eyebrow="Race-ready lineup"
    hero-title="Public Track Days"
    hero-description="Book your next adrenaline rush through the public event catalog. This first iteration focuses on a premium racing look while keeping the current data flow intact."
    info-label="Live availability"
    :info-caption="`Public spots across ${events.length || 0} future events`"
    :toolbar-chips="[
      { label: 'All circuits', accent: true },
      { label: 'Future only' },
      { label: 'Public catalog' },
    ]"
    :toolbar-note="`Showing ${events.length} upcoming events`"
    section-eyebrow="Available track days"
    section-title="Ready for the next session"
    section-hint="A darker, high-contrast marketplace inspired by your references."
    :loading="loading"
    loading-message="Loading future events..."
    :error="error"
    :empty="events.length === 0"
    empty-message="No future public events are available right now."
  >
    <template #hero-value>
      <strong>{{ remainingCapacity }}</strong>
      <span>Catalog capacity still open for booking</span>
    </template>

    <template #metrics>
      <MetricCard
        label="Upcoming events"
        :value="String(events.length)"
        hint="Events published from today onward"
        tone="accent"
      />
      <MetricCard
        label="Next date"
        :value="nextEventLabel"
        hint="Chronological public feed"
      />
      <MetricCard
        label="Open spots"
        :value="String(remainingCapacity)"
        hint="Live remaining capacity"
        tone="success"
      />
      <MetricCard
        label="Total capacity"
        :value="String(totalCapacity)"
        hint="Combined public session volume"
      />
    </template>

    <div class="cards-grid cards-grid--events">
      <EventCard v-for="event in events" :key="event.id" :event="event" />
    </div>
  </CatalogPage>
</template>
