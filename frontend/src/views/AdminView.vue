<script setup lang="ts">
import { isAxiosError } from 'axios'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import AppModal from '@/components/AppModal.vue'
import PageHero from '@/components/PageHero.vue'
import AdminOrganizerRequestsPanel from '@/components/admin/AdminOrganizerRequestsPanel.vue'
import AdminServicesPanel from '@/components/admin/AdminServicesPanel.vue'
import AdminTracksPanel from '@/components/admin/AdminTracksPanel.vue'
import AdminUsersPanel from '@/components/admin/AdminUsersPanel.vue'
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

const tabItems: Array<{ id: AdminTab; label: string }> = [
  { id: 'organizers', label: 'Solicitudes' },
  { id: 'tracks', label: 'Circuitos' },
  { id: 'services', label: 'Servicios' },
  { id: 'users', label: 'Usuarios' },
]

const auth = useAuth()
const toast = useToast()

// AdminView coordina paneles hijos. Aquí viven los datos y las acciones con backend.
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

const trackForm = reactive<TrackPayload>(createEmptyTrackForm())
const serviceForm = reactive<ServiceCatalogPayload>(createEmptyServiceForm())

// Las listas ordenadas son 'computed' para no reordenar manualmente tras cada cambio.
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
  // Mapa auxiliar para cruzar usuarios con su ficha de organizador.
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
  // Solo muestro las asociaciones del circuito seleccionado.
  if (!selectedTrackId.value) {
    return []
  }

  return [...trackServiceAssignments.value]
    .filter((assignment) => assignment.trackId === Number(selectedTrackId.value))
    .sort((left, right) => left.serviceName.localeCompare(right.serviceName))
})

const assignableServices = computed(() => {
  // Evito ofrecer servicios ya asignados al circuito.
  const assignedServiceIds = new Set(selectedTrackAssignments.value.map((assignment) => assignment.serviceId))

  return sortedServices.value.filter(
    (service) =>
      service.allowedForTrack && service.enabled && !assignedServiceIds.has(service.id),
  )
})

onMounted(async () => {
  // Si no hay sesión, no intento cargar datos admin y muestro acceso restringido.
  if (!auth.isAuthenticated.value) {
    loading.value = false
    errorTitle.value = 'Acceso restringido'
    error.value = 'Inicia sesión con una cuenta de administrador para acceder a esta área.'
    return
  }

  if (!isAdminRole(auth.session.value?.roleName)) {
    // Refresco por si el rol cambió en backend y la sesión local todavía está antigua.
    await auth.refreshSession()
  }

  await loadAdminPage()
})

watch(
  () => auth.session.value?.roleName,
  async () => {
    // Si el login cambia mientras estoy en esta vista, reintento cargar el panel.
    if (!auth.isAuthenticated.value || !error.value) {
      return
    }

    await loadAdminPage()
  },
)

function createEmptyTrackForm(): TrackPayload {
  return {
    name: '',
    shortName: '',
    location: '',
    description: '',
  }
}

function createEmptyServiceForm(): ServiceCatalogPayload {
  return {
    name: '',
    description: '',
    allowedForTrack: true,
    allowedForOrganizer: false,
  }
}

async function loadAdminPage() {
  loading.value = true
  error.value = ''
  errorTitle.value = 'No se pudo cargar el panel'

  try {
    // Cargo todas las piezas del panel en paralelo para tener una foto completa.
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

    if (!selectedTrackId.value && nextTracks[0]) {
      selectedTrackId.value = nextTracks[0].id
    }
  } catch (requestError) {
    if (isAxiosError(requestError)) {
      if (requestError.response?.status === 403) {
        errorTitle.value = 'Acceso restringido'
        error.value = 'Esta area esta reservada para cuentas con rol administrador.'
      } else if (requestError.response?.status === 401) {
        errorTitle.value = 'Sesión no válida'
        error.value = 'Tu sesión ya no es válida. Inicia sesión de nuevo para continuar.'
      } else {
        error.value = 'No se pudo cargar el panel de administración.'
      }
    } else {
      error.value = 'No se pudo cargar el panel de administración.'
    }
  } finally {
    loading.value = false
  }
}

function resetTrackForm() {
  // El mismo modal sirve para crear y editar, por eso reseteo id y campos juntos.
  editingTrackId.value = null
  Object.assign(trackForm, createEmptyTrackForm())
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
  editingTrackId.value = track.id
  Object.assign(trackForm, {
    name: track.name,
    shortName: track.shortName,
    location: track.location,
    description: track.description,
  })
  trackError.value = ''
  selectedTrackId.value = track.id
  isTrackModalOpen.value = true
}

