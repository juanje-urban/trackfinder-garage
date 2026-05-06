<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AppModal from '@/components/AppModal.vue'
import MetricCard from '@/components/MetricCard.vue'
import OrganizerEventsPanel from '@/components/organizer/OrganizerEventsPanel.vue'
import OrganizerServicesPanel from '@/components/organizer/OrganizerServicesPanel.vue'
import OrganizerStatsPanel from '@/components/organizer/OrganizerStatsPanel.vue'
import PageHero from '@/components/PageHero.vue'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'
import {
  addOrganizerCatalogService,
  createOrganizerEvent,
  deleteOrganizerEvent,
  getOrganizerWorkspace,
  removeOrganizerCatalogService,
  updateOrganizerEvent,
} from '@/services/organizerWorkspaceService'
import type { OrganizerManagedEvent, OrganizerWorkspace } from '@/types/organizerWorkspace'
import { resolveApiErrorMessage } from '@/utils/apiErrors'
import { isOrganizerRole } from '@/utils/authRoles'
import { toIsoDate } from '@/utils/date'
import { formatCurrency, formatDisplayDate } from '@/utils/format'

type OrganizerTab = 'services' | 'events' | 'stats'

const auth = useAuth()
const toast = useToast()

// El panel del organizador trabaja casi entero contra 'workspace', que trae datos y métricas juntos.
const loading = ref(true)
const error = ref('')
const activeTab = ref<OrganizerTab>('events')
const workspace = ref<OrganizerWorkspace | null>(null)

const serviceError = ref('')
const eventError = ref('')

const selectedAvailableServiceId = ref<number | ''>('')
const serviceSubmitting = ref(false)
const removingOrganizerServiceId = ref<number | null>(null)

const isEventModalOpen = ref(false)
const editingEventId = ref<number | null>(null)
const eventSaving = ref(false)
const deletingEventId = ref<number | null>(null)
const eventPendingDeletion = ref<OrganizerManagedEvent | null>(null)
const isDeleteEventModalOpen = ref(false)

const eventForm = reactive({
  trackId: '',
  eventDate: '',
  basePrice: '',
  maxParticipants: '',
  description: '',
})

// Guardo selecciones de servicios por separado para montar luego el payload de evento.
const selectedTrackServiceIds = ref<number[]>([])
const selectedOrganizerServiceIds = ref<number[]>([])
const servicePriceInputs = reactive<Record<string, string>>({})

const todayIso = computed(() => toIsoDate(new Date()))
const isOrganizer = computed(() => isOrganizerRole(auth.session.value?.roleName))

const tabItems: Array<{ id: OrganizerTab; label: string }> = [
  { id: 'events', label: 'Eventos' },
  { id: 'services', label: 'Servicios' },
  { id: 'stats', label: 'Estadisticas' },
]

const sortedEvents = computed(() =>
  // Muestro primero los eventos más recientes.
  [...(workspace.value?.events ?? [])].sort((left, right) =>
    right.event.eventDate.localeCompare(left.event.eventDate),
  ),
)

const availableCatalogServices = computed(() => {
  if (!workspace.value) {
    return []
  }

  // Ofrezco solo servicios que el organizador aún no tiene en su catálogo.
  const assignedIds = new Set(
    workspace.value.organizerServices
      .map((service) => service.serviceId)
      .filter((serviceId): serviceId is number => typeof serviceId === 'number'),
  )

  return workspace.value.availableServices
    .filter((service) => !assignedIds.has(service.id))
    .sort((left, right) => left.name.localeCompare(right.name))
})

const currentTrackServiceOptions = computed(() => {
  // Al cambiar de circuito cambian los servicios de pista disponibles.
  if (!workspace.value || eventForm.trackId === '') {
    return []
  }

  return workspace.value.trackServices
    .filter((service) => service.trackId === Number(eventForm.trackId))
    .sort((left, right) => left.serviceName.localeCompare(right.serviceName))
})

const organizerServiceOptions = computed(() =>
  [...(workspace.value?.organizerServices ?? [])].sort((left, right) =>
    left.serviceName.localeCompare(right.serviceName),
  ),
)

const futureOrganizerServiceIdsInUse = computed(() => {
  // Si un servicio está en un evento futuro, no dejo retirarlo del catálogo.
  const nextIds = new Set<number>()

  for (const managedEvent of workspace.value?.events ?? []) {
    if (managedEvent.event.eventDate <= todayIso.value) {
      continue
    }

    for (const service of managedEvent.services) {
      if (service.organizerServiceId !== null) {
        nextIds.add(service.organizerServiceId)
      }
    }
  }

  return nextIds
})

