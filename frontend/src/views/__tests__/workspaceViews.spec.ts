import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdminView from '@/views/AdminView.vue'
import EventDetailView from '@/views/EventDetailView.vue'
import MessagesView from '@/views/MessagesView.vue'
import OrganizerView from '@/views/OrganizerView.vue'
import PublicUserProfileView from '@/views/PublicUserProfileView.vue'
import UserProfileView from '@/views/UserProfileView.vue'
import {
  cancelEventBooking,
  checkoutEventBooking,
  getBookedServicesByBookingId,
  getCurrentUserBookedServicesByEventId,
  getCurrentUserEventBookings,
  getEventBookingsByUserId,
  updateOwnEventBookingVisibility,
} from '@/services/eventBookingService'
import { getEventById, getEventServicesByEventId } from '@/services/eventService'
import {
  createCurrentUserLapTime,
  deleteCurrentUserLapTime,
  getCurrentUserLapTimes,
  getLapTimesByUserId,
} from '@/services/lapTimeService'
import {
  createOwnMessage,
  getMessageContacts,
  getOwnMessages,
  markOwnMessageAsRead,
} from '@/services/messageService'
import {
  deleteOrganizer,
  enableOrganizer,
  getCurrentOrganizerProfile,
  getOrganizers,
  updateCurrentOrganizerProfile,
} from '@/services/organizerService'
import {
  addOrganizerCatalogService,
  createOrganizerEvent,
  deleteOrganizerEvent,
  getOrganizerWorkspace,
  removeOrganizerCatalogService,
  updateOrganizerEvent,
} from '@/services/organizerWorkspaceService'
import {
  createServiceCatalogItem,
  disableServiceCatalogItem,
  getServices,
  updateServiceCatalogItem,
} from '@/services/serviceCatalogService'
import { createTrack, getTrackById, getTrackRanking, getTracks, updateTrack } from '@/services/trackService'
import {
  createTrackServiceAssignment,
  deleteTrackServiceAssignment,
  getTrackServiceAssignments,
} from '@/services/trackServiceAssignmentService'
import {
  disableUser,
  getCurrentUserProfile,
  getPublicUserProfile,
  getUsers,
  updateCurrentUserProfile,
} from '@/services/userService'
import type { Event } from '@/types/event'
import type { EventBooking, EventBookedService } from '@/types/eventBooking'
import type { EventServiceItem } from '@/types/eventService'
import type { LapTime } from '@/types/lapTime'
import type { MessageContact, MessageItem } from '@/types/message'
import type { Organizer } from '@/types/organizer'
import type { OrganizerWorkspace } from '@/types/organizerWorkspace'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'
import type { Track } from '@/types/track'
import type { TrackRecord } from '@/types/trackRecord'
import type { UserProfile } from '@/types/user'

const mocks = vi.hoisted(() => ({
  route: {
    params: {} as Record<string, unknown>,
    query: {} as Record<string, unknown>,
  },
  router: {
    push: vi.fn(),
    replace: vi.fn(),
  },
  auth: {
    session: { value: null as null | { userId: number; displayName: string; email: string; roleName: string | null; authorizationHeader: string } },
    isAuthenticated: { value: false },
    isDialogOpen: { value: false },
    openAuthDialog: vi.fn(),
    closeAuthDialog: vi.fn(),
    setSession: vi.fn(),
    clearSession: vi.fn(),
    refreshSession: vi.fn(),
  },
  toast: {
    showToast: vi.fn(),
  },
  messageInbox: {
    syncMessages: vi.fn(),
    refreshUnreadCount: vi.fn(),
  },
}))

vi.mock('vue-router', () => ({
  RouterLink: {
    props: ['to'],
    template: '<a><slot /></a>',
  },
  useRoute: () => mocks.route,
  useRouter: () => mocks.router,
}))

vi.mock('@/composables/useAuth', () => ({
  useAuth: vi.fn(() => mocks.auth),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn(() => mocks.toast),
}))

vi.mock('@/composables/useMessageInbox', () => ({
  useMessageInbox: vi.fn(() => mocks.messageInbox),
}))

vi.mock('@/services/eventBookingService', () => ({
  cancelEventBooking: vi.fn(),
  checkoutEventBooking: vi.fn(),
  getBookedServicesByBookingId: vi.fn(),
  getCurrentUserBookedServicesByEventId: vi.fn(),
  getCurrentUserEventBookings: vi.fn(),
  getEventBookingsByUserId: vi.fn(),
  updateOwnEventBookingVisibility: vi.fn(),
}))

