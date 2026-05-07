import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EventAttendeeList from '@/components/EventAttendeeList.vue'
import { getVisibleEventBookingsByEventId } from '@/services/eventBookingService'
import { getLapTimesByUserId } from '@/services/lapTimeService'
import { getTrackRanking } from '@/services/trackService'
import type { EventBooking } from '@/types/eventBooking'
import type { LapTime } from '@/types/lapTime'
import type { TrackRecord } from '@/types/trackRecord'

vi.mock('@/services/eventBookingService', () => ({
  getVisibleEventBookingsByEventId: vi.fn(),
}))

vi.mock('@/services/lapTimeService', () => ({
  getLapTimesByUserId: vi.fn(),
}))

vi.mock('@/services/trackService', () => ({
  getTrackRanking: vi.fn(),
}))

const booking: EventBooking = {
  id: 10,
  userId: 7,
  userDisplayName: 'juanje',
  eventId: 99,
  eventDate: '2026-07-12',
  organizerLegalName: 'TrackEvents S.L.',
  trackName: 'Circuito del Jarama',
  bookedAt: '2026-06-01T10:00:00',
  basePriceAtPurchase: 225,
  isVisible: true,
}

const lapTime: LapTime = {
  id: 3,
  userId: 7,
  userDisplayName: 'juanje',
  trackId: 1,
  trackName: 'Circuito del Jarama',
  lapDate: '2026-07-12',
  lapTimeMs: 102315,
  vehicle: 'BMW M2',
}

const ranking: TrackRecord[] = [
  {
    trackId: 1,
    trackName: 'Circuito del Jarama',
    userDisplayName: 'maria',
    lapDate: '2026-07-11',
    lapTimeMs: 101000,
    vehicle: 'Alpine A110',
  },
  {
    trackId: 1,
    trackName: 'Circuito del Jarama',
    userDisplayName: 'juanje',
    lapDate: '2026-07-12',
    lapTimeMs: 102315,
    vehicle: 'BMW M2',
  },
]

const getVisibleBookingsMock = vi.mocked(getVisibleEventBookingsByEventId)
const getLapTimesByUserIdMock = vi.mocked(getLapTimesByUserId)
const getTrackRankingMock = vi.mocked(getTrackRanking)

function mountList() {
  return mount(EventAttendeeList, {
    props: {
      eventId: 99,
      trackId: 1,
    },
    global: {
      stubs: {
        UserProfileLink: {
          props: ['displayName'],
          template: '<a><slot /></a>',
        },
      },
    },
  })
}

describe('EventAttendeeList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads visible attendees and shows ranking positions', async () => {
    getVisibleBookingsMock.mockResolvedValue([booking])
    getTrackRankingMock.mockResolvedValue(ranking)
    getLapTimesByUserIdMock.mockResolvedValue([lapTime])

    const wrapper = mountList()
    expect(wrapper.text()).toContain('Cargando asistentes visibles...')

    await flushPromises()

    expect(getVisibleBookingsMock).toHaveBeenCalledWith(99)
    expect(getTrackRankingMock).toHaveBeenCalledWith(1, 200)
    expect(getLapTimesByUserIdMock).toHaveBeenCalledWith(7)
    expect(wrapper.text()).toContain('juanje')
    expect(wrapper.text()).toContain('P2')
  })

  it('renders empty and error states', async () => {
    getVisibleBookingsMock.mockResolvedValueOnce([])
    const emptyWrapper = mountList()
    await flushPromises()
    expect(emptyWrapper.text()).toContain('Nadie ha mostrado todavía su asistencia en este evento.')

    getVisibleBookingsMock.mockRejectedValueOnce(new Error('network'))
    const errorWrapper = mountList()
    await flushPromises()
    expect(errorWrapper.text()).toContain('No se pudo cargar la lista de asistentes visibles.')
  })
})