const revenueChartBars = computed(() => {
  // Convierto ingresos en porcentajes para que el componente pueda pintar barras.
  const eventStats = [...(workspace.value?.stats.eventStats ?? [])].sort((left, right) =>
    right.eventDate.localeCompare(left.eventDate),
  )
  const maxRevenue = Math.max(...eventStats.map((stat) => stat.grossRevenue), 0)

  return eventStats.map((stat) => ({
    ...stat,
    width: maxRevenue > 0 ? `${Math.max(8, Math.round((stat.grossRevenue / maxRevenue) * 100))}%` : '0%',
  }))
})

const editingManagedEvent = computed(
  () => workspace.value?.events.find((event) => event.event.id === editingEventId.value) ?? null,
)

const minimumParticipantsForEdit = computed(() => editingManagedEvent.value?.stats.bookings ?? 1)

onMounted(async () => {
  // Protejo la vista también en cliente, aunque el backend vuelva a validar permisos.
  if (!isOrganizer.value) {
    loading.value = false
    error.value = 'Esta area esta reservada para cuentas con rol organizador.'
    return
  }

  await loadWorkspace()
})

watch(
  () => eventForm.trackId,
  () => {
    // Cuando cambio de circuito limpio servicios de pista que ya no pertenecen a ese circuito.
    const validIds = new Set(currentTrackServiceOptions.value.map((service) => service.id))
    selectedTrackServiceIds.value = selectedTrackServiceIds.value.filter((id) => validIds.has(id))

    Object.keys(servicePriceInputs).forEach((key) => {
      if (!key.startsWith('track:')) {
        return
      }

      const trackServiceId = Number(key.slice('track:'.length))
      if (!validIds.has(trackServiceId)) {
        delete servicePriceInputs[key]
      }
    })
  },
)

async function loadWorkspace() {
  loading.value = true
  error.value = ''

  try {
    // Una sola petición deja el panel consistente: catálogo, eventos y estadísticas llegan juntos.
    workspace.value = await getOrganizerWorkspace()
  } catch (requestError) {
    error.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo cargar tu área de organización.',
      statusMessages: {
        403: 'Esta area esta reservada para cuentas con rol organizador.',
      },
      matches: [
        {
          includes: 'pending approval',
          message: 'Tu perfil de organizador sigue pendiente de aprobacion.',
        },
      ],
    })
  } finally {
    loading.value = false
  }
}

function resetEventForm() {
  // Dejo el modal listo tanto para crear como para editar otro evento.
  editingEventId.value = null
  eventForm.trackId = ''
  eventForm.eventDate = ''
  eventForm.basePrice = ''
  eventForm.maxParticipants = ''
  eventForm.description = ''
  selectedTrackServiceIds.value = []
  selectedOrganizerServiceIds.value = []
  Object.keys(servicePriceInputs).forEach((key) => delete servicePriceInputs[key])
  eventError.value = ''
}

function closeEventModal() {
  if (eventSaving.value) {
    return
  }

  hideEventModal()
}

function hideEventModal() {
  isEventModalOpen.value = false
  resetEventForm()
}

function closeDeleteEventModal() {
  if (deletingEventId.value !== null) {
    return
  }

  eventError.value = ''
  isDeleteEventModalOpen.value = false
  eventPendingDeletion.value = null
}

function openCreateEventModal() {
  resetEventForm()
  isEventModalOpen.value = true
}

function openEditEventModal(managedEvent: OrganizerManagedEvent) {
  // Paso del modelo del backend al formulario plano que entienden los inputs.
  resetEventForm()
  editingEventId.value = managedEvent.event.id
  eventForm.trackId = String(managedEvent.event.trackId)
  eventForm.eventDate = managedEvent.event.eventDate
  eventForm.basePrice = String(managedEvent.event.basePrice)
  eventForm.maxParticipants = String(managedEvent.event.maxParticipants)
  eventForm.description = managedEvent.event.description

  for (const service of managedEvent.services) {
    if (service.trackServiceId !== null) {
      selectedTrackServiceIds.value.push(service.trackServiceId)
      servicePriceInputs[toServiceKey('track', service.trackServiceId)] = String(service.price)
      continue
    }

    if (service.organizerServiceId !== null) {
      selectedOrganizerServiceIds.value.push(service.organizerServiceId)
      servicePriceInputs[toServiceKey('organizer', service.organizerServiceId)] = String(service.price)
    }
  }

  isEventModalOpen.value = true
}

