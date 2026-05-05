<script setup lang="ts">
import { computed } from 'vue'
import { CalendarDays, Eye, EyeOff, Trash2 } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import AppModal from '@/components/AppModal.vue'
import type { EventBooking } from '@/types/eventBooking'
import { formatCurrency, formatDisplayDate } from '@/utils/format'

type BookedServiceLine = {
  id: number
  name: string
  price: number
}

const props = defineProps<{
  isOpen: boolean
  booking: EventBooking | null
  services: BookedServiceLine[]
  loading: boolean
  loadingError: string
  cancellationError: string
  isCancelling: boolean
  canCancel: boolean
}>()

defineEmits<{
  // El padre controla cerrar y cancelar para mantener la lista sincronizada.
  close: []
  cancel: []
}>()

const totalPrice = computed(() =>
  // Total real pagado: entrada base histórica + servicios contratados.
  props.booking
    ? props.booking.basePriceAtPurchase +
      props.services.reduce((sum, service) => sum + service.price, 0)
    : 0,
)

const eventPath = computed(() => (props.booking ? `/events/${props.booking.eventId}` : '/events'))
</script>

<template>
  <AppModal
    :is-open="props.isOpen"
    ariaLabel="Detalle de reserva"
    eyebrow="Reserva"
    :title="props.booking?.trackName ?? ''"
    width="680px"
    @close="$emit('close')"
  >
    <div v-if="props.booking" class="panel-stack-lg">
      <div class="surface-detail-grid">
        <article class="surface-detail-item">
          <span class="surface-detail-item__label">Evento contratado</span>
          <span class="surface-detail-item__value">{{ props.booking.trackName }}</span>
        </article>

        <article class="surface-detail-item">
          <span class="surface-detail-item__label">Fecha</span>
          <span class="surface-detail-item__value">{{ formatDisplayDate(props.booking.eventDate) }}</span>
        </article>

        <article class="surface-detail-item">
          <span class="surface-detail-item__label">Organizador</span>
          <span class="surface-detail-item__value">{{ props.booking.organizerLegalName }}</span>
        </article>

        <article class="surface-detail-item">
          <span class="surface-detail-item__label">Visibilidad</span>
          <span class="surface-detail-item__value profile-booking-detail__visibility">
            <Eye v-if="props.booking.isVisible" :size="15" aria-hidden="true" />
            <EyeOff v-else :size="15" aria-hidden="true" />
            {{ props.booking.isVisible ? 'Visible en perfil público' : 'Oculta en perfil público' }}
          </span>
        </article>
      </div>

      <article class="surface-detail-item surface-detail-item--full">
        <span class="surface-detail-item__label">Entrada base</span>
        <span class="surface-detail-item__value">
          {{ formatCurrency(props.booking.basePriceAtPurchase) }}
        </span>
      </article>

      <section class="panel-stack-sm">
        <div class="panel-copy">
          <p class="ui-eyebrow ui-eyebrow--muted">Servicios contratados</p>
        </div>

        <div v-if="props.loading" class="surface-detail-item surface-detail-item--full">
          <span class="ui-copy-muted">Cargando servicios contratados...</span>
        </div>

        <p v-else-if="props.loadingError" class="status-message status-message--error">
          {{ props.loadingError }}
        </p>

        <div v-else-if="props.services.length > 0" class="surface-card-list">
          <article
            v-for="service in props.services"
            :key="service.id"
            class="profile-booking-detail__service"
          >
            <span class="surface-detail-item__value">{{ service.name }}</span>
            <strong class="surface-detail-item__value">{{ formatCurrency(service.price) }}</strong>
          </article>
        </div>

        <div v-else class="surface-detail-item surface-detail-item--full">
          <span class="ui-copy-muted">Esta reserva no tiene servicios adicionales contratados.</span>
        </div>
      </section>

      <article class="surface-detail-item surface-detail-item--full profile-booking-detail__total">
        <span class="surface-detail-item__label">Total contratado</span>
        <span class="ui-title-inline-price">{{ formatCurrency(totalPrice) }}</span>
      </article>

      <p v-if="props.cancellationError" class="status-message status-message--error">
        {{ props.cancellationError }}
      </p>

      <div class="profile-booking-detail__actions">
        <RouterLink class="action-button action-button--ghost" :to="eventPath">
          <CalendarDays :size="16" aria-hidden="true" />
          Ver evento
        </RouterLink>

        <button
          v-if="props.canCancel"
          class="action-button profile-booking-detail__cancel"
          type="button"
          :disabled="props.isCancelling"
          @click="$emit('cancel')"
        >
          <Trash2 :size="16" aria-hidden="true" />
          {{ props.isCancelling ? 'Anulando...' : 'Anular reserva' }}
        </button>
      </div>

      <p v-if="!props.canCancel" class="ui-copy-caption">
        La anulacion se cierra 14 dias antes del evento.
      </p>
    </div>
  </AppModal>
</template>

<style scoped>
.profile-booking-detail__visibility {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
}

.profile-booking-detail__service {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.profile-booking-detail__total {
  background: var(--surface-glass);
}

.profile-booking-detail__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-sm);
}

.profile-booking-detail__cancel {
  background: linear-gradient(135deg, rgba(190, 34, 34, 0.92), rgba(126, 10, 10, 0.92));
}

@media (max-width: 640px) {
  .profile-booking-detail__service,
  .profile-booking-detail__actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
