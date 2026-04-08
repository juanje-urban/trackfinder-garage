<script setup lang="ts">
import { computed } from 'vue'
import type { TrackRecord } from '@/types/trackRecord'
import { formatDisplayDate, formatLapTime } from '@/utils/format'

const props = defineProps<{
  trackName: string
  eventDate: string
  ranking: TrackRecord[]
}>()

const bestLap = computed(() => props.ranking[0])

function positionLabel(index: number): string {
  return `P${index + 1}`
}
</script>

<template>
  <article class="track-record-board panel">
    <div class="track-record-board__rail"></div>

    <div class="track-record-board__header">
      <div class="panel-stack-sm">
        <h3 class="ui-title-card">{{ trackName }}</h3>
      </div>

      <div v-if="bestLap" class="track-record-board__best">
        <p class="ui-stat-label">Best lap</p>
        <p class="track-record-board__best-time">{{ formatLapTime(bestLap.lapTimeMs) }}</p>
        <p class="track-record-board__best-driver">
          {{ bestLap.userDisplayName }}
        </p>
      </div>
    </div>

    <div class="track-record-board__list">
      <div
        v-for="(record, index) in ranking"
        :key="`${record.trackId}-${record.userDisplayName}-${record.lapTimeMs}-${index}`"
        class="track-record-board__row"
      >
        <span
          class="track-record-board__position"
          :class="`track-record-board__position--${index + 1}`"
        >
          {{ positionLabel(index) }}
        </span>

        <div class="track-record-board__driver">
          <strong>{{ record.userDisplayName }}</strong>
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

.track-record-board__position {
  min-width: 48px;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-pill);
  text-align: center;
  font-family: var(--font-heading);
  font-weight: 900;
  letter-spacing: 0.08em;
}

.track-record-board__position--1 {
  background: var(--accent-emphasis);
  color: var(--text-strong);
}

.track-record-board__position--2 {
  background: var(--racing-amber-soft);
  color: var(--racing-amber);
}

.track-record-board__position--3 {
  background: var(--racing-blue-soft);
  color: var(--racing-blue);
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
