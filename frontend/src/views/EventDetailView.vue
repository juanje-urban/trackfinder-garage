<script setup lang="ts">
import { isAxiosError } from 'axios'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import DetailInfoCard from '@/components/DetailInfoCard.vue'
import EventAttendeeList from '@/components/EventAttendeeList.vue'
import EventAvailabilityBadge from '@/components/EventAvailabilityBadge.vue'
import EventBookingDialog from '@/components/EventBookingDialog.vue'
import SectionCard from '@/components/SectionCard.vue'
import TrackRecordBoard from '@/components/TrackRecordBoard.vue'
import { useAuth } from '@/composables/useAuth'
import {
  cancelEventBooking,
  checkoutEventBooking,
  getCurrentUserBookedServicesByEventId,
  getCurrentUserEventBookings,
} from '@/services/eventBookingService'
import {
  getEventById,
  getEventServicesByEventId,
} from '@/services/eventService'
import { getTrackById } from '@/services/trackService'
import type { Event } from '@/types/event'
import type { EventBooking } from '@/types/eventBooking'
import type { EventServiceItem } from '@/types/eventService'
import type { Track } from '@/types/track'
import {
  getEventAvailabilityLabel,
  getEventAvailabilityState,
  getEventRemainingLabel,
} from '@/utils/eventAvailability'
import { formatCurrency, formatDisplayDate } from '@/utils/format'
import { getTrackMedia } from '@/utils/trackMedia'
import { createVisualStyle, eventVisualPalettes } from '@/utils/visualPalettes'

type DisplayService = {
  id: number
  name: string
  price: number
  source: 'track' | 'organizer'
}

type MediaDialog = {
  eyebrow: string
  title: string
  src: string
  alt: string
}

type BookingDialogMode = 'checkout' | 'cancel'

const route = useRoute()
const auth = useAuth()

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
const activeMediaDialog = ref<MediaDialog | null>(null)

const eventId = computed(() => Number(route.params.id))
const todayIso = computed(() => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const eventDetail = computed(() => {
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

const availability = computed(() => {
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
  eventDetail.value ? getTrackMedia(eventDetail.value.event.trackName) : { gallery: [] },
)

const layoutImage = computed(() => trackMedia.value.layoutImage)
const secondGalleryImage = computed(() => trackMedia.value.gallery[1])

const availableServices = computed<DisplayService[]>(() =>
  eventServices.value
    .map((service) => ({
      id: service.id,
      name: service.trackServiceName ?? service.organizerServiceName ?? 'Servicio',
      price: service.price,
      source: service.trackServiceId ? ('track' as const) : ('organizer' as const),
    }))
    .sort((left, right) => left.name.localeCompare(right.name, 'es')),
)

const trackEventServices = computed(() =>
  availableServices.value.filter((service) => service.source === 'track'),
)

const organizerEventServices = computed(() =>
  availableServices.value.filter((service) => service.source === 'organizer'),
)

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
  eventDetail.value ? eventDetail.value.event.eventDate < todayIso.value : false,
)

const isUserSession = computed(() => auth.session.value?.roleName === 'USER')

const isBookingActionDisabled = computed(
  () =>
    isPastEvent.value ||
    bookingSubmitting.value ||
    (auth.isAuthenticated.value && !isUserSession.value),
)

const bookingButtonLabel = computed(() =>
  isPastEvent.value
    ? 'Evento finalizado'
    : hasConfirmedBooking.value
      ? 'Anular reserva'
      : 'Reservar plaza',
)

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
    return 'Puedes anular tu reserva siempre que falten al menos 14 dias para el evento.'
  }

  if (isUserSession.value) {
    return 'Puedes continuar con la reserva con los servicios seleccionados.'
  }

  if (!auth.isAuthenticated.value) {
    return 'Inicia sesion con una cuenta de usuario para reservar.'
  }

  return 'La reserva esta disponible solo para cuentas de usuario.'
})

