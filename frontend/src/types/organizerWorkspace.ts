import type { Event } from '@/types/event'
import type { EventServiceItem } from '@/types/eventService'
import type { Organizer } from '@/types/organizer'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'
import type { Track } from '@/types/track'
import type { TrackServiceAssignment } from '@/types/trackService'

export interface OrganizerCatalogService {
  id: number
  organizerId: number
  organizerLegalName: string
  serviceId: number
  serviceName: string
}

export interface OrganizerWorkspaceEventStats {
  eventId: number
  trackName: string
  eventDate: string
  bookings: number
  soldServices: number
  remainingCapacity: number
  totalCapacity: number
  baseRevenue: number
  serviceRevenue: number
  grossRevenue: number
}

export interface OrganizerManagedEvent {
  event: Event
  services: EventServiceItem[]
  stats: OrganizerWorkspaceEventStats
}

export interface OrganizerWorkspaceStats {
  totalBaseRevenue: number
  totalServiceRevenue: number
  totalGrossRevenue: number
  totalBookings: number
  totalSoldServices: number
  futureEvents: number
  pastEvents: number
  totalCapacity: number
  totalRemainingCapacity: number
  eventStats: OrganizerWorkspaceEventStats[]
}

export interface OrganizerWorkspace {
  organizer: Organizer
  availableServices: ServiceCatalogItem[]
  organizerServices: OrganizerCatalogService[]
  tracks: Track[]
  trackServices: TrackServiceAssignment[]
  events: OrganizerManagedEvent[]
  stats: OrganizerWorkspaceStats
}

export interface OrganizerEventServicePayload {
  trackServiceId?: number
  organizerServiceId?: number
  price: number
}

export interface OrganizerEventPayload {
  trackId: number
  eventDate: string
  basePrice: number
  maxParticipants: number
  description: string
  services: OrganizerEventServicePayload[]
}