function openDeleteEventModal(managedEvent: OrganizerManagedEvent) {
  eventError.value = ''
  eventPendingDeletion.value = managedEvent
  isDeleteEventModalOpen.value = true
}

function isOrganizerServiceLocked(organizerServiceId: number): boolean {
  return futureOrganizerServiceIdsInUse.value.has(organizerServiceId)
}

function isTrackServiceLockedForEdit(trackServiceId: number): boolean {
  return Boolean(
    editingManagedEvent.value?.services.some(
      (service) => service.trackServiceId === trackServiceId && service.hasBookings,
    ),
  )
}

function isOrganizerEventServiceLockedForEdit(organizerServiceId: number): boolean {
  return Boolean(
    editingManagedEvent.value?.services.some(
      (service) => service.organizerServiceId === organizerServiceId && service.hasBookings,
    ),
  )
}

function toServiceKey(kind: 'track' | 'organizer', id: number): string {
  // Uso una clave con prefijo para no mezclar ids de servicios de pista y de organizador.
  return `${kind}:${id}`
}

async function addCatalogService() {
  // Añadir al catálogo no crea el servicio maestro; solo lo activa para este organizador.
  if (selectedAvailableServiceId.value === '') {
    serviceError.value = 'Selecciona un servicio para añadirlo al catálogo.'
    return
  }

  serviceSubmitting.value = true
  serviceError.value = ''

  try {
    workspace.value = await addOrganizerCatalogService(Number(selectedAvailableServiceId.value))
    selectedAvailableServiceId.value = ''
    toast.showToast('El servicio se ha añadido a tu catálogo.')
  } catch (requestError) {
    serviceError.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo añadir el servicio al catálogo.',
    })
  } finally {
    serviceSubmitting.value = false
  }
}

async function removeCatalogService(organizerServiceId: number) {
  removingOrganizerServiceId.value = organizerServiceId
  serviceError.value = ''

  try {
    workspace.value = await removeOrganizerCatalogService(organizerServiceId)
    toast.showToast('El servicio se ha retirado de tu catálogo.')
  } catch (requestError) {
    serviceError.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo retirar el servicio del catálogo.',
      matches: [
        {
          includes: 'attached to future events',
          message: 'No puedes retirar un servicio que ya esta presente en eventos futuros.',
        },
      ],
    })
  } finally {
    removingOrganizerServiceId.value = null
  }
}

function requestCatalogServiceRemoval(organizerServiceId: number) {
  if (isOrganizerServiceLocked(organizerServiceId)) {
    toast.showToast('No puedes retirar un servicio que ya esta presente en eventos futuros.', {
      tone: 'error',
    })
    return
  }

  void removeCatalogService(organizerServiceId)
}

function buildEventPayload() {
  // Construyo y valido el payload antes de llamar al backend para mostrar errores inmediatos.
  if (eventForm.trackId === '') {
    throw new Error('Selecciona un circuito para el evento.')
  }

  if (eventForm.eventDate.trim() === '') {
    throw new Error('Selecciona la fecha del evento.')
  }

  const basePrice = Number(eventForm.basePrice)
  if (!Number.isFinite(basePrice) || basePrice <= 0) {
    throw new Error('Introduce un precio base valido.')
  }

  const maxParticipants = Number(eventForm.maxParticipants)
  if (!Number.isInteger(maxParticipants) || maxParticipants <= 0) {
    throw new Error('Introduce un aforo máximo válido.')
  }

  const description = eventForm.description.trim()
  if (description === '') {
    throw new Error('La descripción del evento es obligatoria.')
  }

  const services = [
    ...selectedTrackServiceIds.value.map((trackServiceId) => ({
      trackServiceId,
      price: parseServicePrice('track', trackServiceId),
    })),
    ...selectedOrganizerServiceIds.value.map((organizerServiceId) => ({
      organizerServiceId,
      price: parseServicePrice('organizer', organizerServiceId),
    })),
  ]

  return {
    trackId: Number(eventForm.trackId),
    eventDate: eventForm.eventDate,
    basePrice,
    maxParticipants,
    description,
    services,
  }
}

function parseServicePrice(kind: 'track' | 'organizer', id: number): number {
  // Cada servicio seleccionado debe tener precio propio dentro del evento.
  const value = Number(servicePriceInputs[toServiceKey(kind, id)])

  if (!Number.isFinite(value) || value <= 0) {
    throw new Error('Todos los servicios seleccionados deben tener un precio valido.')
  }

  return value
}

