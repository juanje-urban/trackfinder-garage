<script setup lang="ts">
import { Pencil, Plus, Trash2 } from 'lucide-vue-next'
import type { OrganizerManagedEvent } from '@/types/organizerWorkspace'
import { formatCurrency, formatDisplayDate } from '@/utils/format'

// Panel de eventos del organizador: recibe eventos ya ordenados y emite crear/editar/borrar.
const props = defineProps<{
  eventError: string
  events: OrganizerManagedEvent[]
  todayIso: string
}>()

defineEmits<{
  // La vista padre conserva la lógica real de formularios y llamadas HTTP.
  create: []
  edit: [managedEvent: OrganizerManagedEvent]
  delete: [managedEvent: OrganizerManagedEvent]
}>()

function isPastEvent(eventDate: string): boolean {
  // Evento pasado queda en modo lectura.
  return eventDate <= props.todayIso
}

function canDeleteEvent(managedEvent: OrganizerManagedEvent): boolean {
  // Solo dejo borrar futuros sin reservas para evitar perder datos de asistentes.
  return !isPastEvent(managedEvent.event.eventDate) && managedEvent.stats.bookings === 0
}

function formatEventServiceName(service: OrganizerManagedEvent['services'][number]): string {
  // El servicio puede venir del circuito o del organizador.
  return service.trackServiceName ?? service.organizerServiceName ?? ''
}
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="eventError" class="status-message status-message--error">{{ eventError }}</p>

    <article class="panel panel-pad-lg panel-stack-md">
      <div class="section-header">
        <div class="panel-copy">
          <h3 class="ui-title-card">Eventos del organizador</h3>
          <p class="ui-copy-muted">
            Crea jornadas nuevas y ajusta precio base, aforo y servicios ofertados en cada evento.
          </p>
        </div>

        <button class="action-button organizer-events__button" type="button" @click="$emit('create')">
          <Plus :size="16" aria-hidden="true" />
          Crear evento
        </button>
      </div>

      <p v-if="events.length === 0" class="ui-copy-muted">Todavía no has creado ningún evento.</p>

      <div v-else class="surface-card-list">
        <article
          v-for="managedEvent in events"
          :key="managedEvent.event.id"
          class="surface-card organizer-event-card"
        >
          <div class="panel-copy surface-card__copy">
            <p class="ui-eyebrow">{{ formatDisplayDate(managedEvent.event.eventDate) }}</p>
            <h4 class="ui-title-card">{{ managedEvent.event.trackName }}</h4>
            <p class="ui-copy-muted">
              Desde {{ formatCurrency(managedEvent.event.basePrice) }} &middot;
              {{ managedEvent.stats.bookings }} asistentes &middot;
              {{ managedEvent.stats.remainingCapacity }} / {{ managedEvent.stats.totalCapacity }}
              plazas libres &middot; {{ managedEvent.services.length }} servicios configurados
              &middot; {{ formatCurrency(managedEvent.stats.grossRevenue) }} brutos
            </p>

            <div class="meta-pills">
              <span v-if="isPastEvent(managedEvent.event.eventDate)" class="meta-pill">
                Evento finalizado
              </span>
            </div>

            <div v-if="managedEvent.services.length > 0" class="meta-pills">
              <span
                v-for="service in managedEvent.services"
                :key="service.id"
                class="meta-pill"
              >
                {{ formatEventServiceName(service) }} &middot; {{ formatCurrency(service.price) }}
              </span>
            </div>
          </div>

          <div
            v-if="!isPastEvent(managedEvent.event.eventDate)"
            class="surface-card__actions organizer-event-card__actions"
          >
            <button
              class="icon-button icon-button--danger"
              type="button"
              aria-label="Editar evento"
              title="Editar evento"
              @click="$emit('edit', managedEvent)"
            >
              <Pencil :size="16" aria-hidden="true" />
            </button>

            <button
              v-if="canDeleteEvent(managedEvent)"
              class="icon-button icon-button--danger"
              type="button"
              aria-label="Eliminar evento"
              title="Eliminar evento"
              @click="$emit('delete', managedEvent)"
            >
              <Trash2 :size="16" aria-hidden="true" />
            </button>
          </div>
        </article>
      </div>
    </article>
  </section>
</template>

<style scoped>
.organizer-events__button {
  flex: none;
}

.organizer-event-card {
  align-items: start;
}

.organizer-event-card__actions {
  gap: var(--space-sm);
  padding-inline-start: var(--space-sm);
  flex-wrap: nowrap;
}

@media (max-width: 720px) {
  .organizer-events__button {
    width: 100%;
  }
}
</style>
