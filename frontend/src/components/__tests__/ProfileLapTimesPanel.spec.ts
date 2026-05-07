import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ProfileLapTimesPanel from '@/components/profile/ProfileLapTimesPanel.vue'
import type { LapTime } from '@/types/lapTime'
import type { LapTimeFormState } from '@/types/profile'
import type { Track } from '@/types/track'

const tracks: Track[] = [
  {
    id: 1,
    name: 'Circuito del Jarama',
    shortName: 'jarama',
    location: 'Madrid',
    description: 'Trazado madrileño',
  },
]

const lapTime: LapTime = {
  id: 7,
  userId: 1,
  userDisplayName: 'juanje',
  trackId: 1,
  trackName: 'Circuito del Jarama',
  lapDate: '2026-07-12',
  lapTimeMs: 102315,
  vehicle: 'BMW M2',
}

function form(): LapTimeFormState {
  return {
    trackId: '',
    lapDate: '',
    lapTimeText: '',
    vehicle: '',
  }
}

describe('ProfileLapTimesPanel', () => {
  it('renders the form and mutates the reactive form object', async () => {
    const lapForm = form()
    const wrapper = mount(ProfileLapTimesPanel, {
      props: {
        lapError: '',
        lapSaving: false,
        lapForm,
        tracks,
        sortedLapTimes: [],
      },
    })

    await wrapper.get('select').setValue('1')
    await wrapper.get('input[type="date"]').setValue('2026-07-12')
    await wrapper.get('input[placeholder="1:52.340"]').setValue('1:42.315')
    await wrapper.get('input[placeholder="BMW M2"]').setValue('BMW M2')

    expect(lapForm).toEqual({
      trackId: '1',
      lapDate: '2026-07-12',
      lapTimeText: '1:42.315',
      vehicle: 'BMW M2',
    })
  })

  it('emits submit and remove events', async () => {
    const wrapper = mount(ProfileLapTimesPanel, {
      props: {
        lapError: '',
        lapSaving: false,
        lapForm: form(),
        tracks,
        sortedLapTimes: [lapTime],
      },
    })

    expect(wrapper.text()).toContain('1:42.315')
    expect(wrapper.text()).toContain('Circuito del Jarama')

    await wrapper.get('form').trigger('submit')
    await wrapper.get('button[aria-label="Eliminar vuelta"]').trigger('click')

    expect(wrapper.emitted('submit')).toHaveLength(1)
    expect(wrapper.emitted('remove')).toEqual([[lapTime]])
  })

  it('renders error, saving and empty states', () => {
    const wrapper = mount(ProfileLapTimesPanel, {
      props: {
        lapError: 'No se pudo guardar',
        lapSaving: true,
        lapForm: form(),
        tracks,
        sortedLapTimes: [],
      },
    })

    expect(wrapper.text()).toContain('No se pudo guardar')
    expect(wrapper.text()).toContain('Guardando...')
    expect(wrapper.text()).toContain('Todavía no has registrado tiempos por vuelta.')
  })
})
