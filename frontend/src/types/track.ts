// Circuito visible en el catálogo. 'shortName' sirve para enlazar con los assets del front.
export interface Track {
  id: number
  name: string
  shortName: string
  location: string
  description: string
}

export interface TrackPayload {
  name: string
  shortName: string
  location: string
  description: string
}