vi.mock('@/services/eventService', () => ({
  getEventById: vi.fn(),
  getEventServicesByEventId: vi.fn(),
}))

vi.mock('@/services/lapTimeService', () => ({
  createCurrentUserLapTime: vi.fn(),
  deleteCurrentUserLapTime: vi.fn(),
  getCurrentUserLapTimes: vi.fn(),
  getLapTimesByUserId: vi.fn(),
}))

vi.mock('@/services/messageService', () => ({
  createOwnMessage: vi.fn(),
  getMessageContacts: vi.fn(),
  getOwnMessages: vi.fn(),
  markOwnMessageAsRead: vi.fn(),
}))

vi.mock('@/services/organizerService', () => ({
  deleteOrganizer: vi.fn(),
  enableOrganizer: vi.fn(),
  getCurrentOrganizerProfile: vi.fn(),
  getOrganizers: vi.fn(),
  updateCurrentOrganizerProfile: vi.fn(),
}))

vi.mock('@/services/organizerWorkspaceService', () => ({
  addOrganizerCatalogService: vi.fn(),
  createOrganizerEvent: vi.fn(),
  deleteOrganizerEvent: vi.fn(),
  getOrganizerWorkspace: vi.fn(),
  removeOrganizerCatalogService: vi.fn(),
  updateOrganizerEvent: vi.fn(),
}))

vi.mock('@/services/serviceCatalogService', () => ({
  createServiceCatalogItem: vi.fn(),
  disableServiceCatalogItem: vi.fn(),
  enableServiceCatalogItem: vi.fn(),
  getServices: vi.fn(),
  updateServiceCatalogItem: vi.fn(),
}))

vi.mock('@/services/trackService', () => ({
  createTrack: vi.fn(),
  getTrackById: vi.fn(),
  getTrackRanking: vi.fn(),
  getTracks: vi.fn(),
  updateTrack: vi.fn(),
}))

vi.mock('@/services/trackServiceAssignmentService', () => ({
  createTrackServiceAssignment: vi.fn(),
  deleteTrackServiceAssignment: vi.fn(),
  getTrackServiceAssignments: vi.fn(),
}))

vi.mock('@/services/userService', () => ({
  disableUser: vi.fn(),
  enableUser: vi.fn(),
  getCurrentUserProfile: vi.fn(),
  getPublicUserProfile: vi.fn(),
  getUsers: vi.fn(),
  updateCurrentUserProfile: vi.fn(),
}))

vi.mock('lucide-vue-next', () => ({
  MessageSquare: { template: '<span />' },
}))

