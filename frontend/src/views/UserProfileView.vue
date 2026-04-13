<script setup lang="ts">
import { Eye, EyeOff, Pencil, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import EventBookingDialog from '@/components/EventBookingDialog.vue'
import UserProfileHero from '@/components/UserProfileHero.vue'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'
import {
  cancelEventBooking,
  getCurrentUserEventBookings,
  updateOwnEventBookingVisibility,
} from '@/services/eventBookingService'
import {
  getCurrentOrganizerProfile,
  updateCurrentOrganizerProfile,
} from '@/services/organizerService'
import {
  createCurrentUserLapTime,
  deleteCurrentUserLapTime,
  getCurrentUserLapTimes,
} from '@/services/lapTimeService'
import { getTrackRanking, getTracks } from '@/services/trackService'
import { getCurrentUserProfile, updateCurrentUserProfile } from '@/services/userService'
import type { EventBooking } from '@/types/eventBooking'
import type { LapTime } from '@/types/lapTime'
import type { OrganizerProfile } from '@/types/organizer'
import type { TrackRecord } from '@/types/trackRecord'
import type { Track } from '@/types/track'
import type { UserProfile } from '@/types/user'
import { resolveApiErrorMessage } from '@/utils/apiErrors'
import { isOrganizerRole, isUserRole } from '@/utils/authRoles'
import { toIsoDate } from '@/utils/date'
import { formatCurrency, formatDisplayDate, formatLapTime } from '@/utils/format'

type ProfileTab = 'reservas' | 'perfil' | 'vueltas'

type BookingSection = {
  id: 'future'
  title: string
  emptyMessage: string
  bookings: EventBooking[]
  allowCancellation: boolean
}

const auth = useAuth()
const router = useRouter()
const toast = useToast()

const loading = ref(true)
const error = ref('')
const activeTab = ref<ProfileTab>('reservas')

const profile = ref<UserProfile | null>(null)
const organizerProfile = ref<OrganizerProfile | null>(null)
const bookings = ref<EventBooking[]>([])
const lapTimes = ref<LapTime[]>([])
const tracks = ref<Track[]>([])
const trackRankings = ref<Record<number, TrackRecord[]>>({})

const bookingError = ref('')
const profileError = ref('')
const lapError = ref('')

const profileSaving = ref(false)
const lapSaving = ref(false)
const bookingCancellingId = ref<number | null>(null)
const bookingVisibilityUpdatingId = ref<number | null>(null)
const bookingPendingCancellation = ref<EventBooking | null>(null)
const profileEditMode = ref(false)

const profileForm = reactive({
  name: '',
  surname: '',
  email: '',
  address: '',
  phone: '',
  legalName: '',
  cif: '',
  password: '',
  passwordConfirmation: '',
})

const lapForm = reactive({
  trackId: '',
  lapDate: '',
  lapTimeText: '',
  vehicle: '',
})

const isStandardUser = computed(() => isUserRole(auth.session.value?.roleName))
const isOrganizerAccount = computed(() => isOrganizerRole(auth.session.value?.roleName))

const todayIso = computed(() => toIsoDate(new Date()))
const cancellationCutoffIso = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + 14)
  return toIsoDate(date)
})

const futureBookings = computed(() =>
  [...bookings.value]
    .filter((booking) => booking.eventDate >= todayIso.value)
    .sort((left, right) => left.eventDate.localeCompare(right.eventDate)),
)

const pastBookings = computed(() =>
  [...bookings.value]
    .filter((booking) => booking.eventDate < todayIso.value)
    .sort((left, right) => right.eventDate.localeCompare(left.eventDate)),
)

const uniqueVisitedTracks = computed(
  () => new Set(pastBookings.value.map((booking) => booking.trackName)).size,
)

const sortedLapTimes = computed(() =>
  [...lapTimes.value].sort((left, right) => {
    const byDate = right.lapDate.localeCompare(left.lapDate)
    if (byDate !== 0) {
      return byDate
    }

    return left.lapTimeMs - right.lapTimeMs
  }),
)

const topFiveLapTimes = computed(() =>
  lapTimes.value.filter((lapTime) =>
    (trackRankings.value[lapTime.trackId] ?? []).some(
      (record) =>
        record.userDisplayName === lapTime.userDisplayName &&
        record.lapDate === lapTime.lapDate &&
        record.lapTimeMs === lapTime.lapTimeMs &&
        record.vehicle === lapTime.vehicle,
    ),
  ).length,
)

const poleCount = computed(() => {
  if (!profile.value) {
    return 0
  }

  const displayName = profile.value.displayName

  return Object.values(trackRankings.value).filter(
    (ranking) => ranking[0]?.userDisplayName === displayName,
  ).length
})