async function saveEvent() {
  eventSaving.value = true
  eventError.value = ''

  let payload

  try {
    // Si falla la validación, no salgo al backend.
    payload = buildEventPayload()
  } catch (validationError) {
    eventError.value =
      validationError instanceof Error ? validationError.message : 'Revisa los datos del evento.'
    eventSaving.value = false
    return
  }

  try {
    workspace.value =
      editingEventId.value === null
        ? await createOrganizerEvent(payload)
        : await updateOrganizerEvent(editingEventId.value, payload)

    toast.showToast(
      editingEventId.value === null
        ? 'El evento se ha creado correctamente.'
        : 'El evento se ha actualizado correctamente.',
    )
    hideEventModal()
  } catch (requestError) {
    eventError.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo guardar el evento.',
      matches: [
        {
          includes: 'already been purchased',
          message:
            'No puedes retirar un servicio del evento si ya ha sido contratado por asistentes.',
        },
        {
          includes: 'same service cannot be added twice',
          message: 'No puedes incluir el mismo servicio dos veces en un evento.',
        },
        {
          includes: 'track cannot be changed',
          message: 'No puedes cambiar el circuito de un evento que ya existe.',
        },
        {
          includes: 'date cannot be changed',
          message: 'No puedes cambiar la fecha de un evento que ya existe.',
        },
        {
          includes: 'track id',
          message: 'Ya existe un evento para ese circuito en la misma fecha.',
        },
        {
          includes: 'current bookings',
          message: 'No puedes bajar el aforo por debajo de las reservas ya confirmadas.',
        },
      ],
    })
  } finally {
    eventSaving.value = false
  }
}

