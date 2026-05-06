import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import RankingPositionBadge from '@/components/RankingPositionBadge.vue'

describe('RankingPositionBadge', () => {
  it('prints the ranking position with P prefix', () => {
    const wrapper = mount(RankingPositionBadge, {
      props: { position: 1 },
    })

    expect(wrapper.text()).toBe('P1')
    expect(wrapper.classes()).toContain('ranking-position-badge--1')
  })

  it('keeps generic positions renderable', () => {
    const wrapper = mount(RankingPositionBadge, {
      props: { position: 12 },
    })

    expect(wrapper.text()).toBe('P12')
    expect(wrapper.classes()).toContain('ranking-position-badge--12')
  })
})