async function saveTrack() {
  trackSaving.value = true
  trackError.value = ''

  try {
    // Recorto textos justo antes de enviar para no guardar espacios accidentales.
    const payload: TrackPayload = {
      name: trackForm.name.trim(),
      shortName: trackForm.shortName.trim(),
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
  Object.assign(serviceForm, createEmptyServiceForm())
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
  editingServiceId.value = service.id
  Object.assign(serviceForm, {
    name: service.name,
    description: service.description,
    allowedForTrack: service.allowedForTrack,
    allowedForOrganizer: service.allowedForOrganizer,
  })
  serviceError.value = ''
  isServiceModalOpen.value = true
}

async function saveService() {
  serviceSaving.value = true
  serviceError.value = ''

  try {
    // El servicio puede estar disponible para circuito, organizador o ambos.
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
  // Esta acción vincula un servicio maestro con un circuito concreto.
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
  // Aprobar activa la ficha de organizador asociada a ese usuario.
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

function isProtectedDefaultAdmin(user: AdminUser): boolean {
  // Protejo el admin de demo para no dejar el proyecto sin una cuenta administradora.
  return isAdminRole(user.roleName) && user.email.trim().toLowerCase() === DEFAULT_ADMIN_EMAIL
}

function resolveRequestError(requestError: unknown, fallbackMessage: string): string {
  return resolveApiErrorMessage(requestError, {
    fallback: fallbackMessage,
    statusMessages: {
      403: 'No tienes permisos para realizar esta acción.',
    },
  })
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Administración</p>
      <h1 class="ui-title-section">Cargando centro de control</h1>
      <p class="ui-copy-muted">Estamos preparando los datos de organizadores, circuitos y usuarios.</p>
    </section>

    <section v-else-if="error" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Administración</p>
      <h1 class="ui-title-section">{{ errorTitle }}</h1>
      <p class="ui-copy-muted">{{ error }}</p>
      <RouterLink class="action-button" to="/">Volver al inicio</RouterLink>
    </section>

    <template v-else>
      <PageHero
        eyebrow="Centro de control"
        title="Administración"
        description="Gestiona solicitudes de organizador, circuitos, servicios y cuentas de usuario desde un único panel operativo."
      />

      <section class="panel panel-pad-lg panel-stack-lg">
        <div class="pill-tabs" role="tablist" aria-label="Navegación de administración">
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

        <AdminOrganizerRequestsPanel
          v-if="activeTab === 'organizers'"
          :organizer-error="organizerError"
          :pending-organizers="pendingOrganizers"
          :organizer-busy-id="organizerBusyId"
          @approve="approveOrganizerRequest"
          @deny="denyOrganizerRequest"
        />

        <AdminTracksPanel
          v-else-if="activeTab === 'tracks'"
          :track-error="trackError"
          :service-error="serviceError"
          :sorted-tracks="sortedTracks"
          :selected-track-id="selectedTrackId"
          :selected-service-id="selectedServiceId"
          :assignable-services="assignableServices"
          :selected-track-assignments="selectedTrackAssignments"
          :assignment-saving="assignmentSaving"
          :assignment-deleting-id="assignmentDeletingId"
          @open-create-track="openTrackCreateModal"
          @edit-track="startTrackEdit"
          @update:selected-track-id="selectedTrackId = $event"
          @update:selected-service-id="selectedServiceId = $event"
          @add-assignment="addTrackServiceAssignment"
          @remove-assignment="removeTrackServiceAssignment"
        />

        <AdminServicesPanel
          v-else-if="activeTab === 'services'"
          :service-error="serviceError"
          :sorted-services="sortedServices"
          :service-busy-id="serviceBusyId"
          @open-create-service="openServiceCreateModal"
          @edit-service="startServiceEdit"
          @toggle-service="toggleServiceEnabled"
        />

        <AdminUsersPanel
          v-else
          :user-error="userError"
          :active-user-tab="activeUserTab"
          :user-busy-id="userBusyId"
          :admin-users="adminUsers"
          :organizer-users="organizerUsers"
          :standard-users="standardUsers"
          :is-protected-default-admin="isProtectedDefaultAdmin"
          @update:active-user-tab="activeUserTab = $event"
          @toggle-user="toggleUserEnabled"
        />
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
    <form class="surface-form-grid" @submit.prevent="saveTrack">
      <p v-if="trackError" class="status-message status-message--error surface-field--full">{{ trackError }}</p>

      <label class="surface-field surface-field--full">
        <span class="surface-field__label">Nombre</span>
        <input v-model="trackForm.name" type="text" maxlength="255" />
      </label>

      <label class="surface-field surface-field--full">
        <span class="surface-field__label">Nombre corto de assets</span>
        <input
          v-model="trackForm.shortName"
          type="text"
          maxlength="120"
          pattern="[a-z0-9_]+"
          placeholder="jarama"
        />
        <span class="ui-copy-caption">
          Usa minúsculas, números y guiones bajos. Se combinará con `_cover_1`, `_cover_2` y `_layout`.
        </span>
      </label>

      <label class="surface-field surface-field--full">
        <span class="surface-field__label">Ubicación</span>
        <input v-model="trackForm.location" type="text" maxlength="255" />
      </label>

      <label class="surface-field surface-field--full">
        <span class="surface-field__label">Descripción</span>
        <textarea v-model="trackForm.description" rows="6" maxlength="500"></textarea>
      </label>

      <div class="action-row action-row--end surface-field--full">
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
    <form class="surface-form-grid" @submit.prevent="saveService">
      <p v-if="serviceError" class="status-message status-message--error surface-field--full">{{ serviceError }}</p>

      <label class="surface-field surface-field--full">
        <span class="surface-field__label">Nombre</span>
        <input v-model="serviceForm.name" type="text" maxlength="255" />
      </label>

      <label class="surface-field surface-field--full">
        <span class="surface-field__label">Descripción</span>
        <textarea v-model="serviceForm.description" rows="6" maxlength="500"></textarea>
      </label>

      <label class="surface-checkbox surface-field--full">
        <input v-model="serviceForm.allowedForTrack" type="checkbox" />
        <span>Disponible para circuitos</span>
      </label>

      <label class="surface-checkbox surface-field--full">
        <input v-model="serviceForm.allowedForOrganizer" type="checkbox" />
        <span>Disponible para organizadores</span>
      </label>

      <div class="action-row action-row--end surface-field--full">
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
