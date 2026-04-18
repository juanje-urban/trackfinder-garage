<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import RankingPositionBadge from '@/components/RankingPositionBadge.vue'
import { getVisibleEventBookingsByEventId } from '@/services/eventBookingService'
import { getLapTimesByUserId } from '@/services/lapTimeService'
import { getTrackRanking } from '@/services/trackService'
import type { EventBooking } from '@/types/eventBooking'
import type { LapTime } from '@/types/lapTime'
import type { TrackRecord } from '@/types/trackRecord'
import UserProfileLink from '@/components/UserProfileLink.vue'

const props = defineProps<{
  eventId: number
  trackId: number
}>()

const FULL_TRACK_RANKING_LIMIT = 200

const loading = ref(true)
const error = ref('')
const attendees = ref<EventBooking[]>([])
const attendeePositions = ref<Record<number, number>>({})

async function loadAttendees() {
  loading.value = true
  error.value = ''

  try {
    const nextAttendees = await getVisibleEventBookingsByEventId(props.eventId)
    attendees.value = nextAttendees
    await refreshAttendeePositions(nextAttendees)
  } catch {
    attendees.value = []
    attendeePositions.value = {}
    error.value = 'No se pudo cargar la lista de asistentes visibles.'
  } finally {
    loading.value = false
  }
}

async function refreshAttendeePositions(currentAttendees: EventBooking[]) {
  attendeePositions.value = {}

  if (currentAttendees.length === 0) {
    return
  }

  try {
    const ranking = await getTrackRanking(props.trackId, FULL_TRACK_RANKING_LIMIT)
    if (ranking.length === 0) {
      return
    }

    const lapTimeResults = await Promise.allSettled(
      currentAttendees.map(async (booking) => [booking.userId, await getLapTimesByUserId(booking.userId)] as const),
    )

    attendeePositions.value = Object.fromEntries(
      lapTimeResults
        .filter(
          (result): result is PromiseFulfilledResult<readonly [number, LapTime[]]> =>
            result.status === 'fulfilled',
        )
        .map((result) => [
          result.value[0],
          resolveBestPosition(result.value[1], ranking),
        ])
        .filter((entry): entry is [number, number] => entry[1] !== null),
    )
  } catch {
    attendeePositions.value = {}
  }
}

function getAttendeePosition(userId: number): number | null {
  return attendeePositions.value[userId] ?? null
}

function resolveBestPosition(userLapTimes: LapTime[], ranking: TrackRecord[]): number | null {
  const positions = userLapTimes
    .filter((lapTime) => lapTime.trackId === props.trackId)
    .map((lapTime) => findRankingPosition(lapTime, ranking))
    .filter((position): position is number => position !== null)

  if (positions.length === 0) {
    return null
  }

  return Math.min(...positions)
}

function findRankingPosition(lapTime: LapTime, ranking: TrackRecord[]): number | null {
  const rankingIndex = ranking.findIndex(
    (record) =>
      record.userDisplayName === lapTime.userDisplayName &&
      record.lapDate === lapTime.lapDate &&
      record.lapTimeMs === lapTime.lapTimeMs &&
      (record.vehicle ?? null) === (lapTime.vehicle ?? null),
  )

  return rankingIndex >= 0 ? rankingIndex + 1 : null
}

onMounted(() => {
  void loadAttendees()
})

watch(
  () => [props.eventId, props.trackId] as const,
  () => {
    void loadAttendees()
  },
)
</script>

<template>
  <article class="event-attendee-list panel panel-pad-lg panel-stack-md">
    <div class="panel-copy">
      <p class="ui-eyebrow">Paddock</p>
      <h3 class="ui-title-card">Asistentes</h3>
    </div>

    <p v-if="loading" class="ui-copy-muted">Cargando asistentes visibles...</p>
    <p v-else-if="error" class="status-message status-message--error">{{ error }}</p>
    <p v-else-if="attendees.length === 0" class="ui-copy-muted">
      Nadie ha mostrado todavía su asistencia en este evento.
    </p>

    <div v-else class="event-attendee-list__rows">
      <div
        v-for="booking in attendees"
        :key="booking.id"
        class="event-attendee-list__row"
      >
        <UserProfileLink
          :display-name="booking.userDisplayName"
          class="event-attendee-list__name"
        >
          {{ booking.userDisplayName }}
        </UserProfileLink>

        <RankingPositionBadge
          v-if="getAttendeePosition(booking.userId) !== null"
          :position="getAttendeePosition(booking.userId) ?? 0"
          class="event-attendee-list__position"
        />
      </div>
    </div>
  </article>
</template>

<style scoped>
.event-attendee-list__rows {
  display: grid;
  gap: var(--space-sm);
}

.event-attendee-list__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-inner);
  background: var(--surface-glass-subtle);
}

.event-attendee-list__name {
  font-weight: 700;
}

.event-attendee-list__position {
  margin-left: auto;
}

</style>
