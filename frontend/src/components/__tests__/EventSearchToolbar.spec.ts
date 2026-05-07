import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventSearchToolbar from '@/components/EventSearchToolbar.vue'

function mountToolbar(overrides = {}) {
  return mount(EventSearchToolbar, {
    props: {
      selectedStartDate: '',
      selectedEndDate: '',
      selectedTrackId: '',
      maxBasePrice: '',
      trackOptions: [
        { id: 1, name: 'Circuito del Jarama' },
        { id: 2, name: 'MotorLand Aragón' },
      ],
      resultCount: 2,
      totalCount: 5,
      ...overrides,
    },
  })
}

describe('EventSearchToolbar', () => {
  it('emits updates for every controlled filter and reset', async () => {
    const wrapper = mountToolbar({
      selectedStartDate: '2026-07-01',
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('2026-07-10')
    await inputs[1].setValue('2026-07-20')
    await wrapper.get('select').setValue('2')
    await inputs[2].setValue('200')
    await wrapper.get('.search-toolbar__reset').trigger('click')

    expect(wrapper.emitted('update:selectedStartDate')).toEqual([['2026-07-10']])
    expect(wrapper.emitted('update:selectedEndDate')).toEqual([['2026-07-20']])
    expect(wrapper.emitted('update:selectedTrackId')).toEqual([['2']])
    expect(wrapper.emitted('update:maxBasePrice')).toEqual([['200']])
    expect(wrapper.emitted('reset')).toHaveLength(1)
  })

  it('disables reset when there are no active filters and shows result count', () => {
    const wrapper = mountToolbar()

    expect(wrapper.text()).toContain('Mostrando 2 de 5 eventos.')
    expect(wrapper.get('.search-toolbar__reset').attributes('disabled')).toBeDefined()
  })
})