const heroStyle = computed(() => {
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
  window.addEventListener('keydown', handleTrackMapDialogKeydown)

  if (Number.isNaN(eventId.value)) {
    error.value = 'No se pudo identificar el evento solicitado.'
    loading.value = false
    servicesLoading.value = false
    return
  }

  try {
    const selectedEvent = await getEventById(eventId.value)
    event.value = selectedEvent

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
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleTrackMapDialogKeydown)
})

watch(
  () => [auth.session.value?.userId ?? null, auth.session.value?.roleName ?? null] as const,
  async () => {
    if (Number.isNaN(eventId.value)) {
      return
    }

    try {
      event.value = await getEventById(eventId.value)
      await loadExistingBookingState(eventId.value)
    } catch {
      // Keep the current UI state if the refresh fails.
    }
  },
)

function toggleServiceSelection(serviceId: number) {
  if (hasConfirmedBooking.value) {
    return
  }

  if (selectedServiceIds.value.includes(serviceId)) {
    selectedServiceIds.value = selectedServiceIds.value.filter((id) => id !== serviceId)
    return
  }

  selectedServiceIds.value = [...selectedServiceIds.value, serviceId]
}

function isServiceSelected(serviceId: number): boolean {
  return selectedServiceIds.value.includes(serviceId)
}

async function loadExistingBookingState(selectedEventId: number) {
  const session = auth.session.value

  if (!session || session.roleName !== 'USER') {
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
    auth.openAuthDialog()
    return
  }

  if (!eventDetail.value || !isUserSession.value) {
    return
  }

  if (isPastEvent.value) {
    return
  }

  bookingDialogMode.value = hasConfirmedBooking.value ? 'cancel' : 'checkout'
  openBookingDialog()
}

async function confirmBookingDialogAction() {
  if (bookingDialogMode.value === 'cancel') {
    await confirmBookingCancellation()
    return
  }

  await confirmBookingCheckout()
}

async function confirmBookingCheckout() {
  const session = auth.session.value

  if (!session || !eventDetail.value || session.roleName !== 'USER') {
    return
  }

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

    if (event.value) {
      event.value = {
        ...event.value,
        remainingCapacity: Math.max(0, event.value.remainingCapacity - 1),
      }
    }

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
  const session = auth.session.value
  const booking = existingBooking.value

  if (!session || !booking || !eventDetail.value || session.roleName !== 'USER') {
    return
  }

  bookingSubmitting.value = true
  bookingDialogError.value = ''

  try {
    await cancelEventBooking(booking.id)
    existingBooking.value = null
    selectedServiceIds.value = []
    bookingVisibleOnPublicProfile.value = false

    if (event.value) {
      event.value = {
        ...event.value,
        remainingCapacity: event.value.remainingCapacity + 1,
      }
    }

    const updatedEvent = await getEventById(eventDetail.value.event.id)
    event.value = updatedEvent
    bookingDialogOpen.value = false
  } catch (requestError) {
    bookingDialogError.value = resolveBookingCancellationError(requestError)
  } finally {
    bookingSubmitting.value = false
  }
}

function resolveBookingError(requestError: unknown): string {
  if (!isAxiosError(requestError)) {
    return 'No se pudo confirmar la reserva.'
  }

  const backendMessage = requestError.response?.data?.error

  if (typeof backendMessage === 'string') {
    if (backendMessage.includes('already has a booking')) {
      return 'Ya tienes una reserva confirmada para este evento.'
    }

    if (backendMessage.includes('is full')) {
      return 'No quedan plazas disponibles para este evento.'
    }

    if (backendMessage.includes('Only standard users')) {
      return 'Solo las cuentas de usuario pueden reservar plaza.'
    }

    return backendMessage
  }

  if (requestError.response?.status === 401) {
    return 'Tu sesion ha caducado. Inicia sesion de nuevo.'
  }

  if (requestError.response?.status === 403) {
    return 'Solo las cuentas de usuario pueden reservar plaza.'
  }

  return 'No se pudo confirmar la reserva.'
}

