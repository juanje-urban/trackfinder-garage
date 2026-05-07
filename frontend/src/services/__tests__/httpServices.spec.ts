import { describe, expect, it, vi, beforeEach } from 'vitest'
import { api } from '@/services/api'
import { login, register, registerOrganizer, getCurrentSession } from '@/services/authService'
import {
  cancelEventBooking,
  checkoutEventBooking,
  getBookedServicesByBookingId,
  getCurrentUserBookedServicesByEventId,
  getCurrentUserEventBookings,
  getEventBookingsByUserId,
  getVisibleEventBookingsByEventId,
  updateOwnEventBookingVisibility,
} from '@/services/eventBookingService'
import { getEventById, getEventServicesByEventId, getFutureEvents } from '@/services/eventService'
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
  enableServiceCatalogItem,
  getServices,
  updateServiceCatalogItem,
} from '@/services/serviceCatalogService'
import {
  createTrack,
  getTrackById,
  getTrackRanking,
  getTracks,
  updateTrack,
} from '@/services/trackService'
import {
  createTrackServiceAssignment,
  deleteTrackServiceAssignment,
  getTrackServiceAssignments,
} from '@/services/trackServiceAssignmentService'
import {
  disableUser,
  enableUser,
  getCurrentUserProfile,
  getPublicUserProfile,
  getUsers,
  updateCurrentUserProfile,
} from '@/services/userService'

