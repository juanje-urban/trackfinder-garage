import { mount, RouterLinkStub } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ProfileBookingsPanel from '@/components/profile/ProfileBookingsPanel.vue'
import type { EventBooking } from '@/types/eventBooking'

const activeBooking: EventBooking = {
  id: 1,
  userId: 1,
  userDisplayName: 'juanje',
  eventId: 12,
  eventDate: '2026-07-12',
  organizerLegalName: 'TrackEvents S.L.',
  trackName: 'Circuito del Jarama',
  bookedAt: '2026-06-01T10:00:00',
  basePriceAtPurchase: 125,
  isVisible: true,
}

const pastBooking: EventBooking = {
  ...activeBooking,
  id: 2,
  eventId: 13,
  eventDate: '2026-01-12',
  trackName: 'MotorLand Aragón',
  isVisible: false,
}

function mountPanel(overrides = {}) {
  return mount(ProfileBookingsPanel, {
    props: {
      bookingError: '',
      activeBookings: [activeBooking],
      pastBookings: [pastBooking],
      bookingCancellingId: null,
      bookingVisibilityUpdatingId: null,
      ...overrides,
    },
    global: {
      stubs: {
        RouterLink: RouterLinkStub,
      },
    },
  })
}

describe('ProfileBookingsPanel', () => {
  it('renders active and past bookings and emits actions', async () => {
    const wrapper = mountPanel()

    await wrapper.get('button[aria-label="Ocultar en perfil público"]').trigger('click')
    await wrapper.get('button[aria-label="Ver detalle de la reserva"]').trigger('click')
    await wrapper.get('button[aria-label="Mostrar en perfil público"]').trigger('click')

    expect(wrapper.text()).toContain('Reservas activas')
    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('MotorLand Aragón')
    expect(wrapper.findAllComponents(RouterLinkStub)[0].props('to')).toBe('/events/12')
    expect(wrapper.emitted('toggleVisibility')).toEqual([[activeBooking], [pastBooking]])
    expect(wrapper.emitted('viewBooking')).toEqual([[activeBooking]])
  })

  it('renders errors, empty states and busy visibility marker', () => {
    const wrapper = mountPanel({
      bookingError: 'No se pudieron cargar reservas',
      activeBookings: [],
      pastBookings: [pastBooking],
      bookingCancellingId: 99,
      bookingVisibilityUpdatingId: pastBooking.id,
    })

    expect(wrapper.text()).toContain('No se pudieron cargar reservas')
    expect(wrapper.text()).toContain('Todavía no tienes reservas futuras.')
    expect(wrapper.text()).toContain('...')
    expect(wrapper.get('button[aria-label="Mostrar en perfil público"]').attributes('disabled')).toBeDefined()
  })

  it('renders empty history and busy active booking actions', () => {
    const wrapper = mountPanel({
      activeBookings: [
        {
          ...activeBooking,
          isVisible: false,
        },
      ],
      pastBookings: [],
      bookingCancellingId: activeBooking.id,
      bookingVisibilityUpdatingId: activeBooking.id,
    })

    expect(wrapper.text()).toContain('Tu historial todavía no muestra asistencias pasadas.')
    expect(wrapper.text()).toContain('...')
    expect(wrapper.get('button[aria-label="Mostrar en perfil público"]').attributes('disabled')).toBeDefined()
    expect(wrapper.get('button[aria-label="Ver detalle de la reserva"]').attributes('disabled')).toBeDefined()
  })
})