function resolveBookingCancellationError(requestError: unknown): string {
  if (!isAxiosError(requestError)) {
    return 'No se pudo anular la reserva.'
  }

  const backendMessage = requestError.response?.data?.error

  if (typeof backendMessage === 'string') {
    if (backendMessage.includes('less than 14 days before the event')) {
      return 'No puedes anular la reserva con menos de 14 dias de antelacion.'
    }

    if (backendMessage.includes('owner of the booking')) {
      return 'Solo puedes anular tus propias reservas.'
    }

    return backendMessage
  }

  if (requestError.response?.status === 401) {
    return 'Tu sesion ha caducado. Inicia sesion de nuevo.'
  }

  if (requestError.response?.status === 403) {
    return 'Solo puedes anular tus propias reservas.'
  }

  return 'No se pudo anular la reserva.'
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
    alt: `Imagen del circuito ${eventDetail.value.track.name}`,
  }
}

function closeTrackMapDialog() {
  activeMediaDialog.value = null
}

function handleTrackMapDialogKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeTrackMapDialog()
  }
}
</script>

<template>
  <main class="page-shell section-stack">
    <section v-if="loading" class="panel panel-pad-lg panel-stack-sm">
      <p class="ui-eyebrow">Cargando</p>
      <h1 class="ui-title-section">Preparando el detalle del evento</h1>
      <p class="ui-copy-muted">Un momento, estamos reuniendo la informacion del track day.</p>
    </section>

    <section
      v-else-if="error || !eventDetail || !availability"
      class="panel panel-pad-lg panel-stack-sm"
    >
      <p class="ui-eyebrow">Eventos</p>
      <h1 class="ui-title-section">Detalle no disponible</h1>
      <p class="ui-copy-muted">{{ error || 'No se pudo cargar el evento solicitado.' }}</p>
      <RouterLink class="action-button" to="/events">Volver al catalogo</RouterLink>
    </section>

    <template v-else>
      <nav class="event-detail__breadcrumbs subtle-note" aria-label="Miga de pan">
        <RouterLink to="/events">Eventos</RouterLink>
        <span>/</span>
        <span>{{ eventDetail.event.trackName }}</span>
      </nav>

      <section class="event-detail__hero-layout">
        <article class="event-detail__hero panel">
          <div class="event-detail__hero-media" :style="heroStyle">
            <div class="media-card__badges">
              <EventAvailabilityBadge :remaining-capacity="eventDetail.event.remainingCapacity" />
            </div>

            <div class="event-detail__hero-copy">
              <h1 class="ui-title-hero">{{ eventDetail.event.trackName }}</h1>
              <p class="event-detail__hero-location">{{ eventDetail.track.location }}</p>
            </div>
          </div>

          <div class="event-detail__hero-description panel-copy">
            <p class="ui-eyebrow">Evento</p>
            <p class="ui-copy-body">{{ eventDetail.event.description }}</p>
          </div>
        </article>

        <aside class="event-detail__hero-side">
          <DetailInfoCard
            eyebrow="Disponibilidad"
            :title="availability.label"
            :subtitle="availability.remainingLabel"
            :tone="availability.state"
          />
          <DetailInfoCard eyebrow="Fecha" :title="formattedDate" />
          <DetailInfoCard eyebrow="Organiza" :title="eventDetail.event.organizerLegalName" />
          <article
            v-if="layoutImage"
            class="event-detail__track-map panel panel-pad-lg panel-stack-sm"
          >
            <p class="ui-eyebrow">Trazado</p>
            <button
              class="event-detail__track-map-button"
              type="button"
              :aria-label="`Ampliar trazado de ${eventDetail.event.trackName}`"
              @click="openTrackMapDialog"
            >
              <img
                class="event-detail__track-map-image"
                :src="layoutImage"
                :alt="`Trazado de ${eventDetail.event.trackName}`"
              />
            </button>
            <span class="event-detail__track-map-hint">Pulsa para ampliar</span>
          </article>
        </aside>
      </section>

      <section class="event-detail__content-grid">
        <div class="event-detail__main">
          <article class="section-card panel panel-pad-lg panel-stack-lg">
            <div
              class="event-detail__circuit-overview"
              :class="{ 'event-detail__circuit-overview--with-photo': secondGalleryImage }"
            >
              <div class="panel-copy event-detail__circuit-copy">
                <p class="ui-eyebrow">Circuito</p>
                <h2 class="ui-title-section">{{ eventDetail.track.name }}</h2>
                <p class="ui-copy-muted">{{ eventDetail.track.location }}</p>
                <p class="ui-copy-body">{{ eventDetail.track.description }}</p>
              </div>

              <button
                v-if="secondGalleryImage"
                class="event-detail__circuit-photo-button"
                type="button"
                :aria-label="`Ampliar imagen de ${eventDetail.track.name}`"
                @click="openCircuitPhotoDialog"
              >
                <img
                  class="event-detail__circuit-photo"
                  :src="secondGalleryImage"
                  :alt="`Vista del circuito ${eventDetail.track.name}`"
                />
                <span class="event-detail__circuit-photo-hint">Pulsa para ampliar</span>
              </button>
            </div>
          </article>

          <SectionCard
            v-if="!isPastEvent"
            eyebrow="Servicios"
            title="Configura tu reserva"
            description="Selecciona los servicios disponibles del circuito y del organizador para este evento."
          >
            <div v-if="servicesLoading" class="status-message">
              Cargando servicios disponibles...
            </div>
            <p v-else-if="servicesError" class="status-message status-message--error">
              {{ servicesError }}
            </p>
            <p v-else-if="availableServices.length === 0" class="status-message">
              No hay servicios adicionales disponibles para este evento.
            </p>
            <div v-else class="event-detail__service-groups">
              <article class="event-detail__service-group">
                <div class="panel-copy">
                  <p class="ui-eyebrow">Circuito</p>
                  <h3 class="ui-title-card">Servicios del circuito</h3>
                </div>

                <div v-if="trackEventServices.length > 0" class="event-detail__service-list">
                  <label
                    v-for="service in trackEventServices"
                    :key="service.id"
                    class="event-detail__service-row"
                    :class="{
                      'event-detail__service-row--selected': isServiceSelected(service.id),
                      'event-detail__service-row--locked': hasConfirmedBooking,
                    }"
                  >
                    <input
                      class="event-detail__service-checkbox"
                      type="checkbox"
                      :checked="isServiceSelected(service.id)"
                      :disabled="hasConfirmedBooking"
                      @change="toggleServiceSelection(service.id)"
                    />
                    <span class="event-detail__service-name">{{ service.name }}</span>
                    <strong>{{ formatCurrency(service.price) }}</strong>
                  </label>
                </div>
                <p v-else class="ui-copy-muted">Sin servicios de circuito en esta fecha.</p>
              </article>

              <article class="event-detail__service-group">
                <div class="panel-copy">
                  <p class="ui-eyebrow">Organizador</p>
                  <h3 class="ui-title-card">Servicios del organizador</h3>
                </div>

                <div v-if="organizerEventServices.length > 0" class="event-detail__service-list">
                  <label
                    v-for="service in organizerEventServices"
                    :key="service.id"
                    class="event-detail__service-row"
                    :class="{
                      'event-detail__service-row--selected': isServiceSelected(service.id),
                      'event-detail__service-row--locked': hasConfirmedBooking,
                    }"
                  >
                    <input
                      class="event-detail__service-checkbox"
                      type="checkbox"
                      :checked="isServiceSelected(service.id)"
                      :disabled="hasConfirmedBooking"
                      @change="toggleServiceSelection(service.id)"
                    />
                    <span class="event-detail__service-name">{{ service.name }}</span>
                    <strong>{{ formatCurrency(service.price) }}</strong>
                  </label>
                </div>
                <p v-else class="ui-copy-muted">Sin servicios del organizador en esta fecha.</p>
              </article>
            </div>
          </SectionCard>
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

          <article class="event-detail__booking-card panel panel-pad-lg panel-stack-lg">
            <div class="panel-copy">
              <p class="ui-eyebrow">Reserva</p>
              <h2 class="ui-title-price">{{ formatCurrency(totalPrice) }}</h2>
              <p class="ui-copy-meta">Total estimado con la seleccion actual</p>
            </div>

            <p v-if="isPastEvent" class="event-detail__booking-status">
              Evento finalizado
            </p>

            <div class="event-detail__booking-lines">
              <div class="event-detail__booking-line">
                <span>Entrada base</span>
                <strong>{{ formattedPrice }}</strong>
              </div>

              <div
                v-for="service in selectedServices"
                :key="service.id"
                class="event-detail__booking-line event-detail__booking-line--selected"
              >
                <span>{{ service.name }}</span>
                <strong>{{ formatCurrency(service.price) }}</strong>
              </div>
            </div>

            <p v-if="selectedServices.length === 0" class="ui-copy-muted">
              No has seleccionado servicios adicionales.
            </p>

            <button
              class="action-button event-detail__booking-cta"
              type="button"
              :disabled="isBookingActionDisabled"
              @click="handleBookingAction"
            >
              {{ bookingButtonLabel }}
            </button>

            <p class="ui-copy-caption">{{ bookingCaption }}</p>
          </article>
        </aside>
      </section>

      <div
        v-if="activeMediaDialog"
        class="track-map-dialog"
        role="dialog"
        aria-modal="true"
        :aria-label="activeMediaDialog.alt"
        @click.self="closeTrackMapDialog"
      >
        <section class="track-map-dialog__panel panel panel-pad-lg panel-stack-md">
          <div class="track-map-dialog__header">
            <div class="panel-copy">
              <p class="ui-eyebrow">{{ activeMediaDialog.eyebrow }}</p>
              <h2 class="ui-title-section">{{ activeMediaDialog.title }}</h2>
            </div>
            <button class="track-map-dialog__close" type="button" @click="closeTrackMapDialog">
              Cerrar
            </button>
          </div>

          <div class="track-map-dialog__image-wrap">
            <img
              class="track-map-dialog__image"
              :src="activeMediaDialog.src"
              :alt="activeMediaDialog.alt"
            />
          </div>
        </section>
      </div>

      <EventBookingDialog
        :is-open="bookingDialogOpen"
        :mode="bookingDialogMode"
        :track-name="eventDetail.event.trackName"
        :event-date="formattedDate"
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