vi.mock('@/services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

const mockedApi = vi.mocked(api)

function response<T>(data: T) {
  return { data }
}

describe('HTTP services', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('maps auth endpoints and enriches sessions with a Basic header', async () => {
    const identity = {
      userId: 1,
      displayName: 'Juanje',
      email: 'juanje@example.com',
      roleName: 'USER',
    }
    const credentials = { email: 'juanje@example.com', password: 'secreto' }

    mockedApi.post.mockResolvedValue(response(identity))
    mockedApi.get.mockResolvedValue(response(identity))

    await expect(login(credentials)).resolves.toMatchObject({
      ...identity,
      authorizationHeader: `Basic ${globalThis.window.btoa('juanje@example.com:secreto')}`,
    })
    expect(mockedApi.post).toHaveBeenCalledWith('/auth/login', credentials)

    await expect(register({ ...credentials, displayName: 'juanje', name: 'Juan', surname: 'Urban', address: 'Calle Motor', phone: '600000000' }))
      .resolves.toMatchObject(identity)
    expect(mockedApi.post).toHaveBeenLastCalledWith('/auth/register', expect.any(Object))

    await expect(registerOrganizer({
      ...credentials,
      displayName: 'organizer',
      name: 'Track',
      surname: 'Events',
      address: 'Calle Motor',
      phone: '600000001',
      legalName: 'Track Events S.L.',
      cif: 'B12345678',
    })).resolves.toMatchObject(identity)
    expect(mockedApi.post).toHaveBeenLastCalledWith('/auth/register/organizer', expect.any(Object))

    await expect(getCurrentSession()).resolves.toEqual(identity)
    expect(mockedApi.get).toHaveBeenCalledWith('/auth/me')
  })

  it('maps event and booking endpoints', async () => {
    mockedApi.get.mockResolvedValue(response(['ok']))
    mockedApi.post.mockResolvedValue(response({ id: 9 }))
    mockedApi.patch.mockResolvedValue(response({ id: 9, isVisible: false }))
    mockedApi.delete.mockResolvedValue(response(undefined))

    await expect(getFutureEvents()).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/events/future')

    await expect(getEventById(7)).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/events/7')

    await expect(getEventServicesByEventId(7)).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/event-services/event/7')

    await expect(checkoutEventBooking({ eventId: 7, eventServiceIds: [1, 2], isVisible: true }))
      .resolves.toEqual({ id: 9 })
    expect(mockedApi.post).toHaveBeenLastCalledWith('/event-bookings/checkout', {
      eventId: 7,
      eventServiceIds: [1, 2],
      isVisible: true,
    })

    await expect(getEventBookingsByUserId(3)).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/event-bookings/user/3')

    await expect(getVisibleEventBookingsByEventId(7)).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/event-bookings/event/7/visible')

    await expect(getCurrentUserEventBookings()).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/event-bookings/me')

    await expect(getBookedServicesByBookingId(9)).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/event-booking-services/booking/9')

    await expect(getCurrentUserBookedServicesByEventId(7)).resolves.toEqual(['ok'])
    expect(mockedApi.get).toHaveBeenLastCalledWith('/event-booking-services/event/7/me')

    await expect(cancelEventBooking(9)).resolves.toBeUndefined()
    expect(mockedApi.delete).toHaveBeenLastCalledWith('/event-bookings/9')

    await expect(updateOwnEventBookingVisibility(9, { isVisible: false }))
      .resolves.toEqual({ id: 9, isVisible: false })
    expect(mockedApi.patch).toHaveBeenLastCalledWith('/event-bookings/9/visibility', { isVisible: false })
  })

  it('maps track, lap time and messaging endpoints', async () => {
    mockedApi.get.mockResolvedValue(response(['ok']))
    mockedApi.post.mockResolvedValue(response({ id: 1 }))
    mockedApi.put.mockResolvedValue(response({ id: 1 }))
    mockedApi.patch.mockResolvedValue(response({ id: 2, isRead: true }))
    mockedApi.delete.mockResolvedValue(response(undefined))

    await getTracks()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/tracks')

    await getTrackById(1)
    expect(mockedApi.get).toHaveBeenLastCalledWith('/tracks/1')

    await createTrack({ name: 'Jarama', shortName: 'jarama', location: 'Madrid', description: 'Circuito' })
    expect(mockedApi.post).toHaveBeenLastCalledWith('/tracks', expect.any(Object))

    await updateTrack(1, { name: 'Jarama', shortName: 'jarama', location: 'Madrid', description: 'Circuito' })
    expect(mockedApi.put).toHaveBeenLastCalledWith('/tracks/1', expect.any(Object))

    await getTrackRanking(1, 5)
    expect(mockedApi.get).toHaveBeenLastCalledWith('/tracks/1/ranking', { params: { limit: 5 } })

    await getLapTimesByUserId(3)
    expect(mockedApi.get).toHaveBeenLastCalledWith('/lap-times/user/3')

    await getCurrentUserLapTimes()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/lap-times/me')

    await createCurrentUserLapTime({ trackId: 1, lapDate: '2026-07-12', lapTimeMs: 102300 })
    expect(mockedApi.post).toHaveBeenLastCalledWith('/lap-times/me', expect.any(Object))

    await deleteCurrentUserLapTime(4)
    expect(mockedApi.delete).toHaveBeenLastCalledWith('/lap-times/me/4')

    await getOwnMessages()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/messages')

    await getMessageContacts()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/messages/contacts')

    await createOwnMessage({ receiverId: 2, subject: 'Jarama', message: 'Hola' })
    expect(mockedApi.post).toHaveBeenLastCalledWith('/messages', expect.any(Object))

    await markOwnMessageAsRead(2)
    expect(mockedApi.patch).toHaveBeenLastCalledWith('/messages/2/read')
  })

  it('maps organizer, catalog and admin endpoints', async () => {
    mockedApi.get.mockResolvedValue(response(['ok']))
    mockedApi.post.mockResolvedValue(response({ id: 1 }))
    mockedApi.put.mockResolvedValue(response({ id: 1 }))
    mockedApi.patch.mockResolvedValue(response({ id: 1, enabled: true }))
    mockedApi.delete.mockResolvedValue(response({ id: 1 }))

    await getOrganizers()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/organizers')

    await getCurrentOrganizerProfile()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/organizers/me')

    await updateCurrentOrganizerProfile({
      name: 'Track',
      surname: 'Events',
      email: 'organizer@example.com',
      address: 'Calle Motor',
      phone: '600000001',
      legalName: 'Track Events S.L.',
      cif: 'B12345678',
    })
    expect(mockedApi.put).toHaveBeenLastCalledWith('/organizers/me', expect.any(Object))

    await enableOrganizer(3)
    expect(mockedApi.patch).toHaveBeenLastCalledWith('/organizers/3/enable')

    await deleteOrganizer(3)
    expect(mockedApi.delete).toHaveBeenLastCalledWith('/organizers/3')

    await getOrganizerWorkspace()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/organizer-workspace')

    await addOrganizerCatalogService(5)
    expect(mockedApi.post).toHaveBeenLastCalledWith('/organizer-workspace/services', { serviceId: 5 })

    await removeOrganizerCatalogService(6)
    expect(mockedApi.delete).toHaveBeenLastCalledWith('/organizer-workspace/services/6')

    const eventPayload = {
      trackId: 1,
      eventDate: '2026-07-12',
      basePrice: 120,
      maxParticipants: 20,
      description: 'Tandas libres',
      services: [],
    }
    await createOrganizerEvent(eventPayload)
    expect(mockedApi.post).toHaveBeenLastCalledWith('/organizer-workspace/events', eventPayload)

    await updateOrganizerEvent(7, eventPayload)
    expect(mockedApi.put).toHaveBeenLastCalledWith('/organizer-workspace/events/7', eventPayload)

    await deleteOrganizerEvent(7)
    expect(mockedApi.delete).toHaveBeenLastCalledWith('/organizer-workspace/events/7')

    await getServices()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/services')

    await createServiceCatalogItem({
      name: 'Box',
      description: 'Box privado',
      allowedForTrack: true,
      allowedForOrganizer: false,
    })
    expect(mockedApi.post).toHaveBeenLastCalledWith('/services', expect.any(Object))

    await updateServiceCatalogItem(4, {
      name: 'Box',
      description: 'Box privado',
      allowedForTrack: true,
      allowedForOrganizer: false,
    })
    expect(mockedApi.put).toHaveBeenLastCalledWith('/services/4', expect.any(Object))

    await enableServiceCatalogItem(4)
    expect(mockedApi.patch).toHaveBeenLastCalledWith('/services/4/enable')

    await disableServiceCatalogItem(4)
    expect(mockedApi.patch).toHaveBeenLastCalledWith('/services/4/disable')

    await getTrackServiceAssignments()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/track-services')

    await createTrackServiceAssignment({ trackId: 1, serviceId: 4 })
    expect(mockedApi.post).toHaveBeenLastCalledWith('/track-services', { trackId: 1, serviceId: 4 })

    await deleteTrackServiceAssignment(8)
    expect(mockedApi.delete).toHaveBeenLastCalledWith('/track-services/8')
  })

  it('maps user endpoints', async () => {
    mockedApi.get.mockResolvedValue(response(['ok']))
    mockedApi.put.mockResolvedValue(response({ id: 1 }))
    mockedApi.patch.mockResolvedValue(response({ id: 1, enabled: true }))

    await getCurrentUserProfile()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/users/me')

    await getPublicUserProfile('Juanje Urban')
    expect(mockedApi.get).toHaveBeenLastCalledWith('/users/public/Juanje%20Urban')

    await updateCurrentUserProfile({
      name: 'Juan',
      surname: 'Urban',
      email: 'juanje@example.com',
      address: 'Calle Motor',
      phone: '600000000',
    })
    expect(mockedApi.put).toHaveBeenLastCalledWith('/users/me', expect.any(Object))

    await getUsers()
    expect(mockedApi.get).toHaveBeenLastCalledWith('/users')

    await enableUser(3)
    expect(mockedApi.patch).toHaveBeenLastCalledWith('/users/3/enable')

    await disableUser(3)
    expect(mockedApi.patch).toHaveBeenLastCalledWith('/users/3/disable')
  })
})
