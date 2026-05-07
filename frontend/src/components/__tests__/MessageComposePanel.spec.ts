import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MessageComposePanel from '@/components/messages/MessageComposePanel.vue'
import type { MessageContact } from '@/types/message'

const contacts: MessageContact[] = [
  {
    id: 2,
    displayName: 'trackevents',
    roleName: 'ORGANIZER',
  },
  {
    id: 3,
    displayName: 'piloto',
    roleName: null,
  },
]

function mountPanel(overrides = {}) {
  return mount(MessageComposePanel, {
    props: {
      contacts,
      receiverId: '',
      subject: '',
      message: '',
      sendError: '',
      sending: false,
      ...overrides,
    },
  })
}

describe('MessageComposePanel', () => {
  it('emits field updates, cancel and submit events', async () => {
    const wrapper = mountPanel()

    await wrapper.get('select').setValue('2')
    await wrapper.get('input').setValue('Reserva Jarama')
    await wrapper.get('textarea').setValue('Hola, quiero resolver una duda.')
    await wrapper.findAll('button')[0].trigger('click')
    await wrapper.get('form').trigger('submit')

    expect(wrapper.text()).toContain('trackevents')
    expect(wrapper.emitted('update:receiverId')).toEqual([['2']])
    expect(wrapper.emitted('update:subject')).toEqual([['Reserva Jarama']])
    expect(wrapper.emitted('update:message')).toEqual([['Hola, quiero resolver una duda.']])
    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })

  it('renders send error and disabled sending state', () => {
    const wrapper = mountPanel({
      sendError: 'No se pudo enviar',
      sending: true,
    })

    const submitButton = wrapper.findAll('button')[1]

    expect(wrapper.text()).toContain('No se pudo enviar')
    expect(submitButton.text()).toContain('Enviando...')
    expect(submitButton.attributes('disabled')).toBeDefined()
  })
})
