<script setup lang="ts">
import { Pencil, Plus, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AppModal from '@/components/AppModal.vue'
import MetricCard from '@/components/MetricCard.vue'
import PageHero from '@/components/PageHero.vue'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'
import {
  addOrganizerCatalogService,
  createOrganizerEvent,
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

const eventForm = reactive({
  trackId: '',
  eventDate: '',
  basePrice: '',
  maxParticipants: '',
  description: '',
})

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
  [...(workspace.value?.events ?? [])].sort((left, right) => {
    const leftPast = isPastEvent(left.event.eventDate)
    const rightPast = isPastEvent(right.event.eventDate)

    if (leftPast !== rightPast) {
      return leftPast ? 1 : -1
    }

    return leftPast
      ? right.event.eventDate.localeCompare(left.event.eventDate)
      : left.event.eventDate.localeCompare(right.event.eventDate)
  }),
)

const availableCatalogServices = computed(() => {
  if (!workspace.value) {
    return []
  }

  const assignedIds = new Set(workspace.value.organizerServices.map((service) => service.serviceId))
  return workspace.value.availableServices.filter((service) => !assignedIds.has(service.id))
})

const currentTrackServiceOptions = computed(() => {
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

onMounted(async () => {
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
    workspace.value = await getOrganizerWorkspace()
  } catch (requestError) {
    error.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo cargar tu area de organizacion.',
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

  isEventModalOpen.value = false
  resetEventForm()
}

function openCreateEventModal() {
  resetEventForm()
  isEventModalOpen.value = true
}

function openEditEventModal(managedEvent: OrganizerManagedEvent) {
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

function isPastEvent(eventDate: string): boolean {
  return eventDate <= todayIso.value
}

function isOrganizerServiceLocked(organizerServiceId: number): boolean {
  return futureOrganizerServiceIdsInUse.value.has(organizerServiceId)
}

function toServiceKey(kind: 'track' | 'organizer', id: number): string {
  return `${kind}:${id}`
}

function getServicePriceInput(kind: 'track' | 'organizer', id: number): string {
  return servicePriceInputs[toServiceKey(kind, id)] ?? ''
}

async function addCatalogService() {
  if (selectedAvailableServiceId.value === '') {
    serviceError.value = 'Selecciona un servicio para anadirlo al catalogo.'
    return
  }

  serviceSubmitting.value = true
  serviceError.value = ''

  try {
    workspace.value = await addOrganizerCatalogService(Number(selectedAvailableServiceId.value))
    selectedAvailableServiceId.value = ''
    toast.showToast('El servicio se ha anadido a tu catalogo.')
  } catch (requestError) {
    serviceError.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo anadir el servicio al catalogo.',
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
    toast.showToast('El servicio se ha retirado de tu catalogo.')
  } catch (requestError) {
    serviceError.value = resolveApiErrorMessage(requestError, {
      fallback: 'No se pudo retirar el servicio del catalogo.',
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

function buildEventPayload() {
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
    throw new Error('Introduce un aforo maximo valido.')
  }

  const description = eventForm.description.trim()
  if (description === '') {
    throw new Error('La descripcion del evento es obligatoria.')
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
    closeEventModal()
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
          includes: 'track id',
          message: 'Ya existe un evento para ese circuito en la misma fecha.',
        },
      ],
    })
  } finally {
    eventSaving.value = false
  }
}

function formatEventServiceName(service: OrganizerManagedEvent['services'][number]): string {
  return service.trackServiceName ?? service.organizerServiceName ?? ''
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Organizacion</p>
      <h1 class="ui-title-section">Preparando tu area de trabajo</h1>
      <p class="ui-copy-muted">Estamos cargando tu catalogo, tus eventos y las estadisticas.</p>
    </section>

    <section v-else-if="error" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Organizacion</p>
      <h1 class="ui-title-section">Acceso restringido</h1>
      <p class="ui-copy-muted">{{ error }}</p>
    </section>

    <template v-else-if="workspace">
      <PageHero
        eyebrow="Organizador"
        title="Mi organizacion"
        :description="`Gestiona el catalogo y los eventos de ${workspace.organizer.legalName}.`"
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

        <section v-if="activeTab === 'services'" class="panel-stack-lg">
          <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

          <article class="panel panel-pad-lg panel-stack-md">
            <div class="organizer-section-header">
              <div class="panel-copy">
                <h3 class="ui-title-card">Catalogo de servicios del organizador</h3>
                <p class="ui-copy-muted">
                  Anade servicios globales a tu oferta y retira solo los que no esten ligados a eventos futuros.
                </p>
              </div>
            </div>

            <div class="organizer-toolbar">
              <label class="organizer-field">
                <span>Servicio disponible</span>
                <select v-model="selectedAvailableServiceId">
                  <option value="">Selecciona un servicio</option>
                  <option
                    v-for="service in availableCatalogServices"
                    :key="service.id"
                    :value="service.id"
                  >
                    {{ service.name }}
                  </option>
                </select>
              </label>

              <button
                class="action-button organizer-toolbar__button"
                type="button"
                :disabled="serviceSubmitting || selectedAvailableServiceId === ''"
                @click="addCatalogService"
              >
                <Plus :size="16" aria-hidden="true" />
                Anadir servicio
              </button>
            </div>

            <p v-if="workspace.organizerServices.length === 0" class="ui-copy-muted">
              Todavia no has anadido servicios a tu catalogo.
            </p>

            <div v-else class="organizer-card-list">
              <article
                v-for="service in workspace.organizerServices"
                :key="service.id"
                class="organizer-card"
              >
                <div class="panel-copy organizer-card__copy">
                  <h4 class="ui-title-card organizer-card__title">{{ service.serviceName }}</h4>
                  <p v-if="isOrganizerServiceLocked(service.id)" class="ui-copy-muted">
                    Vinculado a eventos futuros
                  </p>
                </div>

                <button
                  class="icon-button icon-button--danger"
                  type="button"
                  :disabled="removingOrganizerServiceId === service.id || isOrganizerServiceLocked(service.id)"
                  aria-label="Retirar servicio del catalogo"
                  title="Retirar servicio del catalogo"
                  @click="removeCatalogService(service.id)"
                >
                  <Trash2 :size="16" aria-hidden="true" />
                </button>
              </article>
            </div>
          </article>
        </section>

        <section v-else-if="activeTab === 'events'" class="panel-stack-lg">
          <p v-if="eventError" class="status-message status-message--error">{{ eventError }}</p>

          <article class="panel panel-pad-lg panel-stack-md">
            <div class="organizer-section-header">
              <div class="panel-copy">
                <h3 class="ui-title-card">Eventos del organizador</h3>
                <p class="ui-copy-muted">
                  Crea jornadas nuevas y ajusta precio base, aforo y servicios ofertados en cada evento.
                </p>
              </div>

              <button class="action-button organizer-toolbar__button" type="button" @click="openCreateEventModal">
                <Plus :size="16" aria-hidden="true" />
                Crear evento
              </button>
            </div>

            <p v-if="sortedEvents.length === 0" class="ui-copy-muted">
              Todavia no has creado ningun evento.
            </p>

            <div v-else class="organizer-card-list">
              <article
                v-for="managedEvent in sortedEvents"
                :key="managedEvent.event.id"
                class="organizer-card organizer-card--event"
              >
                <div class="panel-copy organizer-card__copy">
                  <p class="ui-eyebrow">{{ formatDisplayDate(managedEvent.event.eventDate) }}</p>
                  <h4 class="ui-title-card organizer-card__title">
                    {{ managedEvent.event.trackName }}
                  </h4>
                  <p class="ui-copy-muted">
                    Desde {{ formatCurrency(managedEvent.event.basePrice) }} ·
                    {{ managedEvent.stats.bookings }} asistentes ·
                    {{ formatCurrency(managedEvent.stats.grossRevenue) }} brutos
                  </p>

                  <div class="meta-pills">
                    <span class="meta-pill">
                      {{ managedEvent.stats.remainingCapacity }} / {{ managedEvent.stats.totalCapacity }} plazas libres
                    </span>
                    <span class="meta-pill">
                      {{ managedEvent.services.length }} servicios configurados
                    </span>
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
                      {{ formatEventServiceName(service) }} · {{ formatCurrency(service.price) }}
                    </span>
                  </div>
                </div>

                <button
                  class="icon-button icon-button--danger"
                  type="button"
                  :disabled="isPastEvent(managedEvent.event.eventDate)"
                  aria-label="Editar evento"
                  title="Editar evento"
                  @click="openEditEventModal(managedEvent)"
                >
                  <Pencil :size="16" aria-hidden="true" />
                </button>
              </article>
            </div>
          </article>
        </section>

        <section v-else class="panel-stack-lg">
          <article class="panel panel-pad-lg panel-stack-lg">
            <div class="info-grid info-grid--two">
              <article class="info-tile">
                <p class="info-tile__label">Beneficio bruto total</p>
                <strong class="ui-title-inline-price">
                  {{ formatCurrency(workspace.stats.totalGrossRevenue) }}
                </strong>
                <p class="info-tile__body">
                  {{ formatCurrency(workspace.stats.totalBaseRevenue) }} en plazas +
                  {{ formatCurrency(workspace.stats.totalServiceRevenue) }} en servicios
                </p>
              </article>

              <article class="info-tile">
                <p class="info-tile__label">Capacidad restante</p>
                <strong class="ui-title-inline-price">
                  {{ workspace.stats.totalRemainingCapacity }}
                </strong>
                <p class="info-tile__body">
                  Sobre un total agregado de {{ workspace.stats.totalCapacity }} plazas.
                </p>
              </article>
            </div>

            <div class="panel-copy">
              <h3 class="ui-title-card">Rendimiento por evento</h3>
              <p class="ui-copy-muted">
                Lectura rapida del comportamiento comercial de cada jornada.
              </p>
            </div>

            <div v-if="workspace.stats.eventStats.length === 0" class="ui-copy-muted">
              Todavia no hay eventos que analizar.
            </div>

            <div v-else class="list-divider">
              <article
                v-for="eventStat in workspace.stats.eventStats"
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
          </article>
        </section>
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
      <form class="organizer-form" @submit.prevent="saveEvent">
        <p v-if="eventError" class="status-message status-message--error">{{ eventError }}</p>

        <label class="organizer-field">
          <span>Circuito</span>
          <select v-model="eventForm.trackId">
            <option value="">Selecciona un circuito</option>
            <option v-for="track in workspace?.tracks ?? []" :key="track.id" :value="String(track.id)">
              {{ track.name }}
            </option>
          </select>
        </label>

        <label class="organizer-field">
          <span>Fecha</span>
          <input v-model="eventForm.eventDate" type="date" :min="todayIso" />
        </label>

        <label class="organizer-field">
          <span>Precio base</span>
          <input v-model="eventForm.basePrice" type="number" min="0.01" step="0.01" />
        </label>

        <label class="organizer-field">
          <span>Aforo maximo</span>
          <input v-model="eventForm.maxParticipants" type="number" min="1" step="1" />
        </label>

        <label class="organizer-field organizer-field--full">
          <span>Descripcion</span>
          <textarea v-model="eventForm.description" rows="5" maxlength="5000"></textarea>
        </label>

        <section class="organizer-form__section organizer-field--full">
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
            >
              <div class="organizer-service-option__copy">
                <input
                  v-model="selectedTrackServiceIds"
                  type="checkbox"
                  :value="service.id"
                />
                <span>{{ service.serviceName }}</span>
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

        <section class="organizer-form__section organizer-field--full">
          <div class="panel-copy">
            <h3 class="ui-title-card organizer-form__section-title">Servicios del organizador</h3>
            <p class="ui-copy-muted">
              Define qu&eacute; extras propios quieres vender y con qu&eacute; precio en este evento.
            </p>
          </div>

          <p v-if="organizerServiceOptions.length === 0" class="ui-copy-muted">
            Primero anade servicios a tu catalogo para poder ofertarlos aqui.
          </p>

          <div v-else class="organizer-service-option-list">
            <label
              v-for="service in organizerServiceOptions"
              :key="service.id"
              class="organizer-service-option"
            >
              <div class="organizer-service-option__copy">
                <input
                  v-model="selectedOrganizerServiceIds"
                  type="checkbox"
                  :value="service.id"
                />
                <span>{{ service.serviceName }}</span>
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

        <div class="organizer-form__actions">
          <button class="action-button action-button--ghost" type="button" @click="closeEventModal">
            Cancelar
          </button>
          <button class="action-button" type="submit" :disabled="eventSaving">
            {{ eventSaving ? 'Guardando...' : editingEventId === null ? 'Crear evento' : 'Guardar cambios' }}
          </button>
        </div>
      </form>
    </AppModal>
  </main>
</template>

<style scoped>
.organizer-metrics {
  display: grid;
  gap: var(--space-lg);
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.organizer-section-header,
.organizer-toolbar,
.organizer-form__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
}

.organizer-toolbar {
  align-items: end;
}

.organizer-toolbar__button {
  flex: none;
}

.organizer-card-list {
  display: grid;
  gap: var(--space-md);
}

.organizer-card {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-lg);
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.organizer-card--event {
  align-items: center;
}

.organizer-card__copy {
  min-width: 0;
}

.organizer-card__title {
  font-size: var(--fs-title-info);
}

.organizer-field {
  display: grid;
  gap: var(--space-xs);
}

.organizer-field--full {
  grid-column: 1 / -1;
}

.organizer-field span {
  color: var(--text-muted);
  font-size: var(--fs-caption);
  font-weight: 700;
}

.organizer-field input,
.organizer-field select,
.organizer-field textarea {
  width: 100%;
  min-height: 50px;
  padding: 12px 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  color: var(--text-body);
  background: var(--surface-glass);
}

.organizer-field select {
  appearance: none;
  background:
    linear-gradient(180deg, rgba(39, 16, 16, 0.98) 0%, rgba(24, 10, 10, 0.98) 100%);
}

.organizer-field select option {
  color: var(--text-strong);
  background: #1b0c0c;
}

.organizer-field textarea {
  resize: vertical;
  min-height: 150px;
}

.organizer-field input:focus,
.organizer-field select:focus,
.organizer-field textarea:focus,
.organizer-service-option input[type='number']:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

.organizer-form {
  display: grid;
  gap: var(--space-md);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.organizer-form__section {
  display: grid;
  gap: var(--space-md);
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

.organizer-service-option__copy {
  display: flex;
  align-items: center;
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

.organizer-stats-row__metrics {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-lg);
  color: var(--text-muted);
}

@media (max-width: 980px) {
  .organizer-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .organizer-metrics,
  .organizer-form {
    grid-template-columns: 1fr;
  }

  .organizer-card,
  .organizer-section-header,
  .organizer-toolbar,
  .organizer-form__actions,
  .organizer-service-option,
  .organizer-stats-row__metrics {
    flex-direction: column;
    align-items: stretch;
  }

  .organizer-field--full {
    grid-column: auto;
  }

  .organizer-toolbar__button,
  .organizer-form__actions .action-button,
  .organizer-service-option input[type='number'] {
    width: 100%;
  }
}
</style>
