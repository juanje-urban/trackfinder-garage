<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import RankingPositionBadge from '@/components/RankingPositionBadge.vue'
import UserProfileHero from '@/components/UserProfileHero.vue'
import { useAuth } from '@/composables/useAuth'
import { getEventBookingsByUserId } from '@/services/eventBookingService'
import { getLapTimesByUserId } from '@/services/lapTimeService'
import { getTrackRanking } from '@/services/trackService'
import { getPublicUserProfile } from '@/services/userService'
import type { EventBooking } from '@/types/eventBooking'
import type { LapTime } from '@/types/lapTime'
import type { TrackRecord } from '@/types/trackRecord'
import type { PublicUserProfile } from '@/types/user'
import { toIsoDate } from '@/utils/date'
import { formatDisplayDate, formatLapTime } from '@/utils/format'

type PublicLapTimeRow = LapTime & {
  position: number | null
}

type PublicLapTimeGroup = {
  trackId: number
  trackName: string
  laps: PublicLapTimeRow[]
}

const FULL_TRACK_RANKING_LIMIT = 100

const route = useRoute()
const router = useRouter()
const auth = useAuth()

const loading = ref(true)
const error = ref('')
const publicProfile = ref<PublicUserProfile | null>(null)
const publicBookings = ref<EventBooking[]>([])
const publicLapTimes = ref<LapTime[]>([])
const trackRankings = ref<Record<number, TrackRecord[]>>({})

const displayName = computed(() =>
  typeof route.params.displayName === 'string' ? route.params.displayName : '',
)

const todayIso = computed(() => {
  return toIsoDate(new Date())
})

const orderedBookings = computed(() =>
  [...publicBookings.value].sort((left, right) => {
    const leftIsFuture = left.eventDate >= todayIso.value
    const rightIsFuture = right.eventDate >= todayIso.value

    if (leftIsFuture && rightIsFuture) {
      return left.eventDate.localeCompare(right.eventDate)
    }

    if (leftIsFuture !== rightIsFuture) {
      return leftIsFuture ? -1 : 1
    }

    return right.eventDate.localeCompare(left.eventDate)
  }),
)

const groupedLapTimes = computed<PublicLapTimeGroup[]>(() => {
  const rows = [...publicLapTimes.value]
    .map((lapTime) => ({
      ...lapTime,
      position: resolveLapPosition(lapTime),
    }))
    .sort((left, right) => {
      const byTrack = left.trackName.localeCompare(right.trackName, 'es')
      if (byTrack !== 0) {
        return byTrack
      }

      const byLapTime = left.lapTimeMs - right.lapTimeMs
      if (byLapTime !== 0) {
        return byLapTime
      }

      return right.lapDate.localeCompare(left.lapDate)
    })

  const groups = new Map<number, PublicLapTimeGroup>()

  rows.forEach((lapTime) => {
    const existingGroup = groups.get(lapTime.trackId)

    if (existingGroup) {
      existingGroup.laps.push(lapTime)
      return
    }

    groups.set(lapTime.trackId, {
      trackId: lapTime.trackId,
      trackName: lapTime.trackName,
      laps: [lapTime],
    })
  })

  return [...groups.values()]
})

onMounted(() => {
  void loadPublicProfilePage()
})

watch(
  () => displayName.value,
  () => {
    void loadPublicProfilePage()
  },
)

