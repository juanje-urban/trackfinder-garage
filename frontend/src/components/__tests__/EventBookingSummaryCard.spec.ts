import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventBookingSummaryCard from '@/components/event-detail/EventBookingSummaryCard.vue'

describe('EventBookingSummaryCard', () => {
  it('renders the price breakdown and emits booking action', async () => {
    const wrapper = mount(EventBookingSummaryCard, {
      props: {
        isPastEvent: false,
        totalPriceLabel: '155 €',
        formattedPrice: '125 €',
        selectedServices: [
          {
            id: 1,
            name: 'Box privado',
            price: 30,
            source: 'track',
          },
        ],
        bookingButtonLabel: 'Reservar plaza',
        bookingCaption: 'Podrás confirmar antes de pagar.',
        isBookingActionDisabled: false,
      },
    })

    await wrapper.get('button').trigger('click')

    expect(wrapper.text()).toContain('155 €')
    expect(wrapper.text()).toContain('Entrada base')
    expect(wrapper.text()).toContain('Box privado')
    expect(wrapper.text()).toContain('Reservar plaza')
    expect(wrapper.emitted('bookingAction')).toHaveLength(1)
  })

  it('renders past and empty service states with disabled action', () => {
    const wrapper = mount(EventBookingSummaryCard, {
      props: {
        isPastEvent: true,
        totalPriceLabel: '125 €',
        formattedPrice: '125 €',
        selectedServices: [],
        bookingButtonLabel: 'Evento cerrado',
        bookingCaption: 'El evento ya terminó.',
        isBookingActionDisabled: true,
      },
    })

    expect(wrapper.text()).toContain('Evento finalizado')
    expect(wrapper.text()).toContain('No has seleccionado servicios adicionales.')
    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
  })
})