const tabItems = computed<Array<{ id: ProfileTab; label: string }>>(() =>
  isOrganizerAccount.value
    ? [{ id: 'perfil', label: 'Perfil' }]
    : [
        { id: 'reservas', label: 'Reservas' },
        { id: 'perfil', label: 'Perfil' },
        { id: 'vueltas', label: 'Vueltas' },
      ],
)

const bookingSections = computed<BookingSection[]>(() => [
  {
    id: 'future',
    title: 'Reservas activas',
    emptyMessage: 'Todavia no tienes reservas futuras.',
    bookings: futureBookings.value,
    allowCancellation: true,
  },
])

onMounted(async () => {
  if (!isStandardUser.value && !isOrganizerAccount.value) {
    loading.value = false
    error.value = 'Esta area esta reservada para usuarios y organizadores con sesion iniciada.'
    return
  }

  await loadProfilePage()
})

async function loadProfilePage() {
  loading.value = true
  error.value = ''

  try {
    if (isOrganizerAccount.value) {
      activeTab.value = 'perfil'
      organizerProfile.value = await getCurrentOrganizerProfile()
      profile.value = null
      bookings.value = []
      lapTimes.value = []
      tracks.value = []
      trackRankings.value = {}
      syncOrganizerProfileForm(organizerProfile.value)
      return
    }

    const [profileResult, bookingsResult, lapTimesResult, tracksResult] = await Promise.all([
      getCurrentUserProfile(),
      getCurrentUserEventBookings(),
      getCurrentUserLapTimes(),
      getTracks(),
    ])

    organizerProfile.value = null
    profile.value = profileResult
    bookings.value = bookingsResult
    lapTimes.value = lapTimesResult
    tracks.value = tracksResult
    syncUserProfileForm(profileResult)
    await refreshTrackRankings(lapTimesResult)
  } catch {
    error.value = 'No se pudo cargar tu perfil.'
  } finally {
    loading.value = false
  }
}

async function refreshTrackRankings(sourceLapTimes: LapTime[]) {
  const uniqueTrackIds = [...new Set(sourceLapTimes.map((lapTime) => lapTime.trackId))]

  if (uniqueTrackIds.length === 0) {
    trackRankings.value = {}
    return
  }

  const rankingResults = await Promise.allSettled(
    uniqueTrackIds.map(async (trackId) => [trackId, await getTrackRanking(trackId, 5)] as const),
  )

  trackRankings.value = Object.fromEntries(
    rankingResults
      .filter((result): result is PromiseFulfilledResult<readonly [number, TrackRecord[]]> => result.status === 'fulfilled')
      .map((result) => result.value),
  )
}

function syncUserProfileForm(nextProfile: UserProfile) {
  profileForm.name = nextProfile.name
  profileForm.surname = nextProfile.surname
  profileForm.email = nextProfile.email
  profileForm.address = nextProfile.address
  profileForm.phone = nextProfile.phone
  profileForm.legalName = ''
  profileForm.cif = ''
  profileForm.password = ''
  profileForm.passwordConfirmation = ''
}

function syncOrganizerProfileForm(nextProfile: OrganizerProfile) {
  profileForm.name = nextProfile.name
  profileForm.surname = nextProfile.surname
  profileForm.email = nextProfile.email
  profileForm.address = nextProfile.address
  profileForm.phone = nextProfile.phone
  profileForm.legalName = nextProfile.legalName
  profileForm.cif = nextProfile.cif
  profileForm.password = ''
  profileForm.passwordConfirmation = ''
}

function startProfileEdit() {
  if (!profile.value && !organizerProfile.value) {
    return
  }

  if (isOrganizerAccount.value && organizerProfile.value) {
    syncOrganizerProfileForm(organizerProfile.value)
  } else if (profile.value) {
    syncUserProfileForm(profile.value)
  }

  profileError.value = ''
  profileEditMode.value = true
}

function cancelProfileEdit() {
  if (!profile.value && !organizerProfile.value) {
    return
  }

  if (isOrganizerAccount.value && organizerProfile.value) {
    syncOrganizerProfileForm(organizerProfile.value)
  } else if (profile.value) {
    syncUserProfileForm(profile.value)
  }

  profileError.value = ''
  profileEditMode.value = false
}

function getBookingVisibilityToneClass(isVisible: boolean): string {
  return isVisible ? 'icon-button--danger' : 'icon-button--success'
}

function canCancelBooking(booking: EventBooking): boolean {
  return booking.eventDate >= cancellationCutoffIso.value
}