async function deleteEvent() {
  if (!eventPendingDeletion.value) {
    return
  }

  // La confirmación vive en modal para evitar borrados accidentales.
  deletingEventId.value = eventPendingDeletion.value.event.id
  eventError.value = ''

  try {
    workspace.value = await deleteOrganizerEvent(eventPendingDeletion.value.event.id)
    toast.showToast('El evento se ha eliminado correctamente.')
    isDeleteEventModalOpen.value = false
    eventPendingDeletion.value = null
  } catch (requestError) {
    eventError.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo eliminar el evento.',
      matches: [
        {
          includes: 'future events can be deleted',
          message: 'Solo puedes eliminar eventos futuros.',
        },
        {
          includes: 'already has bookings',
          message: 'No puedes eliminar un evento que ya tiene reservas.',
        },
      ],
    })
  } finally {
    deletingEventId.value = null
  }
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Organización</p>
      <h1 class="ui-title-section">Preparando tu area de trabajo</h1>
      <p class="ui-copy-muted">Estamos cargando tu catálogo, tus eventos y las estadísticas.</p>
    </section>

    <section v-else-if="error" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Organización</p>
      <h1 class="ui-title-section">Acceso restringido</h1>
      <p class="ui-copy-muted">{{ error }}</p>
    </section>

    <template v-else-if="workspace">
      <PageHero
        eyebrow="Organizador"
        title="Mi organización"
        :description="`Gestiona el catálogo y los eventos de ${workspace.organizer.legalName}.`"
      />

      <section class="organizer-metrics">
        <MetricCard
          label="Bruto total"
          :value="formatCurrency(workspace.stats.totalGrossRevenue)"
          hint="Base y servicios vendidos"
          tone="accent"
        />
        <MetricCard
          label="Asistencias"
          :value="String(workspace.stats.totalBookings)"
          hint="Reservas confirmadas en tus eventos"
        />
        <MetricCard
          label="Servicios vendidos"
          :value="String(workspace.stats.totalSoldServices)"
          hint="Contrataciones adicionales cerradas"
          tone="success"
        />
        <MetricCard
          label="Eventos futuros"
          :value="String(workspace.stats.futureEvents)"
          hint="Jornadas pendientes de celebrarse"
        />
      </section>

      <section class="panel panel-pad-lg panel-stack-lg">
        <div class="pill-tabs" role="tablist" aria-label="Panel del organizador">
          <button
            v-for="tab in tabItems"
            :key="tab.id"
            class="pill-tab"
            :class="{ 'pill-tab--active': activeTab === tab.id }"
            type="button"
            @click="activeTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <OrganizerServicesPanel
          v-if="activeTab === 'services'"
          :service-error="serviceError"
          :available-catalog-services="availableCatalogServices"
          :selected-available-service-id="selectedAvailableServiceId"
          :service-submitting="serviceSubmitting"
          :organizer-services="workspace.organizerServices"
          :removing-organizer-service-id="removingOrganizerServiceId"
          :is-organizer-service-locked="isOrganizerServiceLocked"
          @update:selected-available-service-id="selectedAvailableServiceId = $event"
          @add-service="addCatalogService"
          @remove-service="requestCatalogServiceRemoval"
        />

        <OrganizerEventsPanel
          v-else-if="activeTab === 'events'"
          :event-error="eventError"
          :events="sortedEvents"
          :today-iso="todayIso"
          @create="openCreateEventModal"
          @edit="openEditEventModal"
          @delete="openDeleteEventModal"
        />

        <OrganizerStatsPanel
          v-else
          :stats="workspace.stats"
          :revenue-chart-bars="revenueChartBars"
        />
      </section>
    </template>

    <AppModal
      :is-open="isEventModalOpen"
      :ariaLabel="editingEventId === null ? 'Crear evento' : 'Editar evento'"
      eyebrow="Eventos"
      :title="editingEventId === null ? 'Nuevo evento' : 'Editar evento'"
      width="920px"
      @close="closeEventModal"
    >
      <form class="surface-form-grid" @submit.prevent="saveEvent">
        <p v-if="eventError" class="status-message status-message--error">{{ eventError }}</p>

        <label class="surface-field">
          <span class="surface-field__label">Circuito</span>
          <select v-model="eventForm.trackId" :disabled="editingEventId !== null">
            <option value="">Selecciona un circuito</option>
            <option
              v-for="track in workspace?.tracks ?? []"
              :key="track.id"
              :value="String(track.id)"
            >
              {{ track.name }}
            </option>
          </select>
        </label>

        <label class="surface-field">
          <span class="surface-field__label">Fecha</span>
          <input
            v-model="eventForm.eventDate"
            type="date"
            :min="todayIso"
            :disabled="editingEventId !== null"
          />
        </label>

        <label class="surface-field">
          <span class="surface-field__label">Precio base</span>
          <input v-model="eventForm.basePrice" type="number" min="0.01" step="0.01" />
        </label>

        <label class="surface-field">
          <span class="surface-field__label">Aforo máximo</span>
          <input
            v-model="eventForm.maxParticipants"
            type="number"
            :min="String(minimumParticipantsForEdit)"
            step="1"
          />
        </label>

        <label class="surface-field surface-field--full">
          <span class="surface-field__label">Descripción</span>
          <textarea v-model="eventForm.description" rows="5" maxlength="5000"></textarea>
        </label>

        <p v-if="editingManagedEvent" class="ui-copy-muted organizer-form__hint surface-field--full">
          El circuito y la fecha quedan bloqueados tras crear el evento. El aforo no puede bajar de
          {{ minimumParticipantsForEdit }} reservas confirmadas.
        </p>

        <section class="organizer-form__section surface-field--full">
          <div class="panel-copy">
            <h3 class="ui-title-card organizer-form__section-title">Servicios del circuito</h3>
            <p class="ui-copy-muted">
              Activa solo los servicios de pista que quieras ofrecer en este evento.
            </p>
          </div>

          <p v-if="currentTrackServiceOptions.length === 0" class="ui-copy-muted">
            Este circuito no tiene servicios de pista configurados.
          </p>

          <div v-else class="organizer-service-option-list">
            <label
              v-for="service in currentTrackServiceOptions"
              :key="service.id"
              class="organizer-service-option"
              :class="{ 'organizer-service-option--locked': isTrackServiceLockedForEdit(service.id) }"
            >
              <div class="organizer-service-option__copy">
                <input
                  v-model="selectedTrackServiceIds"
                  type="checkbox"
                  :value="service.id"
                  :disabled="isTrackServiceLockedForEdit(service.id)"
                />
                <span>{{ service.serviceName }}</span>
                <span v-if="isTrackServiceLockedForEdit(service.id)" class="ui-copy-muted">
                  Ya contratado
                </span>
              </div>

              <input
                v-if="selectedTrackServiceIds.includes(service.id)"
                v-model="servicePriceInputs[toServiceKey('track', service.id)]"
                type="number"
                min="0.01"
                step="0.01"
                placeholder="Precio"
              />
            </label>
          </div>
        </section>

        <section class="organizer-form__section surface-field--full">
          <div class="panel-copy">
            <h3 class="ui-title-card organizer-form__section-title">Servicios del organizador</h3>
            <p class="ui-copy-muted">
              Define qu&eacute; extras propios quieres vender y con qu&eacute; precio en este evento.
            </p>
          </div>

          <p v-if="organizerServiceOptions.length === 0" class="ui-copy-muted">
            Primero añade servicios a tu catálogo para poder ofertarlos aquí.
          </p>

          <div v-else class="organizer-service-option-list">
            <label
              v-for="service in organizerServiceOptions"
              :key="service.id"
              class="organizer-service-option"
              :class="{ 'organizer-service-option--locked': isOrganizerEventServiceLockedForEdit(service.id) }"
            >
              <div class="organizer-service-option__copy">
                <input
                  v-model="selectedOrganizerServiceIds"
                  type="checkbox"
                  :value="service.id"
                  :disabled="isOrganizerEventServiceLockedForEdit(service.id)"
                />
                <span>{{ service.serviceName }}</span>
                <span
                  v-if="isOrganizerEventServiceLockedForEdit(service.id)"
                  class="ui-copy-muted"
                >
                  Ya contratado
                </span>
              </div>

              <input
                v-if="selectedOrganizerServiceIds.includes(service.id)"
                v-model="servicePriceInputs[toServiceKey('organizer', service.id)]"
                type="number"
                min="0.01"
                step="0.01"
                placeholder="Precio"
              />
            </label>
          </div>
        </section>

        <div class="organizer-form__actions surface-field--full">
          <button class="action-button action-button--ghost" type="button" @click="closeEventModal">
            Cancelar
          </button>
          <button class="action-button" type="submit" :disabled="eventSaving">
            {{ eventSaving ? 'Guardando...' : editingEventId === null ? 'Crear evento' : 'Guardar cambios' }}
          </button>
        </div>
      </form>
    </AppModal>

    <AppModal
      :is-open="isDeleteEventModalOpen"
      ariaLabel="Eliminar evento"
      eyebrow="Eventos"
      title="Eliminar evento"
      width="560px"
      @close="closeDeleteEventModal"
    >
      <div class="panel-copy panel-stack-md">
        <p v-if="eventError" class="status-message status-message--error">{{ eventError }}</p>
        <p class="ui-copy-muted">
          Vas a eliminar <strong>{{ eventPendingDeletion?.event.trackName }}</strong> del
          <strong>{{
            eventPendingDeletion ? formatDisplayDate(eventPendingDeletion.event.eventDate) : ''
          }}</strong>.
        </p>
        <p class="ui-copy-muted">
          Solo se puede eliminar si el evento sigue siendo futuro y todavía no tiene ninguna
          reserva.
        </p>
      </div>

      <div class="organizer-form__actions">
        <button class="action-button action-button--ghost" type="button" @click="closeDeleteEventModal">
          Cancelar
        </button>
        <button class="action-button" type="button" :disabled="deletingEventId !== null" @click="deleteEvent">
          {{ deletingEventId !== null ? 'Eliminando...' : 'Eliminar evento' }}
        </button>
      </div>
    </AppModal>
  </main>
