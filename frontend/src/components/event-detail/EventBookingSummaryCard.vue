<script setup lang="ts">
import type { DisplayService } from '@/types/eventDetail'
import { formatCurrency } from '@/utils/format'

// Resumen lateral de reserva: el padre calcula precios y aquí solo los muestro.
defineProps<{
  isPastEvent: boolean
  totalPriceLabel: string
  formattedPrice: string
  selectedServices: DisplayService[]
  bookingButtonLabel: string
  bookingCaption: string
  isBookingActionDisabled: boolean
}>()

defineEmits<{
  // El botón principal avisa al padre para abrir confirmación o login.
  bookingAction: []
}>()
</script>

<template>
  <article class="event-detail__booking-card panel panel-pad-lg panel-stack-lg">
    <div class="panel-copy">
      <p class="ui-eyebrow">Reserva</p>
      <h2 class="ui-title-price">{{ totalPriceLabel }}</h2>
      <p class="ui-copy-meta">Total estimado con la seleccion actual</p>
    </div>

    <p v-if="isPastEvent" class="event-detail__booking-status">
      Evento finalizado
    </p>

    <div class="event-detail__booking-lines">
      <div class="event-detail__booking-line">
        <span>Entrada base</span>
        <strong>{{ formattedPrice }}</strong>
      </div>

      <div
        v-for="service in selectedServices"
        :key="service.id"
        class="event-detail__booking-line event-detail__booking-line--selected"
      >
        <span>{{ service.name }}</span>
        <strong>{{ formatCurrency(service.price) }}</strong>
      </div>
    </div>

    <p v-if="selectedServices.length === 0" class="ui-copy-muted">
      No has seleccionado servicios adicionales.
    </p>

    <button
      class="action-button event-detail__booking-cta"
      type="button"
      :disabled="isBookingActionDisabled"
      @click="$emit('bookingAction')"
    >
      {{ bookingButtonLabel }}
    </button>

    <p class="ui-copy-caption">{{ bookingCaption }}</p>
  </article>
</template>

<style scoped>
.event-detail__booking-card {
  border-color: var(--line-strong);
}

.event-detail__booking-status {
  margin: 0;
  padding: var(--space-sm) var(--space-md);
  border: 1px solid rgba(255, 160, 72, 0.24);
  border-radius: var(--radius-pill);
  background: rgba(255, 160, 72, 0.12);
  color: var(--racing-amber);
  font-size: var(--fs-caption);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  width: fit-content;
}

.event-detail__booking-lines {
  display: grid;
  gap: var(--space-sm);
}

.event-detail__booking-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  background: var(--surface-glass);
}

.event-detail__booking-line strong {
  color: var(--text-strong);
}

.event-detail__booking-line--selected {
  border-color: var(--line-strong);
  background: var(--accent-soft);
}

.event-detail__booking-cta {
  width: 100%;
}

@media (max-width: 640px) {
  .event-detail__booking-line {
    flex-direction: column;
    align-items: start;
  }
}
</style>