async function loadPublicProfilePage() {
  const nextDisplayName = displayName.value.trim()

  if (!nextDisplayName) {
    error.value = 'No se pudo identificar el perfil solicitado.'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''

  try {
    const publicProfileResult = await getPublicUserProfile(nextDisplayName)
    publicProfile.value = publicProfileResult

    const [bookingsResult, lapTimesResult] = await Promise.all([
      getEventBookingsByUserId(publicProfileResult.id),
      getLapTimesByUserId(publicProfileResult.id),
    ])

    publicBookings.value = bookingsResult
    publicLapTimes.value = lapTimesResult
    await refreshTrackRankings(lapTimesResult)
  } catch {
    publicProfile.value = null
    publicBookings.value = []
    publicLapTimes.value = []
    trackRankings.value = {}
    error.value = 'No se pudo cargar el perfil publico solicitado.'
  } finally {
    loading.value = false
  }
}

async function refreshTrackRankings(sourceLapTimes: LapTime[]) {
  const uniqueTrackIds = [...new Set(sourceLapTimes.map((lapTime) => lapTime.trackId))]

  if (uniqueTrackIds.length === 0) {
    trackRankings.value = {}
    return
  }

  const rankingResults = await Promise.allSettled(
    uniqueTrackIds.map(async (trackId) => [
      trackId,
      await getTrackRanking(trackId, FULL_TRACK_RANKING_LIMIT),
    ] as const),
  )

  trackRankings.value = Object.fromEntries(
    rankingResults
      .filter(
        (result): result is PromiseFulfilledResult<readonly [number, TrackRecord[]]> =>
          result.status === 'fulfilled',
      )
      .map((result) => result.value),
  )
}

function resolveLapPosition(lapTime: LapTime): number | null {
  const ranking = trackRankings.value[lapTime.trackId] ?? []
  const matchingIndex = ranking.findIndex(
    (record) =>
      record.userDisplayName === lapTime.userDisplayName &&
      record.lapDate === lapTime.lapDate &&
      record.lapTimeMs === lapTime.lapTimeMs &&
      (record.vehicle ?? null) === (lapTime.vehicle ?? null),
  )

  return matchingIndex >= 0 ? matchingIndex + 1 : null
}

function handleEmailAction() {
  if (!publicProfile.value) {
    return
  }

  if (!auth.isAuthenticated.value) {
    auth.openAuthDialog()
    return
  }

  void router.push({
    name: 'messages',
    query: {
      receiverId: String(publicProfile.value.id),
    },
  })
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Perfil publico</p>
      <h1 class="ui-title-section">Preparando el perfil del piloto</h1>
      <p class="ui-copy-muted">Un momento, estamos reuniendo su actividad visible.</p>
    </section>

    <section v-else-if="error || !publicProfile" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Perfil publico</p>
      <h1 class="ui-title-section">Perfil no disponible</h1>
      <p class="ui-copy-muted">{{ error || 'No se pudo cargar este perfil.' }}</p>
      <RouterLink class="action-button" to="/">Volver al inicio</RouterLink>
    </section>

    <template v-else>
      <UserProfileHero
        :display-name="publicProfile.displayName"
        :completed-events="publicProfile.completedEvents"
        :visited-circuits="publicProfile.visitedCircuits"
        :top-five-lap-times="publicProfile.topFiveLapTimes"
        :pole-count="publicProfile.poleCount"
        :show-email-action="true"
        @email-action="handleEmailAction"
      />

      <section class="public-profile-tabs panel panel-pad-lg panel-stack-lg">

        <div class="public-profile-grid">
          <article class="panel panel-pad-lg panel-stack-md">
            <h2 class="ui-title-card">Historial de asistencias</h2>

            <p v-if="orderedBookings.length === 0" class="ui-copy-muted">
              Sin historial de asistencias para este piloto.
            </p>

            <div v-else class="public-profile-booking-list">
              <RouterLink
                v-for="booking in orderedBookings"
                :key="booking.id"
                class="public-profile-booking-card"
                :to="`/events/${booking.eventId}`"
              >
                <div class="panel-copy">
                  <p class="ui-eyebrow">{{ formatDisplayDate(booking.eventDate) }}</p>
                  <strong class="public-profile-booking-card__title">{{ booking.trackName }}</strong>
                  <p class="ui-copy-muted">{{ booking.organizerLegalName }}</p>
                </div>
              </RouterLink>
            </div>
          </article>

          <article class="panel panel-pad-lg panel-stack-md">
            <h2 class="ui-title-card">Vueltas rapidas</h2>

            <p v-if="groupedLapTimes.length === 0" class="ui-copy-muted">
              Este piloto todavia no ha registrado vueltas.
            </p>

            <div v-else class="public-profile-lap-groups">
              <section
                v-for="group in groupedLapTimes"
                :key="group.trackId"
                class="public-profile-lap-group"
              >
                <div class="panel-copy">                  
                  <h3 class="ui-eyebrow">{{ group.trackName }}</h3>
                </div>

                <div class="public-profile-lap-list">
                  <article
                    v-for="lapTime in group.laps"
                    :key="lapTime.id"
                    class="public-profile-lap-row"
                  >
                    <RankingPositionBadge
                      v-if="lapTime.position"
                      :position="lapTime.position"
                    />
                    <span v-else class="public-profile-lap-row__position-empty">--</span>

                    <div class="panel-copy public-profile-lap-row__copy">
                      <strong class="public-profile-lap-row__time">
                        {{ formatLapTime(lapTime.lapTimeMs) }}
                      </strong>
                      <p class="ui-copy-muted">
                        {{ formatDisplayDate(lapTime.lapDate) }}
                        <span v-if="lapTime.vehicle"> · {{ lapTime.vehicle }}</span>
                      </p>
                    </div>
                  </article>
                </div>
              </section>
            </div>
          </article>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
.public-profile-grid {
  display: grid;
  gap: var(--space-lg);
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  align-items: start;
}

.public-profile-booking-list,
.public-profile-lap-groups,
.public-profile-lap-list {
  display: grid;
  gap: var(--space-md);
}

.public-profile-booking-card {
  display: block;
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
  color: inherit;
  text-decoration: none;
  transition:
    transform 160ms ease,
    border-color 160ms ease;
}

.public-profile-booking-card:hover,
.public-profile-booking-card:focus-visible {
  transform: translateY(-2px);
  border-color: rgba(255, 45, 32, 0.24);
}

.public-profile-booking-card__title {
  color: var(--text-strong);
}

.public-profile-lap-group {
  display: grid;
  gap: var(--space-md);
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.public-profile-lap-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--space-md);
  align-items: center;
}

.public-profile-lap-row__position-empty {
  min-width: 48px;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-pill);
  text-align: center;
  font-family: var(--font-heading);
  font-weight: 900;
  letter-spacing: 0.08em;
  background: var(--surface-glass);
  color: var(--text-muted);
}

.public-profile-lap-row__copy {
  min-width: 0;
}

.public-profile-lap-row__time {
  color: var(--text-strong);
  font-size: var(--fs-title-sm);
}

@media (max-width: 980px) {
  .public-profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
