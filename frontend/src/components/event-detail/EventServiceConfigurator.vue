<script setup lang="ts">
import SectionCard from '@/components/SectionCard.vue'
import type { DisplayServiceGroup } from '@/types/eventDetail'
import { formatCurrency } from '@/utils/format'

defineProps<{
  isPastEvent: boolean
  servicesLoading: boolean
  servicesError: string
  serviceGroups: DisplayServiceGroup[]
  selectedServiceIds: number[]
  isLocked: boolean
}>()

defineEmits<{
  toggleService: [serviceId: number]
}>()

function isSelected(selectedServiceIds: number[], serviceId: number): boolean {
  return selectedServiceIds.includes(serviceId)
}
</script>

<template>
  <SectionCard
    v-if="!isPastEvent"
    eyebrow="Servicios"
    title="Configura tu reserva"
    description="Selecciona los servicios disponibles del circuito y del organizador para este evento."
  >
    <div v-if="servicesLoading" class="status-message">
      Cargando servicios disponibles...
    </div>
    <p v-else-if="servicesError" class="status-message status-message--error">
      {{ servicesError }}
    </p>
    <p
      v-else-if="serviceGroups.every((group) => group.services.length === 0)"
      class="status-message"
    >
      No hay servicios adicionales disponibles para este evento.
    </p>

    <div v-else class="event-detail__service-groups">
      <article
        v-for="group in serviceGroups"
        :key="group.id"
        class="event-detail__service-group"
      >
        <div class="panel-copy">
          <p class="ui-eyebrow">{{ group.eyebrow }}</p>
          <h3 class="ui-title-card">{{ group.title }}</h3>
        </div>

        <div v-if="group.services.length > 0" class="event-detail__service-list">
          <label
            v-for="service in group.services"
            :key="service.id"
            class="event-detail__service-row"
            :class="{
              'event-detail__service-row--selected': isSelected(selectedServiceIds, service.id),
              'event-detail__service-row--locked': isLocked,
            }"
          >
            <input
              class="event-detail__service-checkbox"
              type="checkbox"
              :checked="isSelected(selectedServiceIds, service.id)"
              :disabled="isLocked"
              @change="$emit('toggleService', service.id)"
            />
            <span class="event-detail__service-name">{{ service.name }}</span>
            <strong>{{ formatCurrency(service.price) }}</strong>
          </label>
        </div>

        <p v-else class="ui-copy-muted">{{ group.emptyMessage }}</p>
      </article>
    </div>
  </SectionCard>
</template>

<style scoped>
.event-detail__service-groups {
  display: grid;
  gap: var(--space-lg);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.event-detail__service-group {
  display: grid;
  gap: var(--space-lg);
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.event-detail__service-list {
  display: grid;
  gap: var(--space-sm);
}

.event-detail__service-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  background: var(--surface-glass);
}

.event-detail__service-row--selected {
  border-color: var(--line-strong);
  background:
    linear-gradient(180deg, rgba(255, 45, 32, 0.1) 0%, rgba(255, 45, 32, 0.03) 100%);
}

.event-detail__service-row--locked {
  opacity: 0.82;
}

.event-detail__service-checkbox {
  accent-color: var(--accent-strong);
}

.event-detail__service-name {
  min-width: 0;
  color: var(--text-body);
}

.event-detail__service-row strong {
  color: var(--text-strong);
}

@media (max-width: 760px) {
  .event-detail__service-groups {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .event-detail__service-row {
    grid-template-columns: 1fr;
    align-items: start;
  }
}
</style>
