import { mount, RouterLinkStub } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ProfileBookingDetailDialog from '@/components/profile/ProfileBookingDetailDialog.vue'
import type { EventBooking } from '@/types/eventBooking'

const booking: EventBooking = {
  id: 4,
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

function mountDialog(overrides = {}) {
  return mount(ProfileBookingDetailDialog, {
    props: {
      isOpen: true,
      booking,
      services: [
        {
          id: 1,
          name: 'Box privado',
          price: 30,
        },
      ],
      loading: false,
      loadingError: '',
      cancellationError: '',
      isCancelling: false,
      canCancel: true,
      ...overrides,
    },
    global: {
      stubs: {
        RouterLink: RouterLinkStub,
      },
    },
  })
}

describe('ProfileBookingDetailDialog', () => {
  it('renders booking details, total price and emits cancellation', async () => {
    const wrapper = mountDialog()

    await wrapper.get('.profile-booking-detail__cancel').trigger('click')

    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('TrackEvents S.L.')
    expect(wrapper.text()).toContain('Box privado')
    expect(wrapper.text()).toContain('155')
    expect(wrapper.getComponent(RouterLinkStub).props('to')).toBe('/events/12')
    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('renders loading, error and non-cancellable states', () => {
    const wrapper = mountDialog({
      services: [],
      loading: false,
      loadingError: 'No se pudieron cargar los servicios',
      cancellationError: 'No se pudo anular',
      canCancel: false,
      booking: {
        ...booking,
        isVisible: false,
      },
    })

    expect(wrapper.text()).toContain('Oculta en perfil público')
    expect(wrapper.text()).toContain('No se pudieron cargar los servicios')
    expect(wrapper.text()).toContain('No se pudo anular')
    expect(wrapper.text()).toContain('La anulacion se cierra 14 dias antes del evento.')
    expect(wrapper.find('.profile-booking-detail__cancel').exists()).toBe(false)
  })

  it('renders loading services and cancelling label', () => {
    const wrapper = mountDialog({
      services: [],
      loading: true,
      isCancelling: true,
    })

    expect(wrapper.text()).toContain('Cargando servicios contratados...')
    expect(wrapper.get('.profile-booking-detail__cancel').text()).toContain('Anulando...')
    expect(wrapper.get('.profile-booking-detail__cancel').attributes('disabled')).toBeDefined()
  })

  it('renders reservation without extra services', () => {
    const wrapper = mountDialog({
      services: [],
    })

    expect(wrapper.text()).toContain('Esta reserva no tiene servicios adicionales contratados.')
    expect(wrapper.text()).toContain('125')
  })
})