</template>

<style scoped>
.organizer-metrics {
  display: grid;
  gap: var(--space-lg);
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.organizer-form__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
}

.organizer-form__section {
  display: grid;
  gap: var(--space-md);
}

.organizer-form__hint {
  margin-top: calc(-1 * var(--space-xs));
}

.organizer-form__section-title {
  font-size: var(--fs-title-info);
}

.organizer-service-option-list {
  display: grid;
  gap: var(--space-sm);
}

.organizer-service-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: 14px 16px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass);
}

.organizer-service-option--locked {
  border-color: rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.04);
}

.organizer-service-option__copy {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-sm);
  font-weight: 600;
}

.organizer-service-option input[type='checkbox'] {
  accent-color: var(--accent);
}

.organizer-service-option input[type='number'] {
  width: 140px;
  min-height: 44px;
  padding: 0 12px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  color: var(--text-body);
  background: rgba(18, 18, 20, 0.55);
}

.organizer-service-option input[type='number']:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

@media (max-width: 980px) {
  .organizer-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .organizer-metrics,
  .surface-form-grid {
    grid-template-columns: 1fr;
  }

  .organizer-form__actions,
  .organizer-service-option {
    flex-direction: column;
    align-items: stretch;
  }

  .organizer-form__actions .action-button,
  .organizer-service-option input[type='number'] {
    width: 100%;
  }
}
</style>
