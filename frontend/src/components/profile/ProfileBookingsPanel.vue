<script setup lang="ts">
import { Eye, EyeOff } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import type { EventBooking } from '@/types/eventBooking'
import { formatDisplayDate } from '@/utils/format'

const props = defineProps<{
  bookingError: string
  activeBookings: EventBooking[]
  pastBookings: EventBooking[]
  bookingCancellingId: number | null
  bookingVisibilityUpdatingId: number | null
  cancellationCutoffIso: string
}>()

const emit = defineEmits<{
  requestCancel: [booking: EventBooking]
  toggleVisibility: [booking: EventBooking]
}>()

function canCancelBooking(booking: EventBooking): boolean {
  return booking.eventDate >= props.cancellationCutoffIso
}

function getBookingVisibilityToneClass(isVisible: boolean): string {
  return isVisible ? 'icon-button--danger' : 'icon-button--success'
}

function visibilityActionLabel(isVisible: boolean): string {
  return isVisible ? 'Ocultar en perfil publico' : 'Mostrar en perfil publico'
}
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="bookingError" class="status-message status-message--error">{{ bookingError }}</p>

    <div class="profile-grid">
      <article class="panel panel-pad-lg panel-stack-sm">
        <h3 class="ui-title-card">Reservas activas</h3>

        <p v-if="activeBookings.length === 0" class="ui-copy-muted">
          Todavia no tienes reservas futuras.
        </p>

        <div v-else class="surface-card-list">
          <article
            v-for="booking in activeBookings"
            :key="booking.id"
            class="surface-card"
          >
            <RouterLink class="profile-booking-card__main" :to="`/events/${booking.eventId}`">
              <div class="panel-copy">
                <p class="ui-eyebrow">{{ formatDisplayDate(booking.eventDate) }}</p>
                <strong class="profile-booking-card__title">{{ booking.trackName }}</strong>
                <p class="ui-copy-muted">{{ booking.organizerLegalName }}</p>
              </div>
            </RouterLink>

            <div class="surface-card__actions">
              <div class="profile-booking-card__action-row">
                <button
                  class="icon-button"
                  :class="getBookingVisibilityToneClass(booking.isVisible)"
                  type="button"
                  :disabled="bookingVisibilityUpdatingId === booking.id"
                  :aria-label="visibilityActionLabel(booking.isVisible)"
                  :title="visibilityActionLabel(booking.isVisible)"
                  @click="emit('toggleVisibility', booking)"
                >
                  <EyeOff
                    v-if="bookingVisibilityUpdatingId !== booking.id && booking.isVisible"
                    :size="16"
                    aria-hidden="true"
                  />
                  <Eye
                    v-else-if="bookingVisibilityUpdatingId !== booking.id"
                    :size="16"
                    aria-hidden="true"
                  />
                  <span v-else class="profile-booking-card__visibility-waiting">...</span>
                </button>

                <button
                  v-if="canCancelBooking(booking)"
                  class="action-button profile-booking-card__cancel"
                  type="button"
                  :disabled="bookingCancellingId === booking.id"
                  @click="emit('requestCancel', booking)"
                >
                  {{ bookingCancellingId === booking.id ? 'Anulando...' : 'Anular reserva' }}
                </button>

                <span v-else class="subtle-note profile-booking-card__note">
                  La anulacion se cierra 14 dias antes.
                </span>
              </div>
            </div>
          </article>
        </div>
      </article>

      <article class="panel panel-pad-lg panel-stack-sm">
        <h3 class="ui-title-card">Historial</h3>

        <p v-if="pastBookings.length === 0" class="ui-copy-muted">
          Tu historial todavia no muestra asistencias pasadas.
        </p>

        <div v-else class="surface-card-list">
          <article
            v-for="booking in pastBookings"
            :key="booking.id"
            class="surface-card"
          >
            <RouterLink class="profile-booking-card__main" :to="`/events/${booking.eventId}`">
              <div class="panel-copy">
                <p class="ui-eyebrow">{{ formatDisplayDate(booking.eventDate) }}</p>
                <strong class="profile-booking-card__title">{{ booking.trackName }}</strong>
                <p class="ui-copy-muted">{{ booking.organizerLegalName }}</p>
              </div>
            </RouterLink>

            <div class="surface-card__actions">
              <button
                class="icon-button"
                :class="getBookingVisibilityToneClass(booking.isVisible)"
                type="button"
                :disabled="bookingVisibilityUpdatingId === booking.id"
                :aria-label="visibilityActionLabel(booking.isVisible)"
                :title="visibilityActionLabel(booking.isVisible)"
                @click="emit('toggleVisibility', booking)"
              >
                <EyeOff
                  v-if="bookingVisibilityUpdatingId !== booking.id && booking.isVisible"
                  :size="16"
                  aria-hidden="true"
                />
                <Eye
                  v-else-if="bookingVisibilityUpdatingId !== booking.id"
                  :size="16"
                  aria-hidden="true"
                />
                <span v-else class="profile-booking-card__visibility-waiting">...</span>
              </button>
            </div>
          </article>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.profile-grid {
  display: grid;
  align-items: start;
  gap: var(--space-lg);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.profile-booking-card__title {
  color: var(--text-strong);
  font-size: var(--fs-title-info);
  font-weight: 800;
  text-decoration: none;
}

.profile-booking-card__main {
  display: block;
  flex: 1 1 auto;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.profile-booking-card__action-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-sm);
}

.profile-booking-card__cancel {
  background: linear-gradient(135deg, rgba(190, 34, 34, 0.92), rgba(126, 10, 10, 0.92));
}

.profile-booking-card__visibility-waiting {
  font-size: var(--fs-caption);
  font-weight: 700;
  letter-spacing: 0.08em;
}

.profile-booking-card__note {
  text-align: right;
}

@media (max-width: 980px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .profile-booking-card__action-row {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