async function cancelBooking(booking: EventBooking) {
  if (!canCancelBooking(booking)) {
    bookingError.value = 'La anulacion solo esta disponible hasta 14 dias antes del evento.'
    return
  }

  bookingPendingCancellation.value = booking
  bookingError.value = ''
}

function closeBookingCancellationDialog() {
  if (bookingCancellingId.value !== null) {
    return
  }

  bookingError.value = ''
  bookingPendingCancellation.value = null
}

async function confirmBookingCancellation() {
  const booking = bookingPendingCancellation.value

  if (!booking) {
    return
  }

  bookingCancellingId.value = booking.id
  bookingError.value = ''

  try {
    await cancelEventBooking(booking.id)
    bookings.value = bookings.value.filter((currentBooking) => currentBooking.id !== booking.id)
    bookingPendingCancellation.value = null
    toast.showToast('La reserva se ha anulado correctamente.')
  } catch (requestError) {
    bookingError.value = resolveBookingCancellationError(requestError)
  } finally {
    bookingCancellingId.value = null
  }
}

async function toggleBookingVisibility(booking: EventBooking) {
  bookingVisibilityUpdatingId.value = booking.id
  bookingError.value = ''

  try {
    const updatedBooking = await updateOwnEventBookingVisibility(booking.id, {
      isVisible: !booking.isVisible,
    })

    bookings.value = bookings.value.map((currentBooking) =>
      currentBooking.id === updatedBooking.id ? updatedBooking : currentBooking,
    )
    toast.showToast(
      updatedBooking.isVisible
        ? 'La reserva vuelve a mostrarse en tu perfil p\u00fablico.'
        : 'La reserva se ha ocultado de tu perfil p\u00fablico.',
    )
  } catch (requestError) {
    bookingError.value = resolveBookingVisibilityError(requestError)
  } finally {
    bookingVisibilityUpdatingId.value = null
  }
}

async function saveProfile() {
  if (isOrganizerAccount.value) {
    await saveOrganizerProfile()
    return
  }

  if (!profile.value) {
    return
  }

  profileSaving.value = true
  profileError.value = ''

  const nextPassword = profileForm.password.trim()
  const nextPasswordConfirmation = profileForm.passwordConfirmation.trim()

  if ((nextPassword !== '' || nextPasswordConfirmation !== '') && nextPassword !== nextPasswordConfirmation) {
    profileSaving.value = false
    profileError.value = 'La confirmacion de la contrasena no coincide.'
    return
  }

  try {
    const nextName = profileForm.name.trim()
    const nextSurname = profileForm.surname.trim()
    const nextEmail = profileForm.email.trim().toLowerCase()
    const credentialChanged =
      nextEmail !== profile.value.email.toLowerCase() || nextPassword !== ''

    const updatedProfile = await updateCurrentUserProfile({
      name: nextName,
      surname: nextSurname,
      email: nextEmail,
      address: profileForm.address.trim(),
      phone: profileForm.phone.trim(),
      password: nextPassword || undefined,
    })

    profile.value = updatedProfile
    syncUserProfileForm(updatedProfile)
    profileEditMode.value = false

    if (credentialChanged) {
      auth.clearSession()
      toast.showToast('Tus credenciales se han actualizado. Inicia sesion de nuevo para continuar.')
      await router.push('/')
      auth.openAuthDialog()
      return
    }

    toast.showToast('Tus datos se han actualizado correctamente.')
  } catch (requestError) {
    profileError.value = resolveProfileError(requestError)
  } finally {
    profileSaving.value = false
  }
}

async function saveOrganizerProfile() {
  if (!organizerProfile.value) {
    return
  }

  profileSaving.value = true
  profileError.value = ''

  const nextPassword = profileForm.password.trim()
  const nextPasswordConfirmation = profileForm.passwordConfirmation.trim()

  if ((nextPassword !== '' || nextPasswordConfirmation !== '') && nextPassword !== nextPasswordConfirmation) {
    profileSaving.value = false
    profileError.value = 'La confirmacion de la contrasena no coincide.'
    return
  }

  try {
    const nextName = profileForm.name.trim()
    const nextSurname = profileForm.surname.trim()
    const nextEmail = profileForm.email.trim().toLowerCase()
    const credentialChanged =
      nextEmail !== organizerProfile.value.email.toLowerCase() || nextPassword !== ''

    const updatedProfile = await updateCurrentOrganizerProfile({
      name: nextName,
      surname: nextSurname,
      email: nextEmail,
      address: profileForm.address.trim(),
      phone: profileForm.phone.trim(),
      legalName: profileForm.legalName.trim(),
      cif: profileForm.cif.trim(),
      password: nextPassword || undefined,
    })

    organizerProfile.value = updatedProfile
    syncOrganizerProfileForm(updatedProfile)
    profileEditMode.value = false

    if (credentialChanged) {
      auth.clearSession()
      toast.showToast('Tus credenciales se han actualizado. Inicia sesion de nuevo para continuar.')
      await router.push('/')
      auth.openAuthDialog()
      return
    }

    toast.showToast('Tus datos se han actualizado correctamente.')
  } catch (requestError) {
    profileError.value = resolveOrganizerProfileError(requestError)
  } finally {
    profileSaving.value = false
  }
}