const globalStubs = {
  PageHero: {
    props: ['title', 'description'],
    template: '<section><h1>{{ title }}</h1><p>{{ description }}</p><slot /></section>',
  },
  MetricCard: {
    props: ['label', 'value'],
    template: '<article>{{ label }}: {{ value }}</article>',
  },
  AppModal: {
    props: ['isOpen', 'title', 'eyebrow'],
    template: '<section v-if="isOpen"><p>{{ eyebrow }}</p><h2>{{ title }}</h2><slot /></section>',
  },
  AdminOrganizerRequestsPanel: {
    props: ['pendingOrganizers'],
    emits: ['approve', 'deny'],
    template: `
      <section>
        Solicitudes {{ pendingOrganizers.length }}
        <button data-test="approve-organizer" type="button" @click="$emit('approve', pendingOrganizers[0])">Aprobar</button>
        <button data-test="deny-organizer" type="button" @click="$emit('deny', pendingOrganizers[0])">Denegar</button>
      </section>
    `,
  },
  AdminTracksPanel: {
    emits: ['open-create-track', 'add-assignment', 'remove-assignment'],
    props: ['sortedTracks', 'selectedTrackAssignments'],
    template: `
      <section>
        Circuitos {{ sortedTracks.length }}
        <button data-test="open-track-modal" type="button" @click="$emit('open-create-track')">Nuevo circuito</button>
        <button data-test="add-assignment" type="button" @click="$emit('add-assignment')">Vincular</button>
        <button data-test="remove-assignment" type="button" @click="$emit('remove-assignment', selectedTrackAssignments[0])">Retirar</button>
      </section>
    `,
  },
  AdminServicesPanel: {
    emits: ['open-create-service', 'toggle-service'],
    props: ['sortedServices'],
    template: `
      <section>
        Servicios {{ sortedServices.length }}
        <button data-test="open-service-modal" type="button" @click="$emit('open-create-service')">Nuevo servicio</button>
        <button data-test="toggle-service" type="button" @click="$emit('toggle-service', sortedServices[0])">Alternar</button>
      </section>
    `,
  },
  AdminUsersPanel: {
    emits: ['toggle-user'],
    props: ['standardUsers'],
    template: '<section>Usuarios {{ standardUsers.length }}<button data-test="toggle-user" type="button" @click="$emit(\'toggle-user\', standardUsers[0])">Alternar usuario</button></section>',
  },
  OrganizerServicesPanel: {
    emits: ['add-service', 'remove-service'],
    props: ['organizerServices', 'serviceError'],
    template: '<section>Servicios organizador {{ organizerServices.length }}<p>{{ serviceError }}</p><button data-test="add-catalog-service" type="button" @click="$emit(\'add-service\')">Anadir</button><button data-test="remove-catalog-service" type="button" @click="$emit(\'remove-service\', organizerServices[0]?.id)">Retirar</button></section>',
  },
  OrganizerEventsPanel: {
    emits: ['create', 'edit', 'delete'],
    props: ['events'],
    template: '<section>Eventos {{ events.length }}<button data-test="create-event" type="button" @click="$emit(\'create\')">Crear</button><button data-test="edit-event" type="button" @click="$emit(\'edit\', events[0])">Editar</button><button data-test="delete-event" type="button" @click="$emit(\'delete\', events[0])">Eliminar</button></section>',
  },
  OrganizerStatsPanel: {
    props: ['revenueChartBars'],
    template: '<section>Stats {{ revenueChartBars.length }}</section>',
  },
  UserProfileHero: {
    props: ['displayName'],
    emits: ['email-action'],
    template: '<section>{{ displayName }}<button data-test="email-action" type="button" @click="$emit(\'email-action\')">Mensaje</button></section>',
  },
  ProfileBookingsPanel: {
    emits: ['view-booking', 'toggle-visibility'],
    props: ['activeBookings'],
    template: '<section>Reservas {{ activeBookings.length }}<button data-test="view-booking" type="button" @click="$emit(\'view-booking\', activeBookings[0])">Ver</button><button data-test="toggle-visibility" type="button" @click="$emit(\'toggle-visibility\', activeBookings[0])">Visibilidad</button></section>',
  },
  UserAccountDetailsPanel: {
    emits: ['edit', 'save', 'cancel'],
    template: '<section>Perfil usuario<button data-test="edit-profile" type="button" @click="$emit(\'edit\')">Editar</button><button data-test="save-profile" type="button" @click="$emit(\'save\')">Guardar</button></section>',
  },
  OrganizerAccountDetailsPanel: {
    emits: ['edit', 'save'],
    template: '<section>Perfil organizador<button data-test="edit-organizer-profile" type="button" @click="$emit(\'edit\')">Editar</button><button data-test="save-organizer-profile" type="button" @click="$emit(\'save\')">Guardar</button></section>',
  },
  ProfileLapTimesPanel: {
    emits: ['submit', 'remove'],
    props: ['sortedLapTimes', 'lapError'],
    template: '<section>Vueltas {{ sortedLapTimes.length }}<p>{{ lapError }}</p><button data-test="add-lap" type="button" @click="$emit(\'submit\')">Anadir vuelta</button><button data-test="remove-lap" type="button" @click="$emit(\'remove\', sortedLapTimes[0])">Eliminar vuelta</button></section>',
  },
  ProfileBookingDetailDialog: {
    props: ['isOpen'],
    emits: ['cancel', 'close'],
    template: '<section v-if="isOpen"><button data-test="cancel-booking" type="button" @click="$emit(\'cancel\')">Cancelar reserva</button></section>',
  },
  RankingPositionBadge: {
    props: ['position'],
    template: '<strong>Pos {{ position }}</strong>',
  },
  EventDetailHero: {
    emits: ['open-layout'],
    props: ['event'],
    template: '<section>{{ event.trackName }}<button data-test="open-layout" type="button" @click="$emit(\'open-layout\')">Mapa</button></section>',
  },
  EventDetailCircuitOverview: {
    emits: ['open-photo'],
    props: ['track'],
    template: '<section>{{ track.name }}<button data-test="open-photo" type="button" @click="$emit(\'open-photo\')">Foto</button></section>',
  },
  EventServiceConfigurator: {
    emits: ['toggle-service'],
    props: ['serviceGroups'],
    template: '<section>Servicios evento {{ serviceGroups.length }}<button data-test="toggle-event-service" type="button" @click="$emit(\'toggle-service\', 1)">Servicio</button></section>',
  },
  EventBookingSummaryCard: {
    emits: ['booking-action'],
    props: ['bookingButtonLabel'],
    template: '<section>{{ bookingButtonLabel }}<button data-test="booking-action" type="button" @click="$emit(\'booking-action\')">Reservar</button></section>',
  },
  EventBookingDialog: {
    props: ['isOpen'],
    emits: ['confirm', 'close', 'update:is-visible-on-public-profile'],
    template: '<section v-if="isOpen"><button data-test="confirm-booking" type="button" @click="$emit(\'confirm\')">Confirmar</button></section>',
  },
  TrackRecordBoard: {
    template: '<section>Ranking</section>',
  },
  EventAttendeeList: {
    template: '<section>Asistentes</section>',
  },
  MessageThreadSidebar: {
    props: ['threads'],
    emits: ['compose', 'open-thread'],
    template: '<aside>Hilos {{ threads.length }}<button data-test="compose-message" type="button" @click="$emit(\'compose\')">Nuevo</button><button data-test="open-thread" type="button" @click="$emit(\'open-thread\', threads[0])">Abrir</button></aside>',
  },
  MessageComposePanel: {
    emits: ['update:receiver-id', 'update:subject', 'update:message', 'submit', 'cancel'],
    props: ['sendError'],
    template: '<section>Compose<p>{{ sendError }}</p><button data-test="fill-message" type="button" @click="$emit(\'update:receiver-id\', \'2\'); $emit(\'update:subject\', \'Jarama\'); $emit(\'update:message\', \'Hola\')">Rellenar</button><button data-test="submit-message" type="button" @click="$emit(\'submit\')">Enviar</button></section>',
  },
  MessageConversationPanel: {
    emits: ['update:reply-body', 'submit'],
    props: ['sendError'],
    template: '<section>Conversation<p>{{ sendError }}</p><button data-test="fill-reply" type="button" @click="$emit(\'update:reply-body\', \'Respuesta\')">Rellenar respuesta</button><button data-test="reply-message" type="button" @click="$emit(\'submit\')">Responder</button></section>',
  },
}