.event-detail__hero-layout {
  display: grid;
  gap: var(--space-xl);
  grid-template-columns: minmax(0, 1.85fr) minmax(280px, 0.95fr);
  align-items: start;
}

.event-detail__hero {
  overflow: hidden;
}

.event-detail__hero-media {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: clamp(20px, 3vw, 32px);
  background-position: center;
  background-size: cover;
}

.event-detail__hero-copy {
  display: grid;
  gap: var(--space-sm);
  max-width: min(640px, 100%);
}

.event-detail__hero-date,
.event-detail__hero-location {
  margin: 0;
  color: rgba(255, 246, 242, 0.92);
  font-size: var(--fs-body);
}

.event-detail__hero-description {
  padding: var(--space-xl);
}

.event-detail__hero-side {
  display: grid;
  gap: var(--space-lg);
}

.event-detail__track-map {
  overflow: hidden;
  border-color: rgba(48, 17, 15, 0.12);
  background: var(--surface-light);
  color: var(--text-on-light);
}

.event-detail__track-map .ui-eyebrow {
  color: var(--accent);
}

.event-detail__track-map-button {
  padding: 0;
  border: 0;
  background: transparent;
  width: 100%;
  cursor: zoom-in;
}

.event-detail__track-map-hint {
  color: rgba(48, 17, 15, 0.62);
  font-size: var(--fs-caption);
}