async function addLapTime() {
  lapSaving.value = true
  lapError.value = ''

  const trackId = Number(lapForm.trackId)
  if (!trackId) {
    lapSaving.value = false
    lapError.value = 'Selecciona un circuito.'
    return
  }

  if (!lapForm.lapDate) {
    lapSaving.value = false
    lapError.value = 'Selecciona la fecha de la vuelta.'
    return
  }

  const lapTimeMs = parseLapTimeInput(lapForm.lapTimeText)
  if (lapTimeMs === null) {
    lapSaving.value = false
    lapError.value = 'Introduce el tiempo con formato m:ss.mmm.'
    return
  }

  try {
    const createdLapTime = await createCurrentUserLapTime({
      trackId,
      lapDate: lapForm.lapDate,
      lapTimeMs,
      vehicle: lapForm.vehicle.trim() || undefined,
    })

    lapTimes.value = [...lapTimes.value, createdLapTime]
    await refreshTrackRankings(lapTimes.value)
    lapForm.trackId = ''
    lapForm.lapDate = ''
    lapForm.lapTimeText = ''
    lapForm.vehicle = ''
    toast.showToast('La vuelta se ha registrado correctamente.')
  } catch (requestError) {
    lapError.value = resolveLapTimeError(requestError)
  } finally {
    lapSaving.value = false
  }
}

async function removeLapTime(lapTime: LapTime) {
  lapError.value = ''

  try {
    await deleteCurrentUserLapTime(lapTime.id)
    lapTimes.value = lapTimes.value.filter((currentLapTime) => currentLapTime.id !== lapTime.id)
    await refreshTrackRankings(lapTimes.value)
    toast.showToast('La vuelta se ha eliminado correctamente.')
  } catch (requestError) {
    lapError.value = resolveLapTimeDeleteError(requestError)
  }
}

function parseLapTimeInput(value: string): number | null {
  const normalizedValue = value.trim()
  const match = normalizedValue.match(/^(\d+):([0-5]\d)\.(\d{3})$/)

  if (!match) {
    return null
  }

  const minutes = Number(match[1])
  const seconds = Number(match[2])
  const milliseconds = Number(match[3])

  return minutes * 60000 + seconds * 1000 + milliseconds
}

function resolveBookingCancellationError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo anular la reserva.',
    matches: [
      {
        includes: 'less than 14 days before the event',
        message: 'No puedes anular una reserva con menos de 14 dias de antelacion.',
      },
    ],
  })
}

function resolveBookingVisibilityError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo actualizar la visibilidad de la reserva.',
    statusMessages: {
      401: 'Tu sesion ha caducado. Inicia sesion de nuevo.',
      403: 'Solo puedes cambiar la visibilidad de tus propias reservas.',
    },
    matches: [
      {
        includes: 'owner of the booking',
        message: 'Solo puedes cambiar la visibilidad de tus propias reservas.',
      },
    ],
  })
}

function resolveProfileError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo actualizar tu perfil.',
    matches: [
      { includes: 'email', message: 'Ya existe una cuenta registrada con ese correo.' },
      { includes: 'phone', message: 'Ya existe una cuenta registrada con ese telefono.' },
    ],
  })
}

function resolveOrganizerProfileError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo actualizar tu perfil de organizador.',
    matches: [
      { includes: 'email', message: 'Ya existe una cuenta registrada con ese correo.' },
      { includes: 'phone', message: 'Ya existe una cuenta registrada con ese telefono.' },
      { includes: 'legal name', message: 'Ya existe un organizador con esa razon social.' },
      { includes: 'cif', message: 'Ya existe un organizador con ese CIF.' },
    ],
  })
}

function resolveLapTimeError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo registrar la vuelta.',
  })
}

