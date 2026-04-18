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
    hero-eyebrow="Circuitos"
    hero-title="Nuestras pistas"
    hero-description="Explora el catálogo de circuitos donde podrás encontrar nuestros eventos."
    section-eyebrow="Catálogo"
    :loading="loading"
    loading-message="Cargando circuitos..."
    :error="error"
    :empty="tracks.length === 0"
    empty-message="No hay circuitos públicos disponibles en este momento."
  >

    <template #metrics>
      <MetricCard
        label="Circuitos"
        :value="String(tracks.length)"
        hint="Circuitos locales y de clase mundial"
        tone="accent"
      />
      <MetricCard
        label="Localizaciones"
        :value="String(locationCount)"
        hint="Ciudades representadas"
      />
    </template>

    <div class="cards-grid cards-grid--tracks">
      <TrackCard v-for="track in tracks" :key="track.id" :track="track" />
    </div>
  </CatalogPage>
</template>
