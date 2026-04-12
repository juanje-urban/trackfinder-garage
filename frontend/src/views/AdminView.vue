<script setup lang="ts">
import { isAxiosError } from 'axios'
import { Check, Pencil, Plus, Power, PowerOff, Trash2, X } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import AppModal from '@/components/AppModal.vue'
import PageHero from '@/components/PageHero.vue'
import AdminUserCard from '@/components/admin/AdminUserCard.vue'
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
import { isAdminRole, isOrganizerRole } from '@/utils/authRoles'
import { resolveApiErrorMessage } from '@/utils/apiErrors'

type AdminTab = 'organizers' | 'tracks' | 'services' | 'users'
type UserManagementTab = 'admins' | 'organizers' | 'users'
type OrganizerAdminRecord = {
  user: AdminUser
  organizer: Organizer | null
}

const DEFAULT_ADMIN_EMAIL = 'admin@example.com'

const auth = useAuth()
const toast = useToast()

const loading = ref(true)
const error = ref('')
const errorTitle = ref('Acceso restringido')
const activeTab = ref<AdminTab>('organizers')
const activeUserTab = ref<UserManagementTab>('admins')
const isTrackModalOpen = ref(false)
const isServiceModalOpen = ref(false)

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

const organizersByUserId = computed(
  () => new Map(organizers.value.map((organizer) => [organizer.idUser, organizer])),
)

const adminUsers = computed(() =>
  sortedUsers.value.filter((user) => isAdminRole(user.roleName)),
)

const organizerUsers = computed<OrganizerAdminRecord[]>(() =>
  sortedUsers.value
    .filter((user) => isOrganizerRole(user.roleName))
    .map((user) => ({
      user,
      organizer: organizersByUserId.value.get(user.id) ?? null,
    })),
)

const standardUsers = computed(() =>
  sortedUsers.value.filter((user) => !isAdminRole(user.roleName) && !isOrganizerRole(user.roleName)),
)

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

const userTabItems: Array<{ id: UserManagementTab; label: string }> = [
  { id: 'admins', label: 'Administradores' },
  { id: 'organizers', label: 'Organizadores' },
  { id: 'users', label: 'Usuarios' },
]

onMounted(async () => {
  if (!auth.isAuthenticated.value) {
    loading.value = false
    errorTitle.value = 'Acceso restringido'
    error.value = 'Inicia sesion con una cuenta de administrador para acceder a esta area.'
    return
  }

  if (!isAdminRole(auth.session.value?.roleName)) {
    await auth.refreshSession()
  }

  await loadAdminPage()
})

watch(
  () => auth.session.value?.roleName,
  async () => {
    if (!auth.isAuthenticated.value || !error.value) {
      return
    }

    await loadAdminPage()
  },
)