function resolveLapTimeDeleteError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo eliminar la vuelta.',
  })
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Mi perfil</p>
      <h1 class="ui-title-section">Preparando tu espacio personal</h1>
      <p class="ui-copy-muted">Un momento, estamos cargando tus datos y tu historial.</p>
    </section>

    <section v-else-if="error" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Mi perfil</p>
      <h1 class="ui-title-section">Acceso restringido</h1>
      <p class="ui-copy-muted">{{ error }}</p>
      <RouterLink class="action-button" to="/">Volver al inicio</RouterLink>
    </section>

    <template v-else-if="profile || organizerProfile">
      <UserProfileHero
        v-if="isStandardUser && profile"
        :display-name="profile.displayName"
        :completed-events="pastBookings.length"
        :visited-circuits="uniqueVisitedTracks"
        :top-five-lap-times="topFiveLapTimes"
        :pole-count="poleCount"
      />

      <section v-else-if="organizerProfile" class="panel panel-pad-lg panel-stack-sm">
        <p class="ui-eyebrow">Mi perfil</p>
        <h1 class="ui-title-section">{{ organizerProfile.legalName }}</h1>
        <p class="ui-copy-muted">
          {{ organizerProfile.displayName }} ·
          {{ organizerProfile.organizerEnabled ? 'Organizador validado' : 'Pendiente de validacion' }}
        </p>
      </section>

      <section class="profile-tabs panel panel-pad-lg panel-stack-lg">
        <div class="pill-tabs" role="tablist" aria-label="Navegacion del perfil">
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

        <section v-if="activeTab === 'reservas'" class="panel-stack-lg">
          <p v-if="bookingError" class="status-message status-message--error">{{ bookingError }}</p>

          <div class="profile-grid">
            <article
              v-for="section in bookingSections"
              :key="section.id"
              class="panel panel-pad-lg panel-stack-sm"
            >
              <h3 class="ui-title-card">{{ section.title }}</h3>

              <p v-if="section.bookings.length === 0" class="ui-copy-muted">
                {{ section.emptyMessage }}
              </p>

              <div v-else class="profile-booking-list">
                <article
                  v-for="booking in section.bookings"
                  :key="booking.id"
                  class="profile-booking-card"
                >
                  <RouterLink class="profile-booking-card__main" :to="`/events/${booking.eventId}`">
                    <div class="panel-copy">
                      <p class="ui-eyebrow">{{ formatDisplayDate(booking.eventDate) }}</p>
                      <strong class="profile-booking-card__title">{{ booking.trackName }}</strong>
                      <p class="ui-copy-muted">{{ booking.organizerLegalName }}</p>
                    </div>
                  </RouterLink>

                  <div class="profile-booking-card__actions">
                    <div class="profile-booking-card__action-row">
                      <button
                        class="icon-button"
                        :class="getBookingVisibilityToneClass(booking.isVisible)"
                        type="button"
                        :disabled="bookingVisibilityUpdatingId === booking.id"
                        :aria-label="
                          booking.isVisible
                            ? 'Ocultar en perfil público'
                            : 'Mostrar en perfil público'
                        "
                        :title="
                          booking.isVisible
                            ? 'Ocultar en perfil público'
                            : 'Mostrar en perfil público'
                        "
                        @click="toggleBookingVisibility(booking)"
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
                      <template v-if="section.allowCancellation">
                        <button
                          v-if="canCancelBooking(booking)"
                          class="action-button profile-booking-card__cancel"
                          type="button"
                          :disabled="bookingCancellingId === booking.id"
                          @click="cancelBooking(booking)"
                        >
                          {{ bookingCancellingId === booking.id ? 'Anulando...' : 'Anular reserva' }}
                        </button>
                        <span v-else class="subtle-note profile-booking-card__note">
                          La anulacion se cierra 14 dias antes.
                        </span>
                      </template>
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

              <div v-else class="profile-booking-list">
                <article
                  v-for="booking in pastBookings"
                  :key="booking.id"
                  class="profile-booking-card"
                >
                  <RouterLink class="profile-booking-card__main" :to="`/events/${booking.eventId}`">
                    <p class="ui-eyebrow">{{ formatDisplayDate(booking.eventDate) }}</p>
                    <strong class="profile-booking-card__title">{{ booking.trackName }}</strong>
                    <p class="ui-copy-muted">{{ booking.organizerLegalName }}</p>
                  </RouterLink>

                  <div class="profile-booking-card__actions">
                    <button
                      class="icon-button"
                      :class="getBookingVisibilityToneClass(booking.isVisible)"
                      type="button"
                      :disabled="bookingVisibilityUpdatingId === booking.id"
                      :aria-label="
                        booking.isVisible
                          ? 'Ocultar en perfil público'
                          : 'Mostrar en perfil público'
                      "
                      :title="
                        booking.isVisible
                          ? 'Ocultar en perfil público'
                          : 'Mostrar en perfil público'
                      "
                      @click="toggleBookingVisibility(booking)"
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

        <section v-else-if="activeTab === 'perfil'" class="panel-stack-lg">
          <p v-if="profileError" class="status-message status-message--error">{{ profileError }}</p>

          <article class="panel panel-pad-lg panel-stack-lg">
            <div class="profile-detail-header">
              <div class="panel-copy">
                <h3 class="ui-title-card">Datos personales</h3>
              </div>

              <div class="profile-detail-header__actions">
                <button
                  v-if="!profileEditMode"
                  class="icon-button icon-button--danger"
                  type="button"
                  aria-label="Editar perfil"
                  title="Editar perfil"
                  @click="startProfileEdit"
                >
                  <Pencil :size="16" aria-hidden="true" />
                </button>

                <template v-else>
                  <button
                    class="action-button action-button--ghost"
                    type="button"
                    @click="cancelProfileEdit"
                  >
                    Cancelar
                  </button>
                  <button
                    class="action-button"
                    type="button"
                    :disabled="profileSaving"
                    @click="saveProfile"
                  >
                    {{ profileSaving ? 'Guardando...' : 'Guardar cambios' }}
                  </button>
                </template>
              </div>
            </div>

            <div v-if="profile" class="profile-detail-grid">
              <div class="profile-detail-item">
                <span class="profile-detail-item__label">Alias</span>
                <strong class="profile-detail-item__value">{{ profile.displayName }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Nombre</span>
                <input v-model="profileForm.name" type="text" autocomplete="given-name" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Nombre</span>
                <strong class="profile-detail-item__value">{{ profile.name }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Apellidos</span>
                <input v-model="profileForm.surname" type="text" autocomplete="family-name" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Apellidos</span>
                <strong class="profile-detail-item__value">{{ profile.surname }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Correo electr&oacute;nico</span>
                <input v-model="profileForm.email" type="email" autocomplete="email" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Correo electr&oacute;nico</span>
                <strong class="profile-detail-item__value">{{ profile.email }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Telefono</span>
                <input v-model="profileForm.phone" type="tel" autocomplete="tel" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Telefono</span>
                <strong class="profile-detail-item__value">{{ profile.phone }}</strong>
              </div>

              <div class="profile-detail-item">
                <span class="profile-detail-item__label">Alta</span>
                <strong class="profile-detail-item__value">
                  {{ formatDisplayDate(profile.created.slice(0, 10)) }}
                </strong>
              </div>

              <div class="profile-detail-item">
                <span class="profile-detail-item__label">Estado</span>
                <strong class="profile-detail-item__value">
                  {{ profile.enabled ? 'Activa' : 'Inactiva' }}
                </strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Direccion</span>
                <input
                  v-model="profileForm.address"
                  type="text"
                  autocomplete="street-address"
                />
              </label>
              <div v-else class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Direccion</span>
                <strong class="profile-detail-item__value">{{ profile.address }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Nueva contrasena</span>
                <input
                  v-model="profileForm.password"
                  type="password"
                  autocomplete="new-password"
                  placeholder="D&eacute;jala en blanco si no quieres cambiarla"
                />
              </label>
              <label v-if="profileEditMode" class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Confirmar nueva contrasena</span>
                <input
                  v-model="profileForm.passwordConfirmation"
                  type="password"
                  autocomplete="new-password"
                  placeholder="Repite la nueva contrasena"
                />
              </label>
              <div v-else class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Contrasena</span>
                <strong class="profile-detail-item__value">••••••••••••</strong>
              </div>
            </div>

            <div v-else-if="organizerProfile" class="profile-detail-grid">
              <div class="profile-detail-item">
                <span class="profile-detail-item__label">Alias</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.displayName }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Nombre</span>
                <input v-model="profileForm.name" type="text" autocomplete="given-name" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Nombre</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.name }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Apellidos</span>
                <input v-model="profileForm.surname" type="text" autocomplete="family-name" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Apellidos</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.surname }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Correo electr&oacute;nico</span>
                <input v-model="profileForm.email" type="email" autocomplete="email" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Correo electr&oacute;nico</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.email }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Telefono</span>
                <input v-model="profileForm.phone" type="tel" autocomplete="tel" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Telefono</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.phone }}</strong>
              </div>

              <div class="profile-detail-item">
                <span class="profile-detail-item__label">Alta</span>
                <strong class="profile-detail-item__value">
                  {{ organizerProfile.created ? formatDisplayDate(organizerProfile.created.slice(0, 10)) : 'Sin fecha' }}
                </strong>
              </div>

              <div class="profile-detail-item">
                <span class="profile-detail-item__label">Estado de cuenta</span>
                <strong class="profile-detail-item__value">
                  {{ organizerProfile.userEnabled ? 'Activa' : 'Inactiva' }}
                </strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Direccion</span>
                <input
                  v-model="profileForm.address"
                  type="text"
                  autocomplete="street-address"
                />
              </label>
              <div v-else class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Direccion</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.address }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">Razon social</span>
                <input v-model="profileForm.legalName" type="text" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">Razon social</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.legalName }}</strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item">
                <span class="profile-detail-item__label">CIF</span>
                <input v-model="profileForm.cif" type="text" />
              </label>
              <div v-else class="profile-detail-item">
                <span class="profile-detail-item__label">CIF</span>
                <strong class="profile-detail-item__value">{{ organizerProfile.cif }}</strong>
              </div>

              <div class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Estado de organizador</span>
                <strong class="profile-detail-item__value">
                  {{ organizerProfile.organizerEnabled ? 'Validado' : 'Pendiente de validacion' }}
                </strong>
              </div>

              <label v-if="profileEditMode" class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Nueva contrasena</span>
                <input
                  v-model="profileForm.password"
                  type="password"
                  autocomplete="new-password"
                  placeholder="D&eacute;jala en blanco si no quieres cambiarla"
                />
              </label>
              <label v-if="profileEditMode" class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Confirmar nueva contrasena</span>
                <input
                  v-model="profileForm.passwordConfirmation"
                  type="password"
                  autocomplete="new-password"
                  placeholder="Repite la nueva contrasena"
                />
              </label>
              <div v-else class="profile-detail-item profile-detail-item--full">
                <span class="profile-detail-item__label">Contrasena</span>
                <strong class="profile-detail-item__value">â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢</strong>
              </div>
            </div>
          </article>
        </section>

        <section v-else-if="activeTab === 'vueltas'" class="panel-stack-lg">
          <div class="panel-copy">
            <p class="ui-eyebrow">Vueltas</p>
            <h2 class="ui-title-section">Tus tiempos por vuelta</h2>
            <p class="ui-copy-muted">
              Registra nuevas vueltas y elimina las que ya no quieras conservar en tu historial.
            </p>
          </div>

          <p v-if="lapError" class="status-message status-message--error">{{ lapError }}</p>

          <div class="profile-grid">
            <article class="panel panel-pad-lg panel-stack-md">
              <div class="panel-copy">
                <p class="ui-eyebrow">Anadir</p>
                <h3 class="ui-title-card">Nueva vuelta</h3>
              </div>

              <form class="profile-form" @submit.prevent="addLapTime">
                <label class="profile-field">
                  <span>Circuito</span>
                  <select v-model="lapForm.trackId">
                    <option value="">Selecciona un circuito</option>
                    <option
                      v-for="trackOption in tracks"
                      :key="trackOption.id"
                      :value="String(trackOption.id)"
                    >
                      {{ trackOption.name }}
                    </option>
                  </select>
                </label>

                <label class="profile-field">
                  <span>Fecha</span>
                  <input v-model="lapForm.lapDate" type="date" />
                </label>

                <label class="profile-field">
                  <span>Tiempo</span>
                  <input v-model="lapForm.lapTimeText" type="text" placeholder="1:52.340" />
                </label>

                <label class="profile-field">
                  <span>Coche</span>
                  <input
                    v-model="lapForm.vehicle"
                    type="text"
                    maxlength="30"
                    placeholder="BMW M2"
                  />
                </label>

                <button
                  class="action-button profile-form__submit"
                  type="submit"
                  :disabled="lapSaving"
                >
                  {{ lapSaving ? 'Guardando...' : 'Registrar vuelta' }}
                </button>
              </form>
            </article>

            <article class="panel panel-pad-lg panel-stack-md">
              <div class="panel-copy">
                <p class="ui-eyebrow">Historial</p>
                <h3 class="ui-title-card">Vueltas registradas</h3>
              </div>

              <p v-if="sortedLapTimes.length === 0" class="ui-copy-muted">
                Todavia no has registrado tiempos por vuelta.
              </p>

              <div v-else class="profile-lap-list">
                <article v-for="lapTime in sortedLapTimes" :key="lapTime.id" class="profile-lap-card">
                  <div class="panel-copy">
                    <p class="ui-eyebrow">{{ formatDisplayDate(lapTime.lapDate) }}</p>
                    <strong class="profile-lap-card__time">{{ formatLapTime(lapTime.lapTimeMs) }}</strong>
                    <p class="ui-copy-muted">
                      {{ lapTime.trackName }}<span v-if="lapTime.vehicle"> · {{ lapTime.vehicle }}</span>
                    </p>
                  </div>

                  <button
                    class="icon-button icon-button--danger"
                    type="button"
                    aria-label="Eliminar vuelta"
                    title="Eliminar vuelta"
                    @click="removeLapTime(lapTime)"
                  >
                    <Trash2 :size="16" aria-hidden="true" />
                  </button>
                </article>
              </div>
            </article>
          </div>
        </section>

      </section>
    </template>

    <EventBookingDialog
      :is-open="bookingPendingCancellation !== null"
      mode="cancel"
      :track-name="bookingPendingCancellation?.trackName ?? ''"
      :event-date="
        bookingPendingCancellation ? formatDisplayDate(bookingPendingCancellation.eventDate) : ''
      "
      :base-price-label="
        bookingPendingCancellation
          ? formatCurrency(bookingPendingCancellation.basePriceAtPurchase)
          : ''
      "
      :total-price-label="
        bookingPendingCancellation
          ? formatCurrency(bookingPendingCancellation.basePriceAtPurchase)
          : ''
      "
      :selected-services="[]"
      :is-submitting="
        bookingPendingCancellation !== null &&
        bookingCancellingId === bookingPendingCancellation.id
      "
      :error-message="bookingError"
      :is-visible-on-public-profile="false"
      @close="closeBookingCancellationDialog"
      @confirm="confirmBookingCancellation"
    />
  </main>