function mountView(component: object) {
  return mount(component, {
    global: {
      stubs: globalStubs,
    },
  })
}

function event(overrides: Partial<Event> = {}): Event {
  return {
    id: 1,
    organizerId: 2,
    organizerLegalName: 'TrackEvents S.L.',
    trackId: 1,
    trackName: 'Jarama',
    trackShortName: 'jarama',
    eventDate: '2026-07-12',
    basePrice: 120,
    maxParticipants: 20,
    remainingCapacity: 10,
    description: 'Tandas libres',
    ...overrides,
  }
}

function track(overrides: Partial<Track> = {}): Track {
  return {
    id: 1,
    name: 'Circuito del Jarama',
    shortName: 'jarama',
    location: 'Madrid',
    description: 'Circuito madrileno',
    ...overrides,
  }
}

function service(overrides: Partial<ServiceCatalogItem> = {}): ServiceCatalogItem {
  return {
    id: 1,
    name: 'Box',
    description: 'Box privado',
    allowedForTrack: true,
    allowedForOrganizer: true,
    enabled: true,
    ...overrides,
  }
}

function organizer(overrides: Partial<Organizer> = {}): Organizer {
  return {
    idUser: 3,
    displayName: 'trackevents',
    email: 'organizer@example.com',
    name: 'Track',
    surname: 'Events',
    address: 'Calle Motor',
    phone: '600000001',
    created: '2026-01-01',
    userEnabled: true,
    roleId: 2,
    roleName: 'ORGANIZER',
    legalName: 'TrackEvents S.L.',
    cif: 'B12345678',
    organizerEnabled: false,
    ...overrides,
  }
}

function user(overrides: Partial<UserProfile> = {}): UserProfile {
  return {
    id: 1,
    displayName: 'Juanje',
    email: 'juanje@example.com',
    created: '2026-01-01',
    enabled: true,
    name: 'Juan',
    surname: 'Urban',
    address: 'Calle Motor',
    phone: '600000000',
    roleId: 1,
    roleName: 'USER',
    ...overrides,
  }
}

