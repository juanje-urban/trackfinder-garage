<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AppModal from '@/components/AppModal.vue'
import EventAttendeeList from '@/components/EventAttendeeList.vue'
import EventBookingDialog from '@/components/EventBookingDialog.vue'
import EventBookingSummaryCard from '@/components/event-detail/EventBookingSummaryCard.vue'
import EventDetailCircuitOverview from '@/components/event-detail/EventDetailCircuitOverview.vue'
import EventDetailHero from '@/components/event-detail/EventDetailHero.vue'
import EventServiceConfigurator from '@/components/event-detail/EventServiceConfigurator.vue'
import TrackRecordBoard from '@/components/TrackRecordBoard.vue'
import { useAuth } from '@/composables/useAuth'
import {
  cancelEventBooking,
  checkoutEventBooking,
  getCurrentUserBookedServicesByEventId,
  getCurrentUserEventBookings,
} from '@/services/eventBookingService'
import { getEventById, getEventServicesByEventId } from '@/services/eventService'
import { getTrackById } from '@/services/trackService'
import type { Event } from '@/types/event'
import type { EventBooking } from '@/types/eventBooking'
import type { EventServiceItem } from '@/types/eventService'
import type {
  DisplayService,
  DisplayServiceGroup,
  EventAvailabilitySummary,
  MediaDialogState,
} from '@/types/eventDetail'
import type { Track } from '@/types/track'
import { resolveApiErrorMessage } from '@/utils/apiErrors'
import {
  getEventAvailabilityLabel,
  getEventAvailabilityState,
  getEventRemainingLabel,
} from '@/utils/eventAvailability'
import { isUserRole } from '@/utils/authRoles'
import { toIsoDate } from '@/utils/date'
import { formatCurrency, formatDisplayDate } from '@/utils/format'
import { getTrackMedia } from '@/utils/trackMedia'
import { createVisualStyle, eventVisualPalettes } from '@/utils/visualPalettes'

type BookingDialogMode = 'checkout' | 'cancel'

const route = useRoute()
const auth = useAuth()

// Estado principal de la pantalla. Separar carga general y carga de servicios permite mostrar errores parciales.
const loading = ref(true)
const error = ref('')
const servicesLoading = ref(true)
const servicesError = ref('')
const bookingDialogOpen = ref(false)
const bookingDialogMode = ref<BookingDialogMode>('checkout')
const bookingDialogError = ref('')
const bookingSubmitting = ref(false)
const bookingVisibleOnPublicProfile = ref(false)
const event = ref<Event | null>(null)
const track = ref<Track | null>(null)
const eventServices = ref<EventServiceItem[]>([])
const existingBooking = ref<EventBooking | null>(null)
const selectedServiceIds = ref<number[]>([])
const activeMediaDialog = ref<MediaDialogState | null>(null)

const eventId = computed(() => Number(route.params.id))
const todayIso = toIsoDate(new Date())

const eventDetail = computed(() => {
  // Junto evento y circuito cuando ambos están disponibles para simplificar el template.
  if (!event.value || !track.value) {
    return null
  }

  return {
    event: event.value,
    track: track.value,
  }
})

const formattedDate = computed(() =>
  eventDetail.value ? formatDisplayDate(eventDetail.value.event.eventDate) : '',
)

const formattedPrice = computed(() =>
  eventDetail.value ? formatCurrency(eventDetail.value.event.basePrice) : '',
)

const availability = computed<EventAvailabilitySummary | null>(() => {
  // Transformo plazas restantes en textos y estado visual reutilizable.
  if (!eventDetail.value) {
    return null
  }

  const remainingCapacity = eventDetail.value.event.remainingCapacity
  return {
    state: getEventAvailabilityState(remainingCapacity),
    label: getEventAvailabilityLabel(remainingCapacity),
    remainingLabel: getEventRemainingLabel(remainingCapacity),
  }
})

const trackMedia = computed(() =>
  eventDetail.value
    ? getTrackMedia(eventDetail.value.event.trackShortName)
    : { gallery: [] },
)

const layoutImage = computed(() => trackMedia.value.layoutImage)
const secondGalleryImage = computed(() => trackMedia.value.gallery[1])

const availableServices = computed<DisplayService[]>(() =>
  // Normalizo servicios de circuito y organizador en una forma común para pintarlos juntos.
  eventServices.value
    .flatMap((service) => {
      const name = service.trackServiceName ?? service.organizerServiceName

      if (!name) {
        return []
      }

      return [{
        id: service.id,
        name,
        price: service.price,
        source: service.trackServiceId ? ('track' as const) : ('organizer' as const),
      }]
    })
    .sort((left, right) => left.name.localeCompare(right.name, 'es')),
)

