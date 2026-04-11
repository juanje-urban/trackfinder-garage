<script setup lang="ts">
import { isAxiosError } from 'axios'
import { Check, Pencil, Power, PowerOff, Trash2, X } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import PageHero from '@/components/PageHero.vue'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'
import { deleteOrganizer, enableOrganizer, getOrganizers } from '@/services/organizerService'
import {
  createServiceCatalogItem,
  disableServiceCatalogItem,
  enableServiceCatalogItem,
  getServices,
  updateServiceCatalogItem,
} from '@/services/serviceCatalogService'
import {
  createTrackServiceAssignment,
  deleteTrackServiceAssignment,
  getTrackServiceAssignments,
} from '@/services/trackServiceAssignmentService'
import { createTrack, getTracks, updateTrack } from '@/services/trackService'
import { disableUser, enableUser, getUsers } from '@/services/userService'
import type { Organizer } from '@/types/organizer'
import type { ServiceCatalogItem, ServiceCatalogPayload } from '@/types/serviceCatalog'
import type { TrackServiceAssignment } from '@/types/trackService'
import type { Track, TrackPayload } from '@/types/track'
import type { AdminUser } from '@/types/user'

type AdminTab = 'organizers' | 'tracks' | 'services' | 'users'

const auth = useAuth()
const toast = useToast()

const loading = ref(true)
const error = ref('')
const activeTab = ref<AdminTab>('organizers')

const organizers = ref<Organizer[]>([])
const tracks = ref<Track[]>([])
const services = ref<ServiceCatalogItem[]>([])
const trackServiceAssignments = ref<TrackServiceAssignment[]>([])
const users = ref<AdminUser[]>([])

const organizerError = ref('')
const trackError = ref('')
const serviceError = ref('')
const userError = ref('')

const organizerBusyId = ref<number | null>(null)
const trackSaving = ref(false)
const editingTrackId = ref<number | null>(null)
const serviceSaving = ref(false)
const serviceBusyId = ref<number | null>(null)
const editingServiceId = ref<number | null>(null)
const assignmentSaving = ref(false)
const assignmentDeletingId = ref<number | null>(null)
const userBusyId = ref<number | null>(null)

const selectedTrackId = ref<number | ''>('')
const selectedServiceId = ref<number | ''>('')

const trackForm = reactive<TrackPayload>({
  name: '',
  location: '',
  description: '',
})

const serviceForm = reactive<ServiceCatalogPayload>({
  name: '',
  description: '',
  allowedForTrack: true,
  allowedForOrganizer: false,
})

const isAdmin = computed(() => auth.session.value?.roleName === 'ADMIN')

const pendingOrganizers = computed(() =>
  [...organizers.value]
    .filter((organizer) => !organizer.organizerEnabled)
    .sort((left, right) => left.legalName.localeCompare(right.legalName)),
)

const sortedTracks = computed(() =>
  [...tracks.value].sort((left, right) => left.name.localeCompare(right.name)),
)

const sortedServices = computed(() =>
  [...services.value].sort((left, right) => left.name.localeCompare(right.name)),
)

const sortedUsers = computed(() =>
  [...users.value].sort((left, right) => left.displayName.localeCompare(right.displayName)),
)

const disabledUsersCount = computed(() => users.value.filter((user) => !user.enabled).length)
const trackLinkedServicesCount = computed(() => trackServiceAssignments.value.length)

const selectedTrackAssignments = computed(() => {
  if (!selectedTrackId.value) {
    return []
  }

  return [...trackServiceAssignments.value]
    .filter((assignment) => assignment.trackId === Number(selectedTrackId.value))
    .sort((left, right) => left.serviceName.localeCompare(right.serviceName))
})

const assignableServices = computed(() => {
  const assignedServiceIds = new Set(selectedTrackAssignments.value.map((assignment) => assignment.serviceId))

  return sortedServices.value.filter(
    (service) =>
      service.allowedForTrack && service.enabled && !assignedServiceIds.has(service.id),
  )
})