function booking(overrides: Partial<EventBooking> = {}): EventBooking {
  return {
    id: 9,
    userId: 1,
    userDisplayName: 'Juanje',
    eventId: 1,
    eventDate: '2026-07-12',
    organizerLegalName: 'TrackEvents S.L.',
    trackName: 'Jarama',
    bookedAt: '2026-05-01T10:00:00',
    basePriceAtPurchase: 120,
    isVisible: true,
    ...overrides,
  }
}

function lapTime(overrides: Partial<LapTime> = {}): LapTime {
  return {
    id: 5,
    userId: 1,
    userDisplayName: 'Juanje',
    trackId: 1,
    trackName: 'Jarama',
    lapDate: '2026-06-01',
    lapTimeMs: 102345,
    vehicle: 'MX-5',
    ...overrides,
  }
}

function trackRecord(overrides: Partial<TrackRecord> = {}): TrackRecord {
  return {
    trackId: 1,
    trackName: 'Jarama',
    userDisplayName: 'Juanje',
    lapDate: '2026-06-01',
    lapTimeMs: 102345,
    vehicle: 'MX-5',
    ...overrides,
  }
}

function eventService(overrides: Partial<EventServiceItem> = {}): EventServiceItem {
  return {
    id: 1,
    eventId: 1,
    trackServiceId: 1,
    trackServiceName: 'Box del circuito',
    organizerServiceId: null,
    organizerServiceName: null,
    price: 30,
    hasBookings: false,
    ...overrides,
  }
}

function bookedService(overrides: Partial<EventBookedService> = {}): EventBookedService {
  return {
    id: 3,
    eventBookingId: 9,
    userId: 1,
    eventId: 1,
    eventServiceId: 1,
    priceAtPurchase: 30,
    ...overrides,
  }
}

function message(overrides: Partial<MessageItem> = {}): MessageItem {
  return {
    id: 1,
    senderId: 2,
    senderDisplayName: 'TrackEvents',
    receiverId: 1,
    receiverDisplayName: 'Juanje',
    sentAt: '2026-05-01T10:00:00',
    isRead: false,
    subject: 'Jarama',
    message: 'Hola',
    ...overrides,
  }
}

function contact(overrides: Partial<MessageContact> = {}): MessageContact {
  return {
    id: 2,
    displayName: 'TrackEvents',
    roleName: 'ORGANIZER',
    ...overrides,
  }
}

function workspace(): OrganizerWorkspace {
  return {
    organizer: organizer({ organizerEnabled: true }),
    availableServices: [service({ id: 2, name: 'Catering' })],
    organizerServices: [{
      id: 11,
      organizerId: 3,
      organizerLegalName: 'TrackEvents S.L.',
      serviceId: 1,
      serviceName: 'Box',
    }],
    tracks: [track()],
    trackServices: [{
      id: 1,
      trackId: 1,
      trackName: 'Jarama',
      serviceId: 1,
      serviceName: 'Box del circuito',
    }],
    events: [{
      event: event(),
      services: [eventService()],
      stats: {
        eventId: 1,
        trackName: 'Jarama',
        eventDate: '2026-07-12',
        bookings: 0,
        soldServices: 0,
        remainingCapacity: 10,
        totalCapacity: 20,
        baseRevenue: 0,
        serviceRevenue: 0,
        grossRevenue: 0,
      },
    }],
    stats: {
      totalBaseRevenue: 120,
      totalServiceRevenue: 30,
      totalGrossRevenue: 150,
      totalBookings: 1,
      totalSoldServices: 1,
      futureEvents: 1,
      pastEvents: 0,
      totalCapacity: 20,
      totalRemainingCapacity: 10,
      eventStats: [{
        eventId: 1,
        trackName: 'Jarama',
        eventDate: '2026-07-12',
        bookings: 1,
        soldServices: 1,
        remainingCapacity: 10,
        totalCapacity: 20,
        baseRevenue: 120,
        serviceRevenue: 30,
        grossRevenue: 150,
      }],
    },
  }
}