const serviceGroups = computed<DisplayServiceGroup[]>(() => [
  {
    id: 'track',
    eyebrow: 'Circuito',
    title: 'Servicios del circuito',
    emptyMessage: 'Sin servicios de circuito en esta fecha.',
    services: availableServices.value.filter((service) => service.source === 'track'),
  },
  {
    id: 'organizer',
    eyebrow: 'Organizador',
    title: 'Servicios del organizador',
    emptyMessage: 'Sin servicios del organizador en esta fecha.',
    services: availableServices.value.filter((service) => service.source === 'organizer'),
  },
])

const selectedServices = computed(() =>
  availableServices.value.filter((service) => selectedServiceIds.value.includes(service.id)),
)

const selectedServicesPrice = computed(() =>
  selectedServices.value.reduce((sum, service) => sum + service.price, 0),
)

const totalPrice = computed(() =>
  eventDetail.value ? eventDetail.value.event.basePrice + selectedServicesPrice.value : 0,
)

const hasConfirmedBooking = computed(() => existingBooking.value !== null)
const isPastEvent = computed(() =>
  eventDetail.value ? eventDetail.value.event.eventDate < todayIso : false,
)
const cancellationCutoffIso = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + 14)
  return toIsoDate(date)
})
const isEventFull = computed(() =>
  eventDetail.value ? eventDetail.value.event.remainingCapacity <= 0 : false,
)
const canCancelExistingBooking = computed(() =>
  hasConfirmedBooking.value && eventDetail.value
    ? eventDetail.value.event.eventDate >= cancellationCutoffIso.value
    : false,
)
const isUserSession = computed(() => isUserRole(auth.session.value?.roleName))
const hasBookableSession = computed(() => auth.isAuthenticated.value && isUserSession.value)

const isBookingActionDisabled = computed(
  () =>
    isPastEvent.value ||
    (hasConfirmedBooking.value ? !canCancelExistingBooking.value : isEventFull.value) ||
    bookingSubmitting.value ||
    (auth.isAuthenticated.value && !isUserSession.value),
)

const bookingButtonLabel = computed(() => {
  if (isPastEvent.value) {
    return 'Evento finalizado'
  }

  if (hasConfirmedBooking.value) {
    return 'Anular reserva'
  }

  if (isEventFull.value) {
    return 'Aforo completo'
  }

  return 'Reservar plaza'
})

const bookingDialogServices = computed(() =>
  selectedServices.value.map((service) => ({
    id: service.id,
    name: service.name,
    priceLabel: formatCurrency(service.price),
  })),
)

const bookingCaption = computed(() => {
  if (isPastEvent.value) {
    return 'Este evento ya se ha celebrado y la reserva queda bloqueada para cambios.'
  }

  if (hasConfirmedBooking.value) {
    return canCancelExistingBooking.value
      ? 'Puedes anular tu reserva siempre que falten al menos 14 días para el evento.'
      : 'La anulación ya no está disponible porque faltan menos de 14 días para el evento.'
  }

  if (isEventFull.value) {
    return 'El aforo está completo y ya no se admiten nuevas reservas para este evento.'
  }

  if (isUserSession.value) {
    return 'Puedes continuar con la reserva con los servicios seleccionados.'
  }

  if (!auth.isAuthenticated.value) {
    return 'Inicia sesión con una cuenta de usuario para reservar.'
  }

  return 'La reserva está disponible solo para cuentas de usuario.'
})

const heroStyle = computed(() => {
  // Si el circuito tiene imagen, la uso; si no, genero una paleta visual estable.
  if (!eventDetail.value) {
    return {}
  }

  if (trackMedia.value.coverImage) {
    return {
      backgroundImage: `linear-gradient(180deg, rgba(8, 10, 14, 0.16) 0%, rgba(8, 10, 14, 0.74) 100%), url(${trackMedia.value.coverImage})`,
      backgroundPosition: 'center',
      backgroundSize: 'cover',
    }
  }

  return createVisualStyle(
    eventDetail.value.event.id,
    eventVisualPalettes,
    '--event-detail-start',
    '--event-detail-end',
    {
      backgroundImage:
        'radial-gradient(circle at top right, rgba(255, 191, 60, 0.18), transparent 32%), linear-gradient(140deg, var(--event-detail-start), var(--event-detail-end))',
    },
  )
})