const tabItems: Array<{ id: AdminTab; label: string }> = [
  { id: 'organizers', label: 'Solicitudes' },
  { id: 'tracks', label: 'Circuitos' },
  { id: 'services', label: 'Servicios' },
  { id: 'users', label: 'Usuarios' },
]

onMounted(async () => {
  if (!isAdmin.value) {
    loading.value = false
    error.value = 'Esta area esta reservada para cuentas con rol administrador.'
    return
  }

  await loadAdminPage()
})

async function loadAdminPage() {
  loading.value = true
  error.value = ''

  try {
    const [nextOrganizers, nextTracks, nextServices, nextAssignments, nextUsers] = await Promise.all([
      getOrganizers(),
      getTracks(),
      getServices(),
      getTrackServiceAssignments(),
      getUsers(),
    ])

    organizers.value = nextOrganizers
    tracks.value = nextTracks
    services.value = nextServices
    trackServiceAssignments.value = nextAssignments
    users.value = nextUsers

    const firstTrack = nextTracks[0]

    if (!selectedTrackId.value && firstTrack) {
      selectedTrackId.value = firstTrack.id
    }
  } catch {
    error.value = 'No se pudo cargar el panel de administracion.'
  } finally {
    loading.value = false
  }
}

function resetTrackForm() {
  editingTrackId.value = null
  trackForm.name = ''
  trackForm.location = ''
  trackForm.description = ''
  trackError.value = ''
}

function startTrackEdit(track: Track) {
  editingTrackId.value = track.id
  trackForm.name = track.name
  trackForm.location = track.location
  trackForm.description = track.description
  trackError.value = ''
}

async function saveTrack() {
  trackSaving.value = true
  trackError.value = ''

  try {
    const payload: TrackPayload = {
      name: trackForm.name.trim(),
      location: trackForm.location.trim(),
      description: trackForm.description.trim(),
    }

    const savedTrack =
      editingTrackId.value === null
        ? await createTrack(payload)
        : await updateTrack(editingTrackId.value, payload)

    tracks.value =
      editingTrackId.value === null
        ? [...tracks.value, savedTrack]
        : tracks.value.map((track) => (track.id === savedTrack.id ? savedTrack : track))

    if (!selectedTrackId.value) {
      selectedTrackId.value = savedTrack.id
    }

    toast.showToast(
      editingTrackId.value === null
        ? 'El circuito se ha creado correctamente.'
        : 'El circuito se ha actualizado correctamente.',
    )
    resetTrackForm()
  } catch (requestError) {
    trackError.value = resolveRequestError(requestError, 'No se pudo guardar el circuito.')
  } finally {
    trackSaving.value = false
  }
}

function resetServiceForm() {
  editingServiceId.value = null
  serviceForm.name = ''
  serviceForm.description = ''
  serviceForm.allowedForTrack = true
  serviceForm.allowedForOrganizer = false
  serviceError.value = ''
}

function startServiceEdit(service: ServiceCatalogItem) {
  editingServiceId.value = service.id
  serviceForm.name = service.name
  serviceForm.description = service.description
  serviceForm.allowedForTrack = service.allowedForTrack
  serviceForm.allowedForOrganizer = service.allowedForOrganizer
  serviceError.value = ''
}

async function saveService() {
  serviceSaving.value = true
  serviceError.value = ''

  try {
    const payload: ServiceCatalogPayload = {
      name: serviceForm.name.trim(),
      description: serviceForm.description.trim(),
      allowedForTrack: serviceForm.allowedForTrack,
      allowedForOrganizer: serviceForm.allowedForOrganizer,
    }

    const savedService =
      editingServiceId.value === null
        ? await createServiceCatalogItem(payload)
        : await updateServiceCatalogItem(editingServiceId.value, payload)

    services.value =
      editingServiceId.value === null
        ? [...services.value, savedService]
        : services.value.map((service) => (service.id === savedService.id ? savedService : service))

    toast.showToast(
      editingServiceId.value === null
        ? 'El servicio se ha creado correctamente.'
        : 'El servicio se ha actualizado correctamente.',
    )
    resetServiceForm()
  } catch (requestError) {
    serviceError.value = resolveRequestError(requestError, 'No se pudo guardar el servicio.')
  } finally {
    serviceSaving.value = false
  }
}