async function loadAdminPage() {
  loading.value = true
  error.value = ''
  errorTitle.value = 'No se pudo cargar el panel'

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
  } catch (requestError) {
    if (isAxiosError(requestError)) {
      if (requestError.response?.status === 403) {
        errorTitle.value = 'Acceso restringido'
        error.value = 'Esta area esta reservada para cuentas con rol administrador.'
      } else if (requestError.response?.status === 401) {
        errorTitle.value = 'Sesion no valida'
        error.value = 'Tu sesion ya no es valida. Inicia sesion de nuevo para continuar.'
      } else {
        error.value = 'No se pudo cargar el panel de administracion.'
      }
    } else {
      error.value = 'No se pudo cargar el panel de administracion.'
    }
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

function openTrackCreateModal() {
  resetTrackForm()
  isTrackModalOpen.value = true
}

function closeTrackModal() {
  isTrackModalOpen.value = false
  resetTrackForm()
}

function startTrackEdit(track: Track) {
  isTrackModalOpen.value = true
  editingTrackId.value = track.id
  trackForm.name = track.name
  trackForm.location = track.location
  trackForm.description = track.description
  trackError.value = ''
  selectedTrackId.value = track.id
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
    closeTrackModal()
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

function openServiceCreateModal() {
  resetServiceForm()
  isServiceModalOpen.value = true
}

function closeServiceModal() {
  isServiceModalOpen.value = false
  resetServiceForm()
}

function startServiceEdit(service: ServiceCatalogItem) {
  isServiceModalOpen.value = true
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
    closeServiceModal()
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

function formatUserName(user: Pick<AdminUser, 'name' | 'surname'>): string {
  return [user.name, user.surname].filter((value) => value.trim() !== '').join(' ')
}

function formatUserSummaryLine(user: Pick<AdminUser, 'email' | 'phone' | 'address'>): string {
  return [user.email, user.phone, user.address]
    .filter((value) => value.trim() !== '')
    .join(' · ')
}

function formatOrganizerSummaryLine(organizer: Organizer | null): string {
  if (organizer === null) {
    return ''
  }

  return [organizer.legalName, `CIF: ${organizer.cif}`]
    .filter((value) => value.trim() !== '')
    .join(' · ')
}

function isProtectedDefaultAdmin(user: AdminUser): boolean {
  return isAdminRole(user.roleName) && user.email.trim().toLowerCase() === DEFAULT_ADMIN_EMAIL
}

function resolveUserTabToneClass(tabId: UserManagementTab): string {
  return `admin-subtabs__tab--${tabId}`
}

function resolveRequestError(requestError: unknown, fallbackMessage: string): string {
  return resolveApiErrorMessage(requestError, {
    fallback: fallbackMessage,
    statusMessages: {
      403: 'No tienes permisos para realizar esta accion.',
    },
  })
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
      <h1 class="ui-title-section">{{ errorTitle }}</h1>
      <p class="ui-copy-muted">{{ error }}</p>
      <RouterLink class="action-button" to="/">Volver al inicio</RouterLink>
    </section>

    <template v-else>
      <PageHero
        eyebrow="Centro de control"
        title="Administraci&oacute;n"
        description="Gestiona solicitudes de organizador, circuitos, servicios y cuentas de usuario desde un unico panel operativo."
      />

      <section class="admin-tabs panel panel-pad-lg panel-stack-lg">
        <div class="pill-tabs" role="tablist" aria-label="Navegacion de administracion">
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
          <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

          <article class="panel panel-pad-lg panel-stack-md">
            <div class="admin-section-header">
              <div class="panel-copy">
                <h3 class="ui-title-card">Circuitos</h3>
                <p class="ui-copy-muted">Edita los trazados existentes o da de alta uno nuevo.</p>
              </div>

              <button class="action-button admin-create-button" type="button" @click="openTrackCreateModal">
                <Plus :size="16" aria-hidden="true" />
                Anadir circuito
              </button>
            </div>

            <div class="admin-list">
              <article v-for="track in sortedTracks" :key="track.id" class="admin-card">
                <div class="panel-copy admin-card__copy">
                  <p class="ui-eyebrow">{{ track.location }}</p>
                  <h4 class="ui-title-card admin-card__title">{{ track.name }}</h4>
                  <p class="ui-copy-muted">{{ track.description }}</p>
                </div>

                <button
                  class="icon-button icon-button--danger"
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
                  class="icon-button icon-button--danger"
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

        <section v-else-if="activeTab === 'services'" class="panel-stack-lg">
          <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

          <article class="panel panel-pad-lg panel-stack-md">
            <div class="admin-section-header">
              <div class="panel-copy">
                <h3 class="ui-title-card">Catalogo de servicios</h3>
                <p class="ui-copy-muted">
                  Crea servicios nuevos, ajusta su alcance y activa o desactiva su disponibilidad.
                </p>
              </div>

              <button class="action-button admin-create-button" type="button" @click="openServiceCreateModal">
                <Plus :size="16" aria-hidden="true" />
                Anadir servicio
              </button>
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
                    class="icon-button"
                    type="button"
                    aria-label="Editar servicio"
                    title="Editar servicio"
                    @click="startServiceEdit(service)"
                  >
                    <Pencil :size="16" aria-hidden="true" />
                  </button>
                  <button
                    class="icon-button"
                    :class="service.enabled ? 'icon-button--danger' : 'icon-button--success'"
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

            <div class="pill-tabs" role="tablist" aria-label="Tipos de cuenta">
              <button
                v-for="tab in userTabItems"
                :key="tab.id"
                class="pill-tab admin-subtabs__tab"
                :class="[resolveUserTabToneClass(tab.id), { 'pill-tab--active': activeUserTab === tab.id }]"
                type="button"
                @click="activeUserTab = tab.id"
              >
                {{ tab.label }}
              </button>
            </div>

            <div v-if="activeUserTab === 'admins'" class="admin-list">
              <p v-if="adminUsers.length === 0" class="ui-copy-muted">
                No hay administradores registrados en este momento.
              </p>

              <AdminUserCard
                v-for="user in adminUsers"
                :key="user.id"
                :display-name="user.displayName"
                :real-name="formatUserName(user)"
                :summary="formatUserSummaryLine(user)"
                :enabled="user.enabled"
                :disabled="userBusyId === user.id || isProtectedDefaultAdmin(user)"
                :protected-label="isProtectedDefaultAdmin(user) ? 'Protegido' : undefined"
                @toggle="toggleUserEnabled(user)"
              />
            </div>

            <div v-else-if="activeUserTab === 'organizers'" class="admin-list">
              <p v-if="organizerUsers.length === 0" class="ui-copy-muted">
                No hay organizadores registrados en este momento.
              </p>

              <AdminUserCard
                v-for="item in organizerUsers"
                :key="item.user.id"
                :display-name="item.user.displayName"
                :real-name="formatUserName(item.user)"
                :summary="formatUserSummaryLine(item.user)"
                :detail="formatOrganizerSummaryLine(item.organizer)"
                :enabled="item.user.enabled"
                :disabled="userBusyId === item.user.id"
                @toggle="toggleUserEnabled(item.user)"
              />
            </div>

            <div v-else class="admin-list">
              <p v-if="standardUsers.length === 0" class="ui-copy-muted">
                No hay usuarios registrados en este momento.
              </p>

              <AdminUserCard
                v-for="user in standardUsers"
                :key="user.id"
                :display-name="user.displayName"
                :real-name="formatUserName(user)"
                :summary="formatUserSummaryLine(user)"
                :enabled="user.enabled"
                :disabled="userBusyId === user.id"
                @toggle="toggleUserEnabled(user)"
              />
            </div>
          </article>
        </section>
      </section>
    </template>
  </main>

  <AppModal
    :is-open="isTrackModalOpen"
    :ariaLabel="editingTrackId === null ? 'Nuevo circuito' : 'Editar circuito'"
    eyebrow="Circuitos"
    :title="editingTrackId === null ? 'Nuevo circuito' : 'Editar circuito'"
    @close="closeTrackModal"
  >
      <form class="admin-form" @submit.prevent="saveTrack">
        <p v-if="trackError" class="status-message status-message--error">{{ trackError }}</p>

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
          <button class="action-button action-button--ghost" type="button" @click="closeTrackModal">
            Cancelar
          </button>
          <button class="action-button" type="submit" :disabled="trackSaving">
            {{ trackSaving ? 'Guardando...' : editingTrackId === null ? 'Crear circuito' : 'Guardar cambios' }}
          </button>
        </div>
      </form>
  </AppModal>

  <AppModal
    :is-open="isServiceModalOpen"
    :ariaLabel="editingServiceId === null ? 'Nuevo servicio' : 'Editar servicio'"
    eyebrow="Servicios"
    :title="editingServiceId === null ? 'Nuevo servicio' : 'Editar servicio'"
    @close="closeServiceModal"
  >
      <form class="admin-form" @submit.prevent="saveService">
        <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

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
          <button class="action-button action-button--ghost" type="button" @click="closeServiceModal">
            Cancelar
          </button>
          <button class="action-button" type="submit" :disabled="serviceSaving">
            {{ serviceSaving ? 'Guardando...' : editingServiceId === null ? 'Crear servicio' : 'Guardar cambios' }}
          </button>
        </div>
      </form>
  </AppModal>
</template>

<style scoped>

.admin-subtabs__tab {
  --admin-subtab-border: rgba(255, 255, 255, 0.12);
  --admin-subtab-bg: rgba(255, 255, 255, 0.04);
  --admin-subtab-active-border: rgba(255, 91, 60, 0.28);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(62, 19, 16, 0.94), rgba(28, 10, 10, 0.96));
  border: 1px solid var(--admin-subtab-border);
  background: var(--admin-subtab-bg);
}

.admin-subtabs__tab.pill-tab--active {
  border-color: var(--admin-subtab-active-border);
  background: var(--admin-subtab-active-bg);
  box-shadow: 0 14px 28px rgba(0, 0, 0, 0.18);
}

.admin-subtabs__tab--admins {
  --admin-subtab-border: rgba(255, 90, 90, 0.24);
  --admin-subtab-bg: rgba(107, 21, 21, 0.22);
  --admin-subtab-active-border: rgba(255, 114, 114, 0.44);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(123, 26, 26, 0.96), rgba(62, 13, 13, 0.98));
}

.admin-subtabs__tab--organizers {
  --admin-subtab-border: rgba(255, 190, 92, 0.24);
  --admin-subtab-bg: rgba(100, 58, 11, 0.22);
  --admin-subtab-active-border: rgba(255, 195, 92, 0.42);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(120, 71, 13, 0.96), rgba(59, 35, 8, 0.98));
}

.admin-subtabs__tab--users {
  --admin-subtab-border: rgba(88, 188, 255, 0.24);
  --admin-subtab-bg: rgba(12, 51, 83, 0.22);
  --admin-subtab-active-border: rgba(98, 194, 255, 0.42);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(13, 81, 120, 0.96), rgba(8, 39, 59, 0.98));
}

.admin-section-header {
  display: flex;
  flex-wrap: wrap;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-md);
}

.admin-create-button {
  align-self: center;
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

@media (max-width: 720px) {
  .admin-card,
  .admin-assignment-toolbar,
  .admin-form__actions,
  .admin-section-header {
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