onMounted(async () => {
  // Además de cargar datos, registro Escape para cerrar el modal de imágenes.
  globalThis.window.addEventListener('keydown', handleMediaDialogKeydown)

  if (Number.isNaN(eventId.value)) {
    error.value = 'No se pudo identificar el evento solicitado.'
    loading.value = false
    servicesLoading.value = false
    return
  }

  await loadEventDetail()
})

onUnmounted(() => {
  globalThis.window.removeEventListener('keydown', handleMediaDialogKeydown)
})

watch(
  () => [auth.session.value?.userId ?? null, auth.session.value?.roleName ?? null] as const,
  async () => {
    // Si cambia la sesión, refresco reserva/plazas porque el botón puede cambiar completamente.
    if (Number.isNaN(eventId.value)) {
      return
    }

    try {
      event.value = await getEventById(eventId.value)
      await loadExistingBookingState(eventId.value)
    } catch {
      // Mantiene el estado actual de la interfaz de usuario si falla la actualización.
    }
  },
)

async function loadEventDetail() {
  try {
    const selectedEvent = await getEventById(eventId.value)
    event.value = selectedEvent

    // El circuito y los servicios son independientes, así que los pido en paralelo.
    const [trackResult, servicesResult] = await Promise.allSettled([
      getTrackById(selectedEvent.trackId),
      getEventServicesByEventId(selectedEvent.id),
    ])

    if (trackResult.status === 'rejected') {
      error.value = 'No se pudo cargar el circuito del evento.'
      return
    }

    track.value = trackResult.value

    if (servicesResult.status === 'fulfilled') {
      eventServices.value = servicesResult.value
    } else {
      servicesError.value = 'No se pudieron cargar los servicios disponibles.'
    }

    await loadExistingBookingState(selectedEvent.id)
  } catch {
    error.value = 'No se pudo cargar el detalle del evento.'
  } finally {
    loading.value = false
    servicesLoading.value = false
  }
}

function toggleServiceSelection(serviceId: number) {
  if (hasConfirmedBooking.value) {
    return
  }

  // Alterno el id dentro del array; Vue detecta el cambio al asignar un array nuevo.
  selectedServiceIds.value = selectedServiceIds.value.includes(serviceId)
    ? selectedServiceIds.value.filter((id) => id !== serviceId)
    : [...selectedServiceIds.value, serviceId]
}

async function loadExistingBookingState(selectedEventId: number) {
  // Solo una cuenta de usuario estándar puede tener reserva propia en un evento.
  if (!hasBookableSession.value) {
    existingBooking.value = null
    selectedServiceIds.value = []
    bookingVisibleOnPublicProfile.value = false
    return
  }

  const [bookingsResult, bookedServicesResult] = await Promise.allSettled([
    getCurrentUserEventBookings(),
    getCurrentUserBookedServicesByEventId(selectedEventId),
  ])

  if (bookingsResult.status === 'fulfilled') {
    existingBooking.value =
      bookingsResult.value.find((booking) => booking.eventId === selectedEventId) ?? null
    bookingVisibleOnPublicProfile.value = existingBooking.value?.isVisible ?? false
  }

  if (bookedServicesResult.status === 'fulfilled' && existingBooking.value) {
    selectedServiceIds.value = bookedServicesResult.value.map((service) => service.eventServiceId)
  }
}

function openBookingDialog() {
  bookingDialogError.value = ''
  bookingDialogOpen.value = true
}

function closeBookingDialog() {
  if (bookingSubmitting.value) {
    return
  }

  bookingDialogError.value = ''
  bookingDialogOpen.value = false
}

function handleBookingAction() {
  if (!auth.isAuthenticated.value) {
    // Si no hay sesión, no navego fuera: abro el diálogo global de login.
    auth.openAuthDialog()
    return
  }

  if (
    !eventDetail.value ||
    !isUserSession.value ||
    isPastEvent.value ||
    (hasConfirmedBooking.value ? !canCancelExistingBooking.value : isEventFull.value)
  ) {
    return
  }

  bookingDialogMode.value = hasConfirmedBooking.value ? 'cancel' : 'checkout'
  openBookingDialog()
}

async function confirmBookingDialogAction() {
  if (bookingDialogMode.value === 'cancel') {
    await confirmBookingCancellation()
  } else {
    await confirmBookingCheckout()
  }
}