async function toggleServiceEnabled(service: ServiceCatalogItem) {
  serviceBusyId.value = service.id
  serviceError.value = ''

  try {
    const updatedService = service.enabled
      ? await disableServiceCatalogItem(service.id)
      : await enableServiceCatalogItem(service.id)

    services.value = services.value.map((currentService) =>
      currentService.id === updatedService.id ? updatedService : currentService,
    )
    toast.showToast(
      updatedService.enabled
        ? 'El servicio vuelve a estar activo.'
        : 'El servicio se ha deshabilitado.',
    )
  } catch (requestError) {
    serviceError.value = resolveRequestError(requestError, 'No se pudo actualizar el servicio.')
  } finally {
    serviceBusyId.value = null
  }
}

async function addTrackServiceAssignment() {
  if (!selectedTrackId.value || !selectedServiceId.value) {
    serviceError.value = 'Selecciona un circuito y un servicio para vincularlos.'
    return
  }

  assignmentSaving.value = true
  serviceError.value = ''

  try {
    const createdAssignment = await createTrackServiceAssignment({
      trackId: Number(selectedTrackId.value),
      serviceId: Number(selectedServiceId.value),
    })

    trackServiceAssignments.value = [...trackServiceAssignments.value, createdAssignment]
    selectedServiceId.value = ''
    toast.showToast('El servicio ya esta disponible para el circuito seleccionado.')
  } catch (requestError) {
    serviceError.value = resolveRequestError(
      requestError,
      'No se pudo vincular el servicio con el circuito.',
    )
  } finally {
    assignmentSaving.value = false
  }
}

async function removeTrackServiceAssignment(assignment: TrackServiceAssignment) {
  assignmentDeletingId.value = assignment.id
  serviceError.value = ''

  try {
    await deleteTrackServiceAssignment(assignment.id)
    trackServiceAssignments.value = trackServiceAssignments.value.filter(
      (currentAssignment) => currentAssignment.id !== assignment.id,
    )
    toast.showToast('El servicio se ha retirado del circuito.')
  } catch (requestError) {
    serviceError.value = resolveRequestError(
      requestError,
      'No se pudo eliminar el servicio del circuito.',
    )
  } finally {
    assignmentDeletingId.value = null
  }
}

async function approveOrganizerRequest(organizer: Organizer) {
  organizerBusyId.value = organizer.idUser
  organizerError.value = ''

  try {
    const updatedOrganizer = await enableOrganizer(organizer.idUser)
    organizers.value = organizers.value.map((currentOrganizer) =>
      currentOrganizer.idUser === updatedOrganizer.idUser ? updatedOrganizer : currentOrganizer,
    )
    toast.showToast('La solicitud se ha aceptado correctamente.')
  } catch (requestError) {
    organizerError.value = resolveRequestError(
      requestError,
      'No se pudo aceptar la solicitud del organizador.',
    )
  } finally {
    organizerBusyId.value = null
  }
}

async function denyOrganizerRequest(organizer: Organizer) {
  organizerBusyId.value = organizer.idUser
  organizerError.value = ''

  try {
    await deleteOrganizer(organizer.idUser)
    organizers.value = organizers.value.filter(
      (currentOrganizer) => currentOrganizer.idUser !== organizer.idUser,
    )
    toast.showToast('La solicitud se ha denegado y eliminado.')
  } catch (requestError) {
    organizerError.value = resolveRequestError(
      requestError,
      'No se pudo denegar la solicitud del organizador.',
    )
  } finally {
    organizerBusyId.value = null
  }
}

