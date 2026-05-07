import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AppModal from '@/components/AppModal.vue'

describe('AppModal', () => {
  it('does not render the dialog while it is closed', () => {
    const wrapper = mount(AppModal, {
      props: {
        isOpen: false,
        ariaLabel: 'Crear reserva',
      },
      slots: {
        default: '<p>Contenido del modal</p>',
      },
    })

    expect(wrapper.find('dialog.app-modal').exists()).toBe(false)
  })

  it('renders header, content and emits close from backdrop and close button', async () => {
    const wrapper = mount(AppModal, {
      props: {
        isOpen: true,
        ariaLabel: 'Crear reserva',
        eyebrow: 'Reserva',
        title: 'Confirma tu plaza',
        closeLabel: 'Cerrar modal',
        width: '480px',
        light: true,
      },
      slots: {
        default: '<p>Contenido del modal</p>',
      },
    })

    expect(wrapper.get('dialog.app-modal').attributes('aria-label')).toBe('Crear reserva')
    expect(wrapper.text()).toContain('Reserva')
    expect(wrapper.text()).toContain('Confirma tu plaza')
    expect(wrapper.text()).toContain('Contenido del modal')
    expect(wrapper.get('.app-modal__panel').classes()).toContain('app-modal__panel--light')

    await wrapper.get('dialog.app-modal').trigger('click')
    await wrapper.get('button[aria-label="Cerrar modal"]').trigger('click')

    expect(wrapper.emitted('close')).toHaveLength(2)
  })
})