</template>

<style scoped>
.profile-grid {
  display: grid;
  align-items: start;
  gap: var(--space-lg);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.profile-booking-list,
.profile-lap-list {
  display: grid;
  gap: var(--space-md);
}

.profile-booking-card,
.profile-lap-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.profile-booking-card__title {
  color: var(--text-strong);
  font-size: var(--fs-title-sm);
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

.profile-booking-card__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-sm);
  flex: none;
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

.profile-detail-header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-lg);
}

.profile-detail-header__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: end;
  gap: var(--space-sm);
}

.profile-detail-grid {
  display: grid;
  gap: var(--space-md);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.profile-detail-item {
  display: grid;
  gap: var(--space-xs);
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.profile-detail-item--full {
  grid-column: 1 / -1;
}

.profile-detail-item__label {
  color: var(--text-muted);
  font-size: var(--fs-caption);
  font-weight: 700;
}

.profile-detail-item__value {
  color: var(--text-strong);
  font-weight: 700;
  line-height: 1.45;
  word-break: break-word;
}

.profile-detail-item input {
  min-height: 50px;
  padding: 0 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  color: var(--text-body);
  background: var(--surface-glass);
}

.profile-detail-item input:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

.profile-form {
  display: grid;
  gap: var(--space-md);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.profile-field {
  display: grid;
  gap: var(--space-xs);
}

.profile-field--full,
.profile-form__submit {
  grid-column: 1 / -1;
}

.profile-field span {
  color: var(--text-muted);
  font-size: var(--fs-caption);
  font-weight: 700;
}

.profile-field input,
.profile-field select {
  min-height: 50px;
  padding: 0 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  color: var(--text-body);
  background: var(--surface-glass);
}

.profile-field select {
  appearance: none;
  background:
    linear-gradient(180deg, rgba(39, 16, 16, 0.98) 0%, rgba(24, 10, 10, 0.98) 100%);
}

.profile-field select option {
  color: var(--text-strong);
  background: #1b0c0c;
}

.profile-field input:focus,
.profile-field select:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

.profile-lap-card__time {
  color: var(--text-strong);
  font-size: var(--fs-title-sm);
}

@media (max-width: 980px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-detail-grid {
    grid-template-columns: 1fr;
  }

  .profile-detail-item--full {
    grid-column: auto;
  }
}

@media (max-width: 720px) {
  .profile-booking-card,
  .profile-lap-card {
    flex-direction: column;
    align-items: start;
  }

  .profile-booking-card__actions {
    width: 100%;
    justify-content: flex-start;
  }

  .profile-booking-card__action-row {
    width: 100%;
    justify-content: flex-start;
  }

  .profile-detail-header {
    flex-direction: column;
  }

  .profile-detail-header__actions {
    width: 100%;
    justify-content: stretch;
  }

  .profile-detail-header__actions .action-button {
    width: 100%;
  }

  .profile-form {
    grid-template-columns: 1fr;
  }

  .profile-field--full,
  .profile-form__submit {
    grid-column: auto;
  }
}
</style>
