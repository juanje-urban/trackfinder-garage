import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import TrackRecordBoard from '@/components/TrackRecordBoard.vue'
import { getTrackRanking } from '@/services/trackService'
import type { TrackRecord } from '@/types/trackRecord'

vi.mock('@/services/trackService', () => ({
  getTrackRanking: vi.fn(),
}))

const ranking: TrackRecord[] = [
  {
    trackId: 1,
    trackName: 'Circuito del Jarama',
    userDisplayName: 'juanje',
    lapDate: '2026-07-12',
    lapTimeMs: 102315,
    vehicle: 'BMW M2',
  },
  {
    trackId: 1,
    trackName: 'Circuito del Jarama',
    userDisplayName: 'maria',
    lapDate: '2026-07-13',
    lapTimeMs: 104000,
    vehicle: 'Alpine A110',
  },
]

const getTrackRankingMock = vi.mocked(getTrackRanking)

function mountBoard() {
  return mount(TrackRecordBoard, {
    props: {
      trackId: 1,
      trackName: 'Circuito del Jarama',
      limit: 2,
      eyebrow: 'Ranking',
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

function mountBoardWithDefaults() {
  return mount(TrackRecordBoard, {
    props: {
      trackId: 1,
      trackName: 'Circuito del Jarama',
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

describe('TrackRecordBoard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads and renders ranking rows', async () => {
    getTrackRankingMock.mockResolvedValue(ranking)

    const wrapper = mountBoard()
    expect(wrapper.text()).toContain('Cargando records de vuelta...')

    await flushPromises()

    expect(getTrackRankingMock).toHaveBeenCalledWith(1, 2)
    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('1:42.315')
    expect(wrapper.text()).toContain('juanje')
    expect(wrapper.text()).toContain('P1')
    expect(wrapper.text()).toContain('P2')
  })

  it('renders empty and error states', async () => {
    getTrackRankingMock.mockResolvedValueOnce([])
    const emptyWrapper = mountBoard()
    await flushPromises()
    expect(emptyWrapper.text()).toContain('Todavía no hay tiempos publicados para este circuito.')

    getTrackRankingMock.mockRejectedValueOnce(new Error('network'))
    const errorWrapper = mountBoard()
    await flushPromises()
    expect(errorWrapper.text()).toContain('No se pudieron cargar los records de vuelta.')
  })

  it('reloads ranking when track or limit changes', async () => {
    getTrackRankingMock.mockResolvedValue(ranking)
    const wrapper = mountBoard()
    await flushPromises()

    await wrapper.setProps({ trackId: 2, limit: 5 })
    await flushPromises()

    expect(getTrackRankingMock).toHaveBeenCalledWith(1, 2)
    expect(getTrackRankingMock).toHaveBeenCalledWith(2, 5)
  })

  it('uses default limit and hides the eyebrow when it is not provided', async () => {
    getTrackRankingMock.mockResolvedValue(ranking)

    const wrapper = mountBoardWithDefaults()
    await flushPromises()

    expect(getTrackRankingMock).toHaveBeenCalledWith(1, 3)
    expect(wrapper.find('.ui-eyebrow').exists()).toBe(false)
  })
})
