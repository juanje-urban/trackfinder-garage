<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import EventBookingDialog from '@/components/EventBookingDialog.vue'
import UserProfileHero from '@/components/UserProfileHero.vue'
import OrganizerAccountDetailsPanel from '@/components/profile/OrganizerAccountDetailsPanel.vue'
import ProfileBookingsPanel from '@/components/profile/ProfileBookingsPanel.vue'
import ProfileLapTimesPanel from '@/components/profile/ProfileLapTimesPanel.vue'
import UserAccountDetailsPanel from '@/components/profile/UserAccountDetailsPanel.vue'
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
import type { LapTimeFormState, ProfileFormState } from '@/types/profile'
import type { Track } from '@/types/track'
import type { TrackRecord } from '@/types/trackRecord'
import type { UserProfile } from '@/types/user'
import { resolveApiErrorMessage } from '@/utils/apiErrors'
import { isOrganizerRole, isUserRole } from '@/utils/authRoles'
import { toIsoDate } from '@/utils/date'
import { formatCurrency, formatDisplayDate } from '@/utils/format'

type ProfileTab = 'reservas' | 'perfil' | 'vueltas'

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

const profileForm = reactive<ProfileFormState>({
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

const lapForm = reactive<LapTimeFormState>({
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
      .filter(
        (result): result is PromiseFulfilledResult<readonly [number, TrackRecord[]]> =>
          result.status === 'fulfilled',
      )
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
        ? 'La reserva vuelve a mostrarse en tu perfil publico.'
        : 'La reserva se ha ocultado de tu perfil publico.',
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

  if (
    (nextPassword !== '' || nextPasswordConfirmation !== '') &&
    nextPassword !== nextPasswordConfirmation
  ) {
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

  if (
    (nextPassword !== '' || nextPasswordConfirmation !== '') &&
    nextPassword !== nextPasswordConfirmation
  ) {
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
          {{ organizerProfile.displayName }} &middot;
          {{ organizerProfile.organizerEnabled ? 'Organizador validado' : 'Pendiente de validacion' }}
        </p>
      </section>

      <section class="panel panel-pad-lg panel-stack-lg">
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

        <ProfileBookingsPanel
          v-if="activeTab === 'reservas'"
          :booking-error="bookingError"
          :active-bookings="futureBookings"
          :past-bookings="pastBookings"
          :booking-cancelling-id="bookingCancellingId"
          :booking-visibility-updating-id="bookingVisibilityUpdatingId"
          :cancellation-cutoff-iso="cancellationCutoffIso"
          @request-cancel="cancelBooking"
          @toggle-visibility="toggleBookingVisibility"
        />

        <UserAccountDetailsPanel
          v-else-if="activeTab === 'perfil' && profile"
          :profile="profile"
          :profile-form="profileForm"
          :edit-mode="profileEditMode"
          :saving="profileSaving"
          :error-message="profileError"
          @edit="startProfileEdit"
          @cancel="cancelProfileEdit"
          @save="saveProfile"
        />

        <OrganizerAccountDetailsPanel
          v-else-if="activeTab === 'perfil' && organizerProfile"
          :organizer-profile="organizerProfile"
          :profile-form="profileForm"
          :edit-mode="profileEditMode"
          :saving="profileSaving"
          :error-message="profileError"
          @edit="startProfileEdit"
          @cancel="cancelProfileEdit"
          @save="saveProfile"
        />

        <ProfileLapTimesPanel
          v-else-if="activeTab === 'vueltas'"
          :lap-error="lapError"
          :lap-saving="lapSaving"
          :lap-form="lapForm"
          :tracks="tracks"
          :sorted-lap-times="sortedLapTimes"
          @submit="addLapTime"
          @remove="removeLapTime"
        />
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
