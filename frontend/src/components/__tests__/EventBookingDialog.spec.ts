import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventBookingDialog from '@/components/EventBookingDialog.vue'

const checkoutProps = {
  isOpen: true,
  mode: 'checkout' as const,
  trackName: 'Circuito del Jarama',
  eventDate: '12 de julio de 2026',
  canConfirm: true,
  basePriceLabel: '125 €',
  totalPriceLabel: '155 €',
  selectedServices: [
    {
      id: 1,
      name: 'Box privado',
      priceLabel: '30 €',
    },
  ],
  isSubmitting: false,
  errorMessage: '',
  isVisibleOnPublicProfile: false,
}

describe('EventBookingDialog', () => {
  it('does not render while closed', () => {
    const wrapper = mount(EventBookingDialog, {
      props: {
        ...checkoutProps,
        isOpen: false,
      },
    })

    expect(wrapper.find('dialog.booking-dialog').exists()).toBe(false)
  })

  it('renders checkout details and emits close, visibility and confirm events', async () => {
    const wrapper = mount(EventBookingDialog, {
      props: checkoutProps,
    })

    await wrapper.get('.booking-dialog__visibility-checkbox').setValue(true)
    await wrapper.get('dialog.booking-dialog').trigger('click')
    await wrapper.get('.booking-dialog__close').trigger('click')
    await wrapper.findAll('.booking-dialog__actions .action-button')[0].trigger('click')
    await wrapper.findAll('.booking-dialog__actions .action-button')[1].trigger('click')

    expect(wrapper.get('dialog.booking-dialog').attributes('aria-label')).toContain('Confirmar reserva')
    expect(wrapper.text()).toContain('Entrada base')
    expect(wrapper.text()).toContain('Box privado')
    expect(wrapper.text()).toContain('155 €')
    expect(wrapper.emitted('update:isVisibleOnPublicProfile')).toEqual([[true]])
    expect(wrapper.emitted('close')).toHaveLength(3)
    expect(wrapper.emitted('confirm')).toHaveLength(1)
  })

  it('renders cancellation mode and disables confirmation when cancellation is not allowed', () => {
    const wrapper = mount(EventBookingDialog, {
      props: {
        ...checkoutProps,
        mode: 'cancel',
        canConfirm: false,
        selectedServices: [],
        errorMessage: 'No se pudo anular',
      },
    })

    const confirmButton = wrapper.findAll('.booking-dialog__actions .action-button')[1]

    expect(wrapper.text()).toContain('Anular reserva')
    expect(wrapper.text()).toContain('Esta reserva no tiene servicios adicionales contratados.')
    expect(wrapper.text()).toContain('No se pudo anular')
    expect(confirmButton.attributes('disabled')).toBeDefined()
  })

  it('renders cancellable service lines and cancelling label', () => {
    const wrapper = mount(EventBookingDialog, {
      props: {
        ...checkoutProps,
        mode: 'cancel',
        canConfirm: true,
        isSubmitting: true,
      },
    })

    const confirmButton = wrapper.findAll('.booking-dialog__actions .action-button')[1]

    expect(wrapper.text()).toContain('Box privado')
    expect(wrapper.text()).toContain('Podrás anular la reserva')
    expect(confirmButton.text()).toContain('Anulando...')
    expect(confirmButton.attributes('disabled')).toBeDefined()
  })

  it('renders checkout without services and submitting label', () => {
    const wrapper = mount(EventBookingDialog, {
      props: {
        ...checkoutProps,
        selectedServices: [],
        isSubmitting: true,
      },
    })

    const confirmButton = wrapper.findAll('.booking-dialog__actions .action-button')[1]

    expect(wrapper.text()).toContain('No has seleccionado servicios adicionales')
    expect(confirmButton.text()).toContain('Confirmando...')
    expect(confirmButton.attributes('disabled')).toBeDefined()
  })
})
