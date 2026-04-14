<script setup lang="ts">
import type { OrganizerWorkspaceEventStats, OrganizerWorkspaceStats } from '@/types/organizerWorkspace'
import { formatCurrency, formatDisplayDate } from '@/utils/format'

type RevenueChartBar = OrganizerWorkspaceEventStats & { width: string }

defineProps<{
  stats: OrganizerWorkspaceStats
  revenueChartBars: RevenueChartBar[]
}>()
</script>

<template>
  <section class="panel-stack-lg">
    <article class="panel panel-pad-lg panel-stack-lg">
      <div class="info-grid info-grid--two">
        <article class="info-tile">
          <p class="info-tile__label">Beneficio bruto total</p>
          <strong class="ui-title-inline-price">
            {{ formatCurrency(stats.totalGrossRevenue) }}
          </strong>
          <p class="info-tile__body">
            {{ formatCurrency(stats.totalBaseRevenue) }} en plazas +
            {{ formatCurrency(stats.totalServiceRevenue) }} en servicios
          </p>
        </article>

        <article class="info-tile">
          <p class="info-tile__label">Capacidad restante</p>
          <strong class="ui-title-inline-price">
            {{ stats.totalRemainingCapacity }}
          </strong>
          <p class="info-tile__body">
            Sobre un total agregado de {{ stats.totalCapacity }} plazas.
          </p>
        </article>
      </div>

      <div class="panel-copy">
        <h3 class="ui-title-card">Rendimiento por evento</h3>
        <p class="ui-copy-muted">
          Lectura rapida del comportamiento comercial de cada jornada.
        </p>
      </div>

      <div v-if="stats.eventStats.length === 0" class="ui-copy-muted">
        Todavia no hay eventos que analizar.
      </div>

      <div v-else class="panel-stack-lg">
        <article class="organizer-chart panel panel-pad-lg panel-stack-md">
          <div class="panel-copy">
            <h4 class="ui-title-card organizer-chart__title">Ingresos brutos por evento</h4>
            <p class="ui-copy-muted">
              Comparativa visual de facturacion entre jornadas.
            </p>
          </div>

          <div class="organizer-chart__bars">
            <article
              v-for="eventStat in revenueChartBars"
              :key="eventStat.eventId"
              class="organizer-chart__row"
            >
              <div class="organizer-chart__meta">
                <strong class="ui-title-info">{{ eventStat.trackName }}</strong>
                <span class="ui-copy-muted">{{ formatDisplayDate(eventStat.eventDate) }}</span>
              </div>
              <div class="organizer-chart__track">
                <div class="organizer-chart__fill" :style="{ width: eventStat.width }"></div>
              </div>
              <strong class="organizer-chart__value">
                {{ formatCurrency(eventStat.grossRevenue) }}
              </strong>
            </article>
          </div>
        </article>

        <div class="list-divider">
          <article
            v-for="eventStat in revenueChartBars"
            :key="eventStat.eventId"
            class="list-divider__item organizer-stats-row"
          >
            <div class="panel-copy">
              <strong class="ui-title-info">{{ eventStat.trackName }}</strong>
              <p class="ui-copy-muted">{{ formatDisplayDate(eventStat.eventDate) }}</p>
            </div>

            <div class="organizer-stats-row__metrics">
              <span>{{ eventStat.bookings }} asistentes</span>
              <span>{{ eventStat.soldServices }} servicios</span>
              <strong>{{ formatCurrency(eventStat.grossRevenue) }}</strong>
            </div>
          </article>
        </div>
      </div>
    </article>
  </section>
</template>

<style scoped>
.organizer-stats-row__metrics {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-lg);
  color: var(--text-muted);
}

.organizer-chart {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background:
    radial-gradient(circle at top right, rgba(255, 70, 49, 0.18), transparent 36%),
    linear-gradient(180deg, rgba(18, 18, 20, 0.9) 0%, rgba(12, 12, 14, 0.96) 100%);
}

.organizer-chart__title {
  font-size: var(--fs-title-info);
}

.organizer-chart__bars {
  display: grid;
  gap: var(--space-md);
}

.organizer-chart__row {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(0, 2fr) auto;
  align-items: center;
  gap: var(--space-md);
}

.organizer-chart__meta {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.organizer-chart__track {
  position: relative;
  min-width: 0;
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
}

.organizer-chart__fill {
  height: 100%;
  min-width: 8px;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(255, 76, 58, 0.92) 0%, rgba(255, 167, 55, 0.92) 100%);
  box-shadow: 0 0 18px rgba(255, 76, 58, 0.28);
}

.organizer-chart__value {
  color: var(--text-strong);
  white-space: nowrap;
}

@media (max-width: 720px) {
  .organizer-stats-row__metrics {
    flex-direction: column;
    align-items: stretch;
  }

  .organizer-chart__row {
    grid-template-columns: 1fr;
  }
}
</style>