async function toggleUserEnabled(user: AdminUser) {
  userBusyId.value = user.id
  userError.value = ''

  try {
    const updatedUser = user.enabled ? await disableUser(user.id) : await enableUser(user.id)

    users.value = users.value.map((currentUser) =>
      currentUser.id === updatedUser.id ? updatedUser : currentUser,
    )
    toast.showToast(
      updatedUser.enabled ? 'La cuenta se ha habilitado.' : 'La cuenta se ha deshabilitado.',
    )
  } catch (requestError) {
    userError.value = resolveRequestError(requestError, 'No se pudo actualizar la cuenta.')
  } finally {
    userBusyId.value = null
  }
}

function resolveUserRoleLabel(user: AdminUser): string {
  const roleName = user.roleName?.toUpperCase() ?? ''

  if (roleName === 'ADMIN') {
    return 'Administrador'
  }

  if (roleName === 'ORGANIZER') {
    return 'Organizador'
  }

  return 'Usuario'
}

function resolveRequestError(requestError: unknown, fallbackMessage: string): string {
  if (!isAxiosError(requestError)) {
    return fallbackMessage
  }

  const backendMessage = requestError.response?.data?.error
  if (typeof backendMessage === 'string' && backendMessage.trim() !== '') {
    return backendMessage
  }

  if (requestError.response?.status === 403) {
    return 'No tienes permisos para realizar esta accion.'
  }

  return fallbackMessage
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Administraci&oacute;n</p>
      <h1 class="ui-title-section">Cargando centro de control</h1>
      <p class="ui-copy-muted">Estamos preparando los datos de organizadores, circuitos y usuarios.</p>
    </section>

    <section v-else-if="error" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Administraci&oacute;n</p>
      <h1 class="ui-title-section">Acceso restringido</h1>
      <p class="ui-copy-muted">{{ error }}</p>
      <RouterLink class="action-button" to="/">Volver al inicio</RouterLink>
    </section>

    <template v-else>
      <PageHero
        eyebrow="Centro de control"
        title="Administraci&oacute;n general"
        description="Gestiona solicitudes de organizador, circuitos, servicios y cuentas de usuario desde un unico panel operativo."
      >
        <template #aside>
          <article class="admin-hero-panel panel panel-pad-lg panel-stack-md">
            <div class="admin-hero-panel__row">
              <span>Solicitudes pendientes</span>
              <strong>{{ pendingOrganizers.length }}</strong>
            </div>
            <div class="admin-hero-panel__row">
              <span>Circuitos registrados</span>
              <strong>{{ tracks.length }}</strong>
            </div>
            <div class="admin-hero-panel__row">
              <span>Servicios vinculados</span>
              <strong>{{ trackLinkedServicesCount }}</strong>
            </div>
            <div class="admin-hero-panel__row">
              <span>Cuentas inactivas</span>
              <strong>{{ disabledUsersCount }}</strong>
            </div>
          </article>
        </template>
      </PageHero>

      <section class="admin-tabs panel panel-pad-lg panel-stack-lg">
        <div class="admin-tabs__nav" role="tablist" aria-label="Navegacion de administracion">
          <button
            v-for="tab in tabItems"
            :key="tab.id"
            class="admin-tabs__tab"
            :class="{ 'admin-tabs__tab--active': activeTab === tab.id }"
            type="button"
            @click="activeTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <section v-if="activeTab === 'organizers'" class="panel-stack-lg">
          <p v-if="organizerError" class="status-message status-message--error">{{ organizerError }}</p>

          <article class="panel panel-pad-lg panel-stack-md">
            <div class="panel-copy">
              <h3 class="ui-title-card">Solicitudes de organizador</h3>
              <p class="ui-copy-muted">
                Revisa las altas pendientes y decide si las aceptas o las deniegas.
              </p>
            </div>

            <p v-if="pendingOrganizers.length === 0" class="ui-copy-muted">
              No hay solicitudes pendientes ahora mismo.
            </p>

            <div v-else class="admin-list">
              <article
                v-for="organizer in pendingOrganizers"
                :key="organizer.idUser"
                class="admin-card"
              >
                <div class="panel-copy admin-card__copy">
                  <p class="ui-eyebrow">{{ organizer.legalName }}</p>
                  <h4 class="ui-title-card admin-card__title">{{ organizer.displayName }}</h4>
                  <p class="ui-copy-muted">
                    {{ organizer.name }} {{ organizer.surname }} · {{ organizer.email }}
                  </p>
                  <p class="ui-copy-muted">CIF: {{ organizer.cif }} · Telefono: {{ organizer.phone }}</p>
                </div>

                <div class="admin-card__actions">
                  <button
                    class="action-button admin-button admin-button--success"
                    type="button"
                    :disabled="organizerBusyId === organizer.idUser"
                    @click="approveOrganizerRequest(organizer)"
                  >
                    <Check :size="16" aria-hidden="true" />
                    Aceptar
                  </button>

                  <button
                    class="action-button action-button--ghost admin-button admin-button--danger"
                    type="button"
                    :disabled="organizerBusyId === organizer.idUser"
                    @click="denyOrganizerRequest(organizer)"
                  >
                    <X :size="16" aria-hidden="true" />
                    Denegar
                  </button>
                </div>
              </article>
            </div>
          </article>
        </section>

        <section v-else-if="activeTab === 'tracks'" class="panel-stack-lg">
          <p v-if="trackError" class="status-message status-message--error">{{ trackError }}</p>

          <div class="admin-grid">
            <article class="panel panel-pad-lg panel-stack-md">
              <div class="panel-copy">
                <h3 class="ui-title-card">Circuitos</h3>
                <p class="ui-copy-muted">Selecciona uno para editarlo o crea un alta nueva.</p>
              </div>

              <div class="admin-list">
                <article v-for="track in sortedTracks" :key="track.id" class="admin-card">
                  <div class="panel-copy admin-card__copy">
                    <p class="ui-eyebrow">{{ track.location }}</p>
                    <h4 class="ui-title-card admin-card__title">{{ track.name }}</h4>
                    <p class="ui-copy-muted">{{ track.description }}</p>
                  </div>

                  <button
                    class="action-button action-button--ghost admin-icon-button"
                    type="button"
                    aria-label="Editar circuito"
                    title="Editar circuito"
                    @click="startTrackEdit(track)"
                  >
                    <Pencil :size="16" aria-hidden="true" />
                  </button>
                </article>
              </div>
            </article>

            <article class="panel panel-pad-lg panel-stack-md">
              <div class="panel-copy">
                <h3 class="ui-title-card">
                  {{ editingTrackId === null ? 'Nuevo circuito' : 'Editar circuito' }}
                </h3>
              </div>

              <form class="admin-form" @submit.prevent="saveTrack">
                <label class="admin-field admin-field--full">
                  <span>Nombre</span>
                  <input v-model="trackForm.name" type="text" maxlength="255" />
                </label>

                <label class="admin-field admin-field--full">
                  <span>Ubicacion</span>
                  <input v-model="trackForm.location" type="text" maxlength="255" />
                </label>

                <label class="admin-field admin-field--full">
                  <span>Descripcion</span>
                  <textarea v-model="trackForm.description" rows="6" maxlength="500"></textarea>
                </label>

                <div class="admin-form__actions">
                  <button
                    v-if="editingTrackId !== null"
                    class="action-button action-button--ghost"
                    type="button"
                    @click="resetTrackForm"
                  >
                    Cancelar
                  </button>
                  <button class="action-button" type="submit" :disabled="trackSaving">
                    {{ trackSaving ? 'Guardando...' : editingTrackId === null ? 'Crear circuito' : 'Guardar cambios' }}
                  </button>
                </div>
              </form>
            </article>
          </div>
        </section>

        <section v-else-if="activeTab === 'services'" class="panel-stack-lg">
          <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

          <div class="admin-grid">
            <article class="panel panel-pad-lg panel-stack-md">
              <div class="panel-copy">
                <h3 class="ui-title-card">Catalogo de servicios</h3>
                <p class="ui-copy-muted">
                  Crea servicios nuevos, ajusta su alcance y activa o desactiva su disponibilidad.
                </p>
              </div>

              <div class="admin-list">
                <article v-for="service in sortedServices" :key="service.id" class="admin-card">
                  <div class="panel-copy admin-card__copy">
                    <div class="admin-inline-badges">
                      <span v-if="service.allowedForTrack" class="badge badge--soft">Circuito</span>
                      <span v-if="service.allowedForOrganizer" class="badge badge--soft">Organizador</span>
                      <span
                        class="badge"
                        :class="service.enabled ? 'badge--success' : 'badge--soft'"
                      >
                        {{ service.enabled ? 'Activo' : 'Inactivo' }}
                      </span>
                    </div>
                    <h4 class="ui-title-card admin-card__title">{{ service.name }}</h4>
                    <p class="ui-copy-muted">{{ service.description }}</p>
                  </div>

                  <div class="admin-card__actions">
                    <button
                      class="action-button action-button--ghost admin-icon-button"
                      type="button"
                      aria-label="Editar servicio"
                      title="Editar servicio"
                      @click="startServiceEdit(service)"
                    >
                      <Pencil :size="16" aria-hidden="true" />
                    </button>
                    <button
                      class="action-button action-button--ghost admin-icon-button"
                      :class="service.enabled ? 'admin-icon-button--danger' : 'admin-icon-button--success'"
                      type="button"
                      :disabled="serviceBusyId === service.id"
                      :aria-label="service.enabled ? 'Deshabilitar servicio' : 'Habilitar servicio'"
                      :title="service.enabled ? 'Deshabilitar servicio' : 'Habilitar servicio'"
                      @click="toggleServiceEnabled(service)"
                    >
                      <component :is="service.enabled ? PowerOff : Power" :size="16" aria-hidden="true" />
                    </button>
                  </div>
                </article>
              </div>
            </article>

            <article class="panel panel-pad-lg panel-stack-md">
              <div class="panel-copy">
                <h3 class="ui-title-card">
                  {{ editingServiceId === null ? 'Nuevo servicio' : 'Editar servicio' }}
                </h3>
              </div>

              <form class="admin-form" @submit.prevent="saveService">
                <label class="admin-field admin-field--full">
                  <span>Nombre</span>
                  <input v-model="serviceForm.name" type="text" maxlength="255" />
                </label>

                <label class="admin-field admin-field--full">
                  <span>Descripcion</span>
                  <textarea v-model="serviceForm.description" rows="6" maxlength="500"></textarea>
                </label>

                <label class="admin-checkbox">
                  <input v-model="serviceForm.allowedForTrack" type="checkbox" />
                  <span>Disponible para circuitos</span>
                </label>

                <label class="admin-checkbox">
                  <input v-model="serviceForm.allowedForOrganizer" type="checkbox" />
                  <span>Disponible para organizadores</span>
                </label>

                <div class="admin-form__actions">
                  <button
                    v-if="editingServiceId !== null"
                    class="action-button action-button--ghost"
                    type="button"
                    @click="resetServiceForm"
                  >
                    Cancelar
                  </button>
                  <button class="action-button" type="submit" :disabled="serviceSaving">
                    {{ serviceSaving ? 'Guardando...' : editingServiceId === null ? 'Crear servicio' : 'Guardar cambios' }}
                  </button>
                </div>
              </form>
            </article>
          </div>

          <article class="panel panel-pad-lg panel-stack-md">
            <div class="panel-copy">
              <h3 class="ui-title-card">Servicios ofrecidos por circuito</h3>
              <p class="ui-copy-muted">
                Vincula servicios activos a cada circuito y retira los que ya no deban mostrarse.
              </p>
            </div>

            <div class="admin-assignment-toolbar">
              <label class="admin-field">
                <span>Circuito</span>
                <select v-model="selectedTrackId">
                  <option value="">Selecciona un circuito</option>
                  <option v-for="track in sortedTracks" :key="track.id" :value="track.id">
                    {{ track.name }}
                  </option>
                </select>
              </label>

              <label class="admin-field">
                <span>Servicio</span>
                <select v-model="selectedServiceId" :disabled="assignableServices.length === 0">
                  <option value="">Selecciona un servicio</option>
                  <option v-for="service in assignableServices" :key="service.id" :value="service.id">
                    {{ service.name }}
                  </option>
                </select>
              </label>

              <button
                class="action-button admin-assignment-toolbar__button"
                type="button"
                :disabled="assignmentSaving || !selectedTrackId || !selectedServiceId"
                @click="addTrackServiceAssignment"
              >
                {{ assignmentSaving ? 'Vinculando...' : 'Anadir al circuito' }}
              </button>
            </div>

            <p v-if="selectedTrackAssignments.length === 0" class="ui-copy-muted">
              Este circuito todavia no tiene servicios vinculados.
            </p>

            <div v-else class="admin-list">
              <article
                v-for="assignment in selectedTrackAssignments"
                :key="assignment.id"
                class="admin-card admin-card--compact"
              >
                <div class="panel-copy admin-card__copy">
                  <h4 class="ui-title-card admin-card__title">{{ assignment.serviceName }}</h4>
                  <p class="ui-copy-muted">{{ assignment.trackName }}</p>
                </div>

                <button
                  class="action-button action-button--ghost admin-icon-button admin-icon-button--danger"
                  type="button"
                  :disabled="assignmentDeletingId === assignment.id"
                  aria-label="Eliminar servicio del circuito"
                  title="Eliminar servicio del circuito"
                  @click="removeTrackServiceAssignment(assignment)"
                >
                  <Trash2 :size="16" aria-hidden="true" />
                </button>
              </article>
            </div>
          </article>
        </section>

        <section v-else class="panel-stack-lg">
          <p v-if="userError" class="status-message status-message--error">{{ userError }}</p>

          <article class="panel panel-pad-lg panel-stack-md">
            <div class="panel-copy">
              <h3 class="ui-title-card">Gestion general de usuarios</h3>
              <p class="ui-copy-muted">
                Consulta sus datos y habilita o deshabilita la cuenta cuando sea necesario.
              </p>
            </div>

            <div class="admin-list">
              <article v-for="user in sortedUsers" :key="user.id" class="admin-card">
                <div class="panel-copy admin-card__copy">
                  <div class="admin-inline-badges">
                    <span class="badge badge--soft">{{ resolveUserRoleLabel(user) }}</span>
                    <span class="badge" :class="user.enabled ? 'badge--success' : 'badge--soft'">
                      {{ user.enabled ? 'Activa' : 'Inactiva' }}
                    </span>
                  </div>
                  <h4 class="ui-title-card admin-card__title">{{ user.displayName }}</h4>
                  <p class="ui-copy-muted">{{ user.name }} {{ user.surname }}</p>
                  <p class="ui-copy-muted">{{ user.email }} · {{ user.phone }}</p>
                  <p class="ui-copy-muted">{{ user.address }}</p>
                </div>

                <button
                  class="action-button"
                  :class="user.enabled ? 'admin-button admin-button--danger' : 'admin-button admin-button--success'"
                  type="button"
                  :disabled="userBusyId === user.id"
                  @click="toggleUserEnabled(user)"
                >
                  <component :is="user.enabled ? PowerOff : Power" :size="16" aria-hidden="true" />
                  {{ user.enabled ? 'Deshabilitar' : 'Habilitar' }}
                </button>
              </article>
            </div>
          </article>
        </section>
      </section>
    </template>
  </main>
</template>

<style scoped>
.admin-hero-panel {
  gap: var(--space-sm);
}

.admin-hero-panel__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  padding-bottom: var(--space-sm);
  border-bottom: 1px solid var(--line-soft);
  color: var(--text-body);
}