describe('workspace views', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.route.params = {}
    mocks.route.query = {}
    mocks.auth.session.value = {
      userId: 1,
      displayName: 'Juanje',
      email: 'juanje@example.com',
      roleName: 'USER',
      authorizationHeader: 'Basic token',
    }
    mocks.auth.isAuthenticated.value = true
  })

  it('loads the admin workspace and delegates tab actions', async () => {
    mocks.auth.session.value = { ...mocks.auth.session.value!, roleName: 'ADMIN' }
    vi.mocked(getOrganizers).mockResolvedValue([organizer()])
    vi.mocked(getTracks).mockResolvedValue([track()])
    vi.mocked(getServices).mockResolvedValue([service()])
    vi.mocked(getTrackServiceAssignments).mockResolvedValue([{
      id: 1,
      trackId: 1,
      trackName: 'Jarama',
      serviceId: 1,
      serviceName: 'Box',
    }])
    vi.mocked(getUsers).mockResolvedValue([
      user({ roleName: 'ADMIN', email: 'admin@example.com' }),
      user({ id: 2, displayName: 'Piloto', roleName: 'USER' }),
    ])
    vi.mocked(enableOrganizer).mockResolvedValue(organizer({ organizerEnabled: true }))
    vi.mocked(deleteOrganizer).mockResolvedValue()
    vi.mocked(createTrack).mockResolvedValue(track({ id: 5 }))
    vi.mocked(updateTrack).mockResolvedValue(track())
    vi.mocked(createTrackServiceAssignment).mockResolvedValue({
      id: 2,
      trackId: 1,
      trackName: 'Jarama',
      serviceId: 1,
      serviceName: 'Box',
    })
    vi.mocked(deleteTrackServiceAssignment).mockResolvedValue()
    vi.mocked(createServiceCatalogItem).mockResolvedValue(service({ id: 3 }))
    vi.mocked(updateServiceCatalogItem).mockResolvedValue(service())
    vi.mocked(disableServiceCatalogItem).mockResolvedValue(service({ enabled: false }))
    vi.mocked(disableUser).mockResolvedValue(user({ id: 2, enabled: false }))

    const wrapper = mountView(AdminView)
    await flushPromises()

    expect(wrapper.text()).toContain('Solicitudes 1')
    await wrapper.get('[data-test="approve-organizer"]').trigger('click')
    await flushPromises()
    expect(enableOrganizer).toHaveBeenCalledWith(3)

    await wrapper.findAll('.pill-tab')[1].trigger('click')
    await wrapper.get('[data-test="open-track-modal"]').trigger('click')
    expect(wrapper.text()).toContain('Nuevo circuito')
    const trackInputs = wrapper.findAll('input[type="text"]')
    await trackInputs[0].setValue('Nuevo Jarama')
    await trackInputs[1].setValue('nuevo_jarama')
    await trackInputs[2].setValue('Madrid')
    await wrapper.get('textarea').setValue('Nuevo trazado')
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(createTrack).toHaveBeenCalledWith({
      name: 'Nuevo Jarama',
      shortName: 'nuevo_jarama',
      location: 'Madrid',
      description: 'Nuevo trazado',
    })

    await wrapper.findAll('.pill-tab')[2].trigger('click')
    await wrapper.get('[data-test="open-service-modal"]').trigger('click')
    await wrapper.get('input[type="text"]').setValue('Hospitality')
    await wrapper.get('textarea').setValue('Zona cubierta')
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(createServiceCatalogItem).toHaveBeenCalled()

    await wrapper.get('[data-test="toggle-service"]').trigger('click')
    await flushPromises()
    expect(disableServiceCatalogItem).toHaveBeenCalledWith(1)

    await wrapper.findAll('.pill-tab')[3].trigger('click')
    await wrapper.get('[data-test="toggle-user"]').trigger('click')
    await flushPromises()
    expect(disableUser).toHaveBeenCalledWith(2)
  })

  it('loads organizer workspace tabs and validates event/service actions', async () => {
    mocks.auth.session.value = { ...mocks.auth.session.value!, roleName: 'ORGANIZER' }
    vi.mocked(getOrganizerWorkspace).mockResolvedValue(workspace())
    vi.mocked(addOrganizerCatalogService).mockResolvedValue(workspace())
    vi.mocked(removeOrganizerCatalogService).mockResolvedValue(workspace())
    vi.mocked(updateOrganizerEvent).mockResolvedValue(workspace())
    vi.mocked(createOrganizerEvent).mockResolvedValue(workspace())
    vi.mocked(deleteOrganizerEvent).mockResolvedValue(workspace())

    const wrapper = mountView(OrganizerView)
    await flushPromises()

    expect(wrapper.text()).toContain('Bruto total')

    await wrapper.findAll('.pill-tab')[1].trigger('click')
    await wrapper.get('[data-test="add-catalog-service"]').trigger('click')
    expect(wrapper.text()).toContain('Selecciona un servicio')

    await wrapper.findAll('.pill-tab')[0].trigger('click')
    await wrapper.get('[data-test="edit-event"]').trigger('click')
    expect(wrapper.text()).toContain('Editar evento')
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(updateOrganizerEvent).toHaveBeenCalledWith(1, expect.objectContaining({
      trackId: 1,
      eventDate: '2026-07-12',
      services: [{ trackServiceId: 1, price: 30 }],
    }))

    await wrapper.findAll('.pill-tab')[2].trigger('click')
    expect(wrapper.text()).toContain('Stats 1')
  })

  it('loads the standard user profile and delegates booking/profile/lap actions', async () => {
    vi.mocked(getCurrentUserProfile).mockResolvedValue(user())
    vi.mocked(getCurrentUserEventBookings).mockResolvedValue([booking()])
    vi.mocked(getCurrentUserLapTimes).mockResolvedValue([lapTime()])
    vi.mocked(getTracks).mockResolvedValue([track()])
    vi.mocked(getTrackRanking).mockResolvedValue([trackRecord()])
    vi.mocked(getBookedServicesByBookingId).mockResolvedValue([bookedService()])
    vi.mocked(getEventServicesByEventId).mockResolvedValue([eventService()])
    vi.mocked(updateOwnEventBookingVisibility).mockResolvedValue(booking({ isVisible: false }))
    vi.mocked(updateCurrentUserProfile).mockResolvedValue(user())
    vi.mocked(createCurrentUserLapTime).mockResolvedValue(lapTime({ id: 6 }))
    vi.mocked(deleteCurrentUserLapTime).mockResolvedValue()

    const wrapper = mountView(UserProfileView)
    await flushPromises()

    expect(wrapper.text()).toContain('Juanje')
    await wrapper.get('[data-test="view-booking"]').trigger('click')
    await flushPromises()
    expect(getBookedServicesByBookingId).toHaveBeenCalledWith(9)

    await wrapper.get('[data-test="toggle-visibility"]').trigger('click')
    await flushPromises()
    expect(updateOwnEventBookingVisibility).toHaveBeenCalledWith(9, { isVisible: false })

    await wrapper.findAll('.pill-tab')[1].trigger('click')
    await wrapper.get('[data-test="edit-profile"]').trigger('click')
    await wrapper.get('[data-test="save-profile"]').trigger('click')
    await flushPromises()
    expect(updateCurrentUserProfile).toHaveBeenCalled()

    await wrapper.findAll('.pill-tab')[2].trigger('click')
    await wrapper.get('[data-test="add-lap"]').trigger('click')
    expect(wrapper.text()).toContain('Selecciona un circuito')
  })

  it('loads and saves the organizer profile variant', async () => {
    mocks.auth.session.value = { ...mocks.auth.session.value!, roleName: 'ORGANIZER' }
    vi.mocked(getCurrentOrganizerProfile).mockResolvedValue({
      ...organizer({ organizerEnabled: true }),
      created: '2026-01-01',
      roleName: 'ORGANIZER',
    })
    vi.mocked(updateCurrentOrganizerProfile).mockResolvedValue({
      ...organizer({ organizerEnabled: true }),
      created: '2026-01-01',
      roleName: 'ORGANIZER',
    })

    const wrapper = mountView(UserProfileView)
    await flushPromises()

    expect(wrapper.text()).toContain('Perfil organizador')
    await wrapper.get('[data-test="edit-organizer-profile"]').trigger('click')
    await wrapper.get('[data-test="save-organizer-profile"]').trigger('click')
    await flushPromises()

    expect(updateCurrentOrganizerProfile).toHaveBeenCalledWith(expect.objectContaining({
      legalName: 'TrackEvents S.L.',
      cif: 'B12345678',
    }))
  })

  it('loads a public profile and starts a message flow through auth or router', async () => {
    mocks.route.params = { displayName: 'Juanje' }
    vi.mocked(getPublicUserProfile).mockResolvedValue({
      id: 1,
      displayName: 'Juanje',
      completedEvents: 1,
      visitedCircuits: 1,
      topFiveLapTimes: 1,
      poleCount: 1,
    })
    vi.mocked(getEventBookingsByUserId).mockResolvedValue([booking()])
    vi.mocked(getLapTimesByUserId).mockResolvedValue([lapTime()])
    vi.mocked(getTrackRanking).mockResolvedValue([trackRecord()])

    mocks.auth.isAuthenticated.value = false
    const anonymousWrapper = mountView(PublicUserProfileView)
    await flushPromises()
    await anonymousWrapper.get('[data-test="email-action"]').trigger('click')
    expect(mocks.auth.openAuthDialog).toHaveBeenCalled()

    mocks.auth.isAuthenticated.value = true
    const loggedWrapper = mountView(PublicUserProfileView)
    await flushPromises()
    await loggedWrapper.get('[data-test="email-action"]').trigger('click')
    expect(mocks.router.push).toHaveBeenCalledWith({
      name: 'messages',
      query: { receiverId: '1' },
    })
  })

  it('loads messages, marks unread messages and validates compose/reply actions', async () => {
    vi.mocked(getOwnMessages).mockResolvedValue([
      message(),
      message({ id: 2, senderId: 1, receiverId: 2, isRead: true, sentAt: '2026-05-01T11:00:00' }),
    ])
    vi.mocked(getMessageContacts).mockResolvedValue([contact()])
    vi.mocked(markOwnMessageAsRead).mockResolvedValue(message({ isRead: true }))
    vi.mocked(createOwnMessage).mockResolvedValue(message({ id: 3, senderId: 1, receiverId: 2, isRead: true }))

    const wrapper = mountView(MessagesView)
    await flushPromises()

    expect(wrapper.text()).toContain('Bandeja de entrada')
    expect(markOwnMessageAsRead).toHaveBeenCalledWith(1)

    await wrapper.get('[data-test="compose-message"]').trigger('click')
    await wrapper.get('[data-test="submit-message"]').trigger('click')
    expect(wrapper.text()).toContain('Selecciona un destinatario')
    await wrapper.get('[data-test="fill-message"]').trigger('click')
    await wrapper.get('[data-test="submit-message"]').trigger('click')
    await flushPromises()
    expect(createOwnMessage).toHaveBeenCalledWith({
      receiverId: 2,
      subject: 'Jarama',
      message: 'Hola',
    })

    await wrapper.get('[data-test="open-thread"]').trigger('click')
    await wrapper.get('[data-test="reply-message"]').trigger('click')
    expect(wrapper.text()).toContain('Escribe un mensaje')
    await wrapper.get('[data-test="fill-reply"]').trigger('click')
    await wrapper.get('[data-test="reply-message"]').trigger('click')
    await flushPromises()
    expect(createOwnMessage).toHaveBeenLastCalledWith({
      receiverId: 2,
      subject: 'Jarama',
      message: 'Respuesta',
    })
  })

  it('loads event detail and confirms a booking from the summary flow', async () => {
    mocks.route.params = { id: '1' }
    vi.mocked(getEventById).mockResolvedValue(event())
    vi.mocked(getTrackById).mockResolvedValue(track())
    vi.mocked(getEventServicesByEventId).mockResolvedValue([eventService()])
    vi.mocked(getCurrentUserEventBookings).mockResolvedValue([])
    vi.mocked(getCurrentUserBookedServicesByEventId).mockResolvedValue([])
    vi.mocked(checkoutEventBooking).mockResolvedValue(booking())

    const wrapper = mountView(EventDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('Jarama')
    await wrapper.get('[data-test="toggle-event-service"]').trigger('click')
    await wrapper.get('[data-test="open-layout"]').trigger('click')
    expect(wrapper.text()).toContain('Trazado')
    await wrapper.get('[data-test="open-photo"]').trigger('click')

    await wrapper.get('[data-test="booking-action"]').trigger('click')
    await wrapper.get('[data-test="confirm-booking"]').trigger('click')
    await flushPromises()

    expect(checkoutEventBooking).toHaveBeenCalledWith({
      eventId: 1,
      eventServiceIds: [1],
      isVisible: false,
    })
  })

  it('loads an existing event booking and confirms cancellation when allowed', async () => {
    mocks.route.params = { id: '1' }
    vi.mocked(getEventById).mockResolvedValue(event())
    vi.mocked(getTrackById).mockResolvedValue(track())
    vi.mocked(getEventServicesByEventId).mockResolvedValue([eventService()])
    vi.mocked(getCurrentUserEventBookings).mockResolvedValue([booking()])
    vi.mocked(getCurrentUserBookedServicesByEventId).mockResolvedValue([bookedService()])
    vi.mocked(cancelEventBooking).mockResolvedValue()

    const wrapper = mountView(EventDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('Anular reserva')
    await wrapper.get('[data-test="booking-action"]').trigger('click')
    await wrapper.get('[data-test="confirm-booking"]').trigger('click')
    await flushPromises()

    expect(cancelEventBooking).toHaveBeenCalledWith(9)
  })
})
