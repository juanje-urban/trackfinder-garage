<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CatalogPage from '@/components/CatalogPage.vue'
import MetricCard from '@/components/MetricCard.vue'
import TrackCard from '@/components/TrackCard.vue'
import { getTracks } from '@/services/trackService'
import type { Track } from '@/types/track'

const tracks = ref<Track[]>([])
const loading = ref(true)
const error = ref('')

const locationCount = computed(
  () => new Set(tracks.value.map((track) => track.location)).size,
)

const featuredTrack = computed(() => tracks.value[0]?.name ?? 'Catalog pending')

onMounted(async () => {
  try {
    tracks.value = await getTracks()
  } catch {
    error.value = 'No se pudieron cargar los circuitos.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <CatalogPage
    hero-eyebrow="Circuit registry"
    hero-title="Tracks with a paddock-grade look"
    hero-description="The circuit catalog now shares the same dark premium language as the public events feed, ready to grow into richer detail and booking flows."
    info-label="Featured circuit"
    info-caption="Public racing venues currently exposed by the backend"
    :toolbar-chips="[
      { label: 'Track catalog', accent: true },
      { label: 'Public circuits' },
      { label: 'Linked to events feed' },
    ]"
    :toolbar-note="`Exploring ${tracks.length} visible circuits`"
    section-eyebrow="Track management look"
    section-title="Circuit cards with shared styling"
    section-hint="Reusable surfaces, spacing and typography now match the event listing."
    :loading="loading"
    loading-message="Loading circuits..."
    :error="error"
    :empty="tracks.length === 0"
    empty-message="No public circuits are available right now."
  >
    <template #hero-value>
      <strong>{{ featuredTrack }}</strong>
      <span>Circuit catalog powered by the public API</span>
    </template>

    <template #metrics>
      <MetricCard
        label="Total tracks"
        :value="String(tracks.length)"
        hint="Publicly visible circuits"
        tone="accent"
      />
      <MetricCard
        label="Locations"
        :value="String(locationCount)"
        hint="Cities or regions represented"
      />
      <MetricCard
        label="Live connection"
        value="Online"
        hint="Catalog is being served by the backend"
        tone="success"
      />
    </template>

    <div class="cards-grid cards-grid--tracks">
      <TrackCard v-for="track in tracks" :key="track.id" :track="track" />
    </div>
  </CatalogPage>
</template>