.event-detail__track-map-image {
  display: block;
  width: 100%;
  max-height: 220px;
  object-fit: contain;
  border-radius: var(--radius-inner);
  background: var(--surface-light);
}

.track-map-dialog {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(8, 8, 10, 0.74);
  backdrop-filter: blur(10px);
}

.track-map-dialog__panel {
  width: min(920px, calc(100vw - 32px));
  max-height: calc(100vh - 32px);
  overflow: auto;
  border-color: rgba(48, 17, 15, 0.12);
  background: var(--surface-light);
  color: var(--text-on-light);
}

.track-map-dialog__header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-lg);
}

.track-map-dialog__header .ui-eyebrow {
  color: var(--accent);
}

.track-map-dialog__close {
  padding: 10px 14px;
  border: 1px solid rgba(48, 17, 15, 0.14);
  border-radius: var(--radius-sm);
  background: rgba(48, 17, 15, 0.06);
  color: var(--text-on-light);
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

.event-detail__content-grid {
  display: grid;
  gap: var(--space-xl);
  grid-template-columns: minmax(0, 1.7fr) minmax(300px, 0.9fr);
  align-items: start;
}

.event-detail__main {
  display: grid;
  gap: var(--space-xl);
}

.event-detail__booking-status {
  margin: 0;
  padding: var(--space-sm) var(--space-md);
  border: 1px solid rgba(255, 160, 72, 0.24);
  border-radius: var(--radius-pill);
  background: rgba(255, 160, 72, 0.12);
  color: var(--racing-amber);
  font-size: var(--fs-caption);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  width: fit-content;
}

.event-detail__circuit-overview {
  display: grid;
  gap: var(--space-lg);
}

.event-detail__circuit-copy {
  display: grid;
  gap: var(--space-sm);
  align-content: start;
}

.event-detail__circuit-overview--with-photo {
  grid-template-columns: minmax(0, 1.3fr) minmax(240px, 0.9fr);
  align-items: start;
}

.event-detail__circuit-overview .ui-copy-body {
  margin: 0;
}

.event-detail__circuit-photo-button {
  display: grid;
  gap: var(--space-sm);
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: zoom-in;
}

.event-detail__circuit-photo {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: var(--radius-inner);
  border: 1px solid var(--line-soft);
}

.event-detail__circuit-photo-hint {
  color: var(--text-muted);
  font-size: var(--fs-caption);
}

.event-detail__booking-side {
  display: grid;
  gap: var(--space-lg);
}

.event-detail__booking-card {
  border-color: var(--line-strong);
}

.event-detail__booking-lines {
  display: grid;
  gap: var(--space-sm);
}

.event-detail__booking-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  background: var(--surface-glass);
}

