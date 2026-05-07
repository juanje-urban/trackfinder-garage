import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import OrganizerStatsPanel from '@/components/organizer/OrganizerStatsPanel.vue'
import type { OrganizerWorkspaceEventStats, OrganizerWorkspaceStats } from '@/types/organizerWorkspace'

const eventStat: OrganizerWorkspaceEventStats & { width: string } = {
  eventId: 1,
  trackName: 'Circuito del Jarama',
  eventDate: '2026-07-12',
  bookings: 12,
  soldServices: 5,
  remainingCapacity: 8,
  totalCapacity: 20,
  baseRevenue: 1500,
  serviceRevenue: 250,
  grossRevenue: 1750,
  width: '75%',
}

function stats(overrides: Partial<OrganizerWorkspaceStats> = {}): OrganizerWorkspaceStats {
  return {
    totalBaseRevenue: 1500,
    totalServiceRevenue: 250,
    totalGrossRevenue: 1750,
    totalBookings: 12,
    totalSoldServices: 5,
    futureEvents: 1,
    pastEvents: 0,
    totalCapacity: 20,
    totalRemainingCapacity: 8,
    eventStats: [eventStat],
    ...overrides,
  }
}

describe('OrganizerStatsPanel', () => {
  it('renders aggregate stats and revenue chart rows', () => {
    const wrapper = mount(OrganizerStatsPanel, {
      props: {
        stats: stats(),
        revenueChartBars: [eventStat],
      },
    })

    expect(wrapper.text()).toContain('Beneficio bruto total')
    expect(wrapper.text()).toContain('Circuito del Jarama')
    expect(wrapper.text()).toContain('12 asistentes')
    expect(wrapper.text()).toContain('5 servicios')
    expect(wrapper.get('.organizer-chart__fill').attributes('style')).toContain('width: 75%;')
  })

  it('renders empty analysis state without event stats', () => {
    const wrapper = mount(OrganizerStatsPanel, {
      props: {
        stats: stats({
          eventStats: [],
          totalGrossRevenue: 0,
          totalBaseRevenue: 0,
          totalServiceRevenue: 0,
          totalRemainingCapacity: 0,
          totalCapacity: 0,
        }),
        revenueChartBars: [],
      },
    })

    expect(wrapper.text()).toContain('Todavía no hay eventos que analizar.')
    expect(wrapper.find('.organizer-chart').exists()).toBe(false)
  })
})
