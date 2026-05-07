import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import FutureEventsView from '@/views/FutureEventsView.vue'
import HomeView from '@/views/HomeView.vue'
import TracksView from '@/views/TracksView.vue'
import { getFutureEvents } from '@/services/eventService'
import { getTracks } from '@/services/trackService'
import type { Event } from '@/types/event'
import type { Track } from '@/types/track'

const routerMocks = vi.hoisted(() => ({
  route: {
    query: {} as Record<string, unknown>,
  },
}))

vi.mock('vue-router', () => ({
  RouterLink: {
    props: ['to'],
    template: '<a><slot /></a>',
  },
  useRoute: () => routerMocks.route,
}))

vi.mock('@/services/eventService', () => ({
  getFutureEvents: vi.fn(),
}))

vi.mock('@/services/trackService', () => ({
  getTracks: vi.fn(),
}))

vi.mock('@/assets/home/hero_page_jarama.jpg', () => ({
  default: '/hero.jpg',
}))

const getFutureEventsMock = vi.mocked(getFutureEvents)
const getTracksMock = vi.mocked(getTracks)

const stubs = {
  PageHero: {
    props: ['title', 'description'],
    template: '<section><h1>{{ title }}</h1><p>{{ description }}</p><slot name="aside" /></section>',
  },
  HeroInfoPanel: {
    props: ['label', 'caption'],
    template: '<article><p>{{ label }}</p><p>{{ caption }}</p><slot /></article>',
  },
  MetricCard: {
    props: ['label', 'value'],
    template: '<article>{{ label }}: {{ value }}</article>',
  },
  ContentSection: {
    props: ['title', 'loading', 'loadingMessage', 'error', 'empty', 'emptyMessage'],
    template: `
      <section>
        <h2>{{ title }}</h2>
        <slot name="action" />
        <p v-if="loading">{{ loadingMessage }}</p>
        <p v-if="error">{{ error }}</p>
        <p v-if="empty">{{ emptyMessage }}</p>
        <slot />
      </section>
    `,
  },
  CatalogPage: {
    props: ['loading', 'loadingMessage', 'error', 'empty', 'emptyMessage'],
    template: `
      <section>
        <slot name="metrics" />
        <slot name="summary" />
        <slot name="toolbar" />
        <p v-if="loading">{{ loadingMessage }}</p>
        <p v-if="error">{{ error }}</p>
        <p v-if="empty">{{ emptyMessage }}</p>
        <slot />
      </section>
    `,
  },
  EventCard: {
    props: ['event'],
    template: '<article>{{ event.trackName }} - {{ event.basePrice }}</article>',
  },
  TrackCard: {
    props: ['track'],
    template: '<article>{{ track.name }} - {{ track.location }}</article>',
  },
  TrackRecordBoard: {
    props: ['trackName'],
    template: '<article>Records {{ trackName }}</article>',
  },
  EventSearchToolbar: {
    props: ['resultCount', 'totalCount'],
    emits: ['reset'],
    template: '<button data-test="reset-filters" type="button" @click="$emit(\'reset\')">{{ resultCount }}/{{ totalCount }}</button>',
  },
}

function event(overrides: Partial<Event> = {}): Event {
  return {
    id: 1,
    organizerId: 1,
    organizerLegalName: 'TrackEvents S.L.',
    trackId: 1,
    trackName: 'Jarama',
    trackShortName: 'jarama',
    eventDate: '2026-07-12',
    basePrice: 120,
    maxParticipants: 20,
    remainingCapacity: 12,
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

describe('catalog views', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    routerMocks.route.query = {}
  })

  it('loads home events and tracks, then derives metrics and unique record boards', async () => {
    getFutureEventsMock.mockResolvedValue([
      event({ id: 2, trackId: 2, trackName: 'Calafat', eventDate: '2026-08-12', remainingCapacity: 3 }),
      event({ id: 1, trackId: 1, trackName: 'Jarama', eventDate: '2026-07-12', remainingCapacity: 7 }),
      event({ id: 3, trackId: 1, trackName: 'Jarama', eventDate: '2026-09-12', remainingCapacity: 2 }),
    ])
    getTracksMock.mockResolvedValue([
      track({ id: 1, location: 'Madrid' }),
      track({ id: 2, name: 'Circuit de Calafat', location: 'Tarragona' }),
    ])

    const wrapper = mount(HomeView, {
      global: { stubs },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('Jarama a Fondo')
    expect(wrapper.text()).toContain('Sesiones programadas: 3')
    expect(wrapper.text()).toContain('Plazas disponibles: 12')
    expect(wrapper.text()).toContain('Localizaciones: 2')
    expect(wrapper.text()).toContain('Records Jarama')
    expect(wrapper.text()).toContain('Records Calafat')
  })

  it('renders track catalog success and error states', async () => {
    getTracksMock.mockResolvedValue([track()])

    const successWrapper = mount(TracksView, {
      global: { stubs },
    })
    await flushPromises()

    expect(successWrapper.text()).toContain('Circuito del Jarama')
    expect(successWrapper.text()).toContain('Circuitos: 1')

    getTracksMock.mockRejectedValue(new Error('network'))

    const errorWrapper = mount(TracksView, {
      global: { stubs },
    })
    await flushPromises()

    expect(errorWrapper.text()).toContain('No se pudieron cargar los circuitos.')
  })

  it('loads future events, applies route filters and can reset them', async () => {
    routerMocks.route.query = { trackId: '2' }
    getFutureEventsMock.mockResolvedValue([
      event({ id: 1, trackId: 1, trackName: 'Jarama', eventDate: '2026-07-12', basePrice: 120 }),
      event({ id: 2, trackId: 2, trackName: 'Calafat', eventDate: '2026-08-12', basePrice: 150 }),
    ])

    const wrapper = mount(FutureEventsView, {
      global: { stubs },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('Calafat')
    expect(wrapper.text()).not.toContain('Jarama - 120')
    expect(wrapper.get('[data-test="reset-filters"]').text()).toContain('1/2')

    await wrapper.get('[data-test="reset-filters"]').trigger('click')

    expect(wrapper.text()).toContain('Jarama - 120')
    expect(wrapper.text()).toContain('Calafat - 150')
  })
})