.event-detail__booking-line strong {
  color: var(--text-strong);
}

.event-detail__booking-line--selected {
  border-color: var(--line-strong);
  background: var(--accent-soft);
}

.event-detail__booking-cta {
  width: 100%;
}

.event-detail__service-groups {
  display: grid;
  gap: var(--space-lg);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.event-detail__service-group {
  display: grid;
  gap: var(--space-lg);
  padding: var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass-subtle);
}

.event-detail__service-list {
  display: grid;
  gap: var(--space-sm);
}

.event-detail__service-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  background: var(--surface-glass);
}

.event-detail__service-row--selected {
  border-color: var(--line-strong);
  background:
    linear-gradient(180deg, rgba(255, 45, 32, 0.1) 0%, rgba(255, 45, 32, 0.03) 100%);
}

.event-detail__service-row--locked {
  opacity: 0.82;
}

.event-detail__service-checkbox {
  accent-color: var(--accent-strong);
}

.event-detail__service-name {
  min-width: 0;
  color: var(--text-body);
}

.event-detail__service-row strong {
  color: var(--text-strong);
}

@media (max-width: 1080px) {
  .event-detail__content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .event-detail__hero-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .event-detail__service-groups {
    grid-template-columns: 1fr;
  }

  .event-detail__circuit-overview--with-photo {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .event-detail__hero-media {
    min-height: 320px;
    padding: var(--space-lg);
  }

  .event-detail__hero-description {
    padding: var(--space-lg);
  }

  .track-map-dialog {
    padding: 16px;
  }

  .track-map-dialog__header {
    flex-direction: column;
  }

  .event-detail__booking-line,
  .event-detail__service-row {
    grid-template-columns: 1fr;
    align-items: start;
  }
}
</style>
