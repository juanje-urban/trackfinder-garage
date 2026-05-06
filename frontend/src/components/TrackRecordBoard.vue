<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import RankingPositionBadge from '@/components/RankingPositionBadge.vue'
import UserProfileLink from '@/components/UserProfileLink.vue'
import { getTrackRanking } from '@/services/trackService'
import type { TrackRecord } from '@/types/trackRecord'
import { formatDisplayDate, formatLapTime } from '@/utils/format'

const props = withDefaults(
  defineProps<{
    trackId: number
    trackName: string
    limit?: number
    eyebrow?: string
  }>(),
  {
    limit: 3,
    eyebrow: '',
  },
)

const loading = ref(true)
const error = ref('')
const ranking = ref<TrackRecord[]>([])

// 'bestLap' se recalcula cuando llega un ranking nuevo.
const bestLap = computed(() => ranking.value[0] ?? null)

async function loadRanking() {
  // Este componente se carga solo. El padre solo le dice qué circuito y límite usar.
  loading.value = true
  error.value = ''

  try {
    ranking.value = await getTrackRanking(props.trackId, props.limit)
  } catch {
    ranking.value = []
    error.value = 'No se pudieron cargar los records de vuelta.'
  } finally {
    loading.value = false
  }
}
onMounted(() => {
  // Primera carga al aparecer el marcador.
  void loadRanking()
})

watch(
  () => [props.trackId, props.limit] as const,
  () => {
    // Si cambia el circuito o el límite, vuelvo a pedir el ranking.
    void loadRanking()
  },
)
</script>

<template>
  <article class="track-record-board panel">
    <div class="track-record-board__rail"></div>

    <div class="track-record-board__header">
      <div class="panel-stack-sm">
        <p v-if="eyebrow" class="ui-eyebrow">{{ eyebrow }}</p>
        <h3 class="ui-title-card">{{ trackName }}</h3>
      </div>

      <div v-if="bestLap" class="track-record-board__best">
        <p class="ui-stat-label">Best lap</p>
        <p class="track-record-board__best-time">{{ formatLapTime(bestLap.lapTimeMs) }}</p>
        <UserProfileLink
          :display-name="bestLap.userDisplayName"
          class="track-record-board__best-driver"
        >
          {{ bestLap.userDisplayName }}
        </UserProfileLink>
      </div>
    </div>

    <p v-if="loading" class="track-record-board__status">
      Cargando records de vuelta...
    </p>
    <p v-else-if="error" class="track-record-board__status track-record-board__status--error">
      {{ error }}
    </p>
    <p v-else-if="ranking.length === 0" class="track-record-board__status">
      Todavía no hay tiempos publicados para este circuito.
    </p>

    <div v-else class="track-record-board__list">
      <div
        v-for="(record, index) in ranking"
        :key="`${record.trackId}-${record.userDisplayName}-${record.lapTimeMs}-${index}`"
        class="track-record-board__row"
      >
        <RankingPositionBadge :position="index + 1" />

        <div class="track-record-board__driver">
          <UserProfileLink :display-name="record.userDisplayName" class="track-record-board__driver-link">
            <strong>{{ record.userDisplayName }}</strong>
          </UserProfileLink>
          <span>{{ record.vehicle }}</span>
        </div>

        <div class="track-record-board__time">
          <strong>{{ formatLapTime(record.lapTimeMs) }}</strong>
          <span>{{ formatDisplayDate(record.lapDate) }}</span>
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped>
.track-record-board {
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(180deg, var(--surface-glass-highlight), transparent 24%),
    var(--timing-surface);
}

.track-record-board__rail {
  height: 6px;
  background:
    linear-gradient(
      90deg,
      var(--accent) 0 34%,
      var(--racing-amber) 34% 67%,
      var(--racing-blue) 67% 100%
    );
}

.track-record-board__header {
  padding: var(--space-2xl);
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--space-xl);
  align-items: end;
  border-bottom: 1px solid var(--timing-grid-line);
}

.track-record-board__best {
  min-width: 180px;
  padding: var(--space-lg);
  border: 1px solid var(--success-border);
  border-radius: var(--radius-inner);
  background:
    linear-gradient(180deg, var(--success-surface), transparent),
    var(--surface-glass);
  display: grid;
  gap: var(--space-2xs);
}

.track-record-board__best-time {
  margin: 0;
  color: var(--success-text);
  font-family: var(--font-heading);
  font-size: clamp(1.8rem, 4vw, 2.4rem);
  line-height: 1;
}

.track-record-board__best-driver {
  margin: 0;
  color: var(--text-on-media-soft);
  font-size: var(--fs-meta);
}

.track-record-board__status {
  margin: 0;
  padding: var(--space-xl) var(--space-2xl);
  color: var(--text-muted);
}

.track-record-board__status--error {
  color: var(--danger-text);
}

.track-record-board__list {
  display: grid;
}

.track-record-board__row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: var(--space-lg);
  align-items: center;
  padding: var(--space-lg) var(--space-2xl);
  border-bottom: 1px solid var(--timing-grid-line);
}

.track-record-board__row:last-child {
  border-bottom: 0;
}

.track-record-board__driver,
.track-record-board__time {
  display: grid;
  gap: var(--space-3xs);
}

.track-record-board__driver strong,
.track-record-board__time strong {
  color: var(--text-strong);
}

.track-record-board__driver-link {
  width: fit-content;
}

.track-record-board__driver span,
.track-record-board__time span {
  color: var(--text-muted);
  font-size: var(--fs-caption);
}

.track-record-board__time {
  justify-items: end;
}

@media (max-width: 780px) {
  .track-record-board__header {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .track-record-board__row {
    grid-template-columns: auto 1fr;
  }

  .track-record-board__time {
    grid-column: 2;
    justify-items: start;
  }
}
</style>