async function confirmBookingCheckout() {
  if (!eventDetail.value || !hasBookableSession.value) {
    return
  }

  // Mientras envío la reserva bloqueo botones para evitar dobles clicks.
  bookingSubmitting.value = true
  bookingDialogError.value = ''

  try {
    const createdBooking = await checkoutEventBooking({
      eventId: eventDetail.value.event.id,
      eventServiceIds: selectedServiceIds.value,
      isVisible: bookingVisibleOnPublicProfile.value,
    })

    existingBooking.value = createdBooking
    bookingVisibleOnPublicProfile.value = createdBooking.isVisible

    const [updatedEventResult, bookedServicesResult] = await Promise.allSettled([
      getEventById(eventDetail.value.event.id),
      getCurrentUserBookedServicesByEventId(eventDetail.value.event.id),
    ])

    if (updatedEventResult.status === 'fulfilled') {
      event.value = updatedEventResult.value
    }

    if (bookedServicesResult.status === 'fulfilled') {
      selectedServiceIds.value = bookedServicesResult.value.map((service) => service.eventServiceId)
    }

    bookingDialogOpen.value = false
  } catch (requestError) {
    bookingDialogError.value = resolveBookingError(requestError)
  } finally {
    bookingSubmitting.value = false
  }
}

async function confirmBookingCancellation() {
  const booking = existingBooking.value

  if (!booking || !eventDetail.value || !hasBookableSession.value) {
    return
  }

  bookingSubmitting.value = true
  bookingDialogError.value = ''

  try {
    await cancelEventBooking(booking.id)
    // Limpio el estado local antes de recargar plazas para que la UI responda al momento.
    existingBooking.value = null
    selectedServiceIds.value = []
    bookingVisibleOnPublicProfile.value = false

    event.value = await getEventById(eventDetail.value.event.id)
    bookingDialogOpen.value = false
  } catch (requestError) {
    bookingDialogError.value = resolveBookingCancellationError(requestError)
  } finally {
    bookingSubmitting.value = false
  }
}

function resolveBookingError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo confirmar la reserva.',
    statusMessages: {
      401: 'Tu sesión ha caducado. Inicia sesión de nuevo.',
      403: 'Solo las cuentas de usuario pueden reservar plaza.',
    },
    matches: [
      { includes: 'already has a booking', message: 'Ya tienes una reserva confirmada para este evento.' },
      { includes: 'is full', message: 'No quedan plazas disponibles para este evento.' },
      { includes: 'Only standard users', message: 'Solo las cuentas de usuario pueden reservar plaza.' },
    ],
  })
}

function resolveBookingCancellationError(requestError: unknown): string {
  return resolveApiErrorMessage(requestError, {
    fallback: 'No se pudo anular la reserva.',
    statusMessages: {
      401: 'Tu sesión ha caducado. Inicia sesión de nuevo.',
      403: 'Solo puedes anular tus propias reservas.',
    },
    matches: [
      { includes: 'less than 14 days before the event', message: 'No puedes anular la reserva con menos de 14 días de antelación.' },
      { includes: 'owner of the booking', message: 'Solo puedes anular tus propias reservas.' },
    ],
  })
}

function openTrackMapDialog() {
  if (!layoutImage.value || !eventDetail.value) {
    return
  }

  activeMediaDialog.value = {
    eyebrow: 'Trazado',
    title: eventDetail.value.event.trackName,
    src: layoutImage.value,
    alt: `Trazado ampliado de ${eventDetail.value.event.trackName}`,
  }
}

function openCircuitPhotoDialog() {
  if (!secondGalleryImage.value || !eventDetail.value) {
    return
  }

  activeMediaDialog.value = {
    eyebrow: 'Circuito',
    title: eventDetail.value.track.name,
    src: secondGalleryImage.value,
    alt: `Vista del circuito ${eventDetail.value.track.name}`,
  }
}

function closeMediaDialog() {
  activeMediaDialog.value = null
}

function handleMediaDialogKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeMediaDialog()
  }
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Cargando</p>
      <h1 class="ui-title-section">Preparando el detalle del evento</h1>
      <p class="ui-copy-muted">Un momento, estamos reuniendo la información del track day.</p>
    </section>

    <section
      v-else-if="error || !eventDetail || !availability"
      class="panel panel-pad-lg panel-stack-sm"
    >
      <p class="ui-eyebrow">Eventos</p>
      <h1 class="ui-title-section">Detalle no disponible</h1>
      <p class="ui-copy-muted">{{ error || 'No se pudo cargar el evento solicitado.' }}</p>
      <RouterLink class="action-button" to="/events">Volver al catálogo</RouterLink>
    </section>

    <template v-else>
      <nav class="event-detail__breadcrumbs subtle-note" aria-label="Miga de pan">
        <RouterLink to="/events">Eventos</RouterLink>
        <span>/</span>
        <span>{{ eventDetail.event.trackName }}</span>
      </nav>

      <EventDetailHero
        :event="eventDetail.event"
        :track="eventDetail.track"
        :availability="availability"
        :formatted-date="formattedDate"
        :hero-style="heroStyle"
        :layout-image="layoutImage"
        @open-layout="openTrackMapDialog"
      />

      <section class="event-detail__content-grid">
        <div class="event-detail__main">
          <EventDetailCircuitOverview
            :track="eventDetail.track"
            :second-gallery-image="secondGalleryImage"
            @open-photo="openCircuitPhotoDialog"
          />

          <EventServiceConfigurator
            :is-past-event="isPastEvent"
            :services-loading="servicesLoading"
            :services-error="servicesError"
            :service-groups="serviceGroups"
            :selected-service-ids="selectedServiceIds"
            :is-locked="hasConfirmedBooking"
            @toggle-service="toggleServiceSelection"
          />
        </div>

        <aside class="event-detail__booking-side">
          <TrackRecordBoard
            :track-id="eventDetail.event.trackId"
            :track-name="eventDetail.event.trackName"
            :limit="5"
            eyebrow="Top 5"
          />

          <EventAttendeeList
            v-if="auth.isAuthenticated"
            :event-id="eventDetail.event.id"
            :track-id="eventDetail.event.trackId"
          />

          <EventBookingSummaryCard
            :is-past-event="isPastEvent"
            :total-price-label="formatCurrency(totalPrice)"
            :formatted-price="formattedPrice"
            :selected-services="selectedServices"
            :booking-button-label="bookingButtonLabel"
            :booking-caption="bookingCaption"
            :is-booking-action-disabled="isBookingActionDisabled"
            @booking-action="handleBookingAction"
          />
        </aside>
      </section>

      <AppModal
        :is-open="activeMediaDialog !== null"
        :ariaLabel="activeMediaDialog?.alt ?? 'Vista ampliada'"
        :eyebrow="activeMediaDialog?.eyebrow ?? ''"
        :title="activeMediaDialog?.title ?? ''"
        width="920px"
        light
        @close="closeMediaDialog"
      >
        <div class="track-map-dialog__image-wrap">
          <img
            v-if="activeMediaDialog"
            class="track-map-dialog__image"
            :src="activeMediaDialog.src"
            :alt="activeMediaDialog.alt"
          />
        </div>
      </AppModal>

      <EventBookingDialog
        :is-open="bookingDialogOpen"
        :mode="bookingDialogMode"
        :track-name="eventDetail.event.trackName"
        :event-date="formattedDate"
        :can-confirm="bookingDialogMode === 'checkout' || canCancelExistingBooking"
        :base-price-label="formattedPrice"
        :total-price-label="formatCurrency(totalPrice)"
        :selected-services="bookingDialogServices"
        :is-submitting="bookingSubmitting"
        :error-message="bookingDialogError"
        :is-visible-on-public-profile="bookingVisibleOnPublicProfile"
        @update:is-visible-on-public-profile="bookingVisibleOnPublicProfile = $event"
        @close="closeBookingDialog"
        @confirm="confirmBookingDialogAction"
      />
    </template>
  </main>
</template>

<style scoped>
.event-detail__breadcrumbs {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-xs);
}

.event-detail__breadcrumbs a {
  color: inherit;
  text-decoration: none;
}

.event-detail__content-grid {
  display: grid;
  gap: var(--space-xl);
  grid-template-columns: minmax(0, 1.7fr) minmax(300px, 0.9fr);
  align-items: start;
}

.event-detail__main,
.event-detail__booking-side {
  display: grid;
  gap: var(--space-xl);
}

.event-detail__booking-side {
  gap: var(--space-lg);
}

.track-map-dialog__image-wrap {
  border-radius: var(--radius-inner);
  background: var(--surface-light);
}

.track-map-dialog__image {
  display: block;
  width: 100%;
  max-height: calc(100vh - 180px);
  object-fit: contain;
  background: var(--surface-light);
}

@media (max-width: 1080px) {
  .event-detail__content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