.admin-hero-panel__row:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.admin-hero-panel__row strong {
  color: var(--text-strong);
  font-size: var(--fs-title-info);
}

.admin-tabs__nav {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.admin-tabs__tab {
  min-height: 44px;
  padding: 0 18px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-pill);
  background: var(--surface-glass);
  color: var(--text-muted);
  font-weight: 700;
}

.admin-tabs__tab--active {
  border-color: transparent;
  background: var(--accent-gradient-horizontal);
  color: var(--text-strong);
  box-shadow: var(--accent-shadow);
}

.admin-grid {
  display: grid;
  align-items: start;
  gap: var(--space-lg);
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
}

.admin-list {
  display: grid;
  gap: var(--space-md);
}

.admin-card {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-lg);
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.admin-card--compact {
  align-items: center;
}

.admin-card__copy {
  min-width: 0;
}

.admin-card__title {
  font-size: var(--fs-title-info);
}

.admin-card__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-sm);
}

.admin-button {
  min-width: 136px;
}

.admin-button--success {
  background: linear-gradient(135deg, rgba(20, 150, 78, 0.94), rgba(11, 98, 49, 0.94));
  color: #f2fff5;
  box-shadow: 0 14px 30px rgba(11, 98, 49, 0.24);
}

.admin-button--danger {
  background: linear-gradient(135deg, rgba(214, 31, 31, 0.94), rgba(146, 12, 12, 0.94));
  color: #fff4f4;
  box-shadow: 0 14px 30px rgba(146, 12, 12, 0.24);
}

