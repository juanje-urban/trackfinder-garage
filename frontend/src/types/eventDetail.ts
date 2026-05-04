import type { EventAvailabilityState } from '@/utils/eventAvailability'

// Tipos específicos de la pantalla de detalle. No vienen tal cual del backend, los preparo para pintar la UI.
export interface DisplayService {
  id: number
  name: string
  price: number
  source: 'track' | 'organizer'
}

export interface DisplayServiceGroup {
  id: 'track' | 'organizer'
  eyebrow: string
  title: string
  emptyMessage: string
  services: DisplayService[]
}

export interface MediaDialogState {
  eyebrow: string
  title: string
  src: string
  alt: string
}

export interface EventAvailabilitySummary {
  state: EventAvailabilityState
  label: string
  remainingLabel: string
}