.admin-icon-button {
  min-width: 40px;
  min-height: 40px;
  padding: 0;
  border-radius: 999px;
}

.admin-icon-button--success {
  border-color: rgba(150, 255, 176, 0.45);
  background: linear-gradient(135deg, rgba(20, 150, 78, 0.94), rgba(11, 98, 49, 0.94));
  color: #f2fff5;
}

.admin-icon-button--danger {
  border-color: rgba(255, 114, 114, 0.5);
  background: linear-gradient(135deg, rgba(214, 31, 31, 0.94), rgba(146, 12, 12, 0.94));
  color: #fff4f4;
}

.admin-inline-badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
}

.admin-form {
  display: grid;
  gap: var(--space-md);
}

.admin-field {
  display: grid;
  gap: var(--space-xs);
}

.admin-field--full {
  grid-column: 1 / -1;
}

.admin-field span {
  color: var(--text-muted);
  font-size: var(--fs-caption);
  font-weight: 700;
}

.admin-field input,
.admin-field select,
.admin-field textarea {
  width: 100%;
  min-height: 50px;
  padding: 12px 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  color: var(--text-body);
  background: var(--surface-glass);
}

.admin-field textarea {
  resize: vertical;
  min-height: 150px;
}

.admin-field select {
  appearance: none;
  background:
    linear-gradient(180deg, rgba(39, 16, 16, 0.98) 0%, rgba(24, 10, 10, 0.98) 100%);
}

.admin-field select option {
  color: var(--text-strong);
  background: #1b0c0c;
}

.admin-field input:focus,
.admin-field select:focus,
.admin-field textarea:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

.admin-checkbox {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--text-body);
  font-weight: 600;
}

.admin-checkbox input {
  accent-color: var(--accent);
}

.admin-form__actions,
.admin-assignment-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: var(--space-md);
}

.admin-assignment-toolbar > * {
  flex: 1 1 220px;
}

.admin-assignment-toolbar__button {
  flex: none;
}

@media (max-width: 980px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .admin-card,
  .admin-assignment-toolbar,
  .admin-form__actions {
    flex-direction: column;
    align-items: stretch;
  }

  .admin-card__actions {
    width: 100%;
    justify-content: flex-start;
  }

  .admin-button,
  .admin-form__actions .action-button,
  .admin-assignment-toolbar__button {
    width: 100%;
  }
}
</style>
