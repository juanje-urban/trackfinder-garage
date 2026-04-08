type TrackMediaManifestEntry = {
  gallery: string[]
  layout: string
}

export type TrackMedia = {
  gallery: string[]
  coverImage?: string
  layoutImage?: string
}

const trackAssetUrls = import.meta.glob('../assets/tracks/*.{jpg,jpeg,png,webp,avif,svg}', {
  eager: true,
  import: 'default',
}) as Record<string, string>

function defineTrackMedia(slug: string, galleryCount = 2): TrackMediaManifestEntry {
  return {
    gallery: Array.from(
      { length: galleryCount },
      (_, index) => `${slug}_cover_${index + 1}.jpg`,
    ),
    layout: `${slug}_layout.jpg`,
  }
}

// Update only the dummy file names here as you add real assets to src/assets/tracks.
export const trackMediaManifest: Record<string, TrackMediaManifestEntry> = {
  'Circuit Calafat': defineTrackMedia('calafat'),
  'Circuito de Madrid Jarama - RACE': defineTrackMedia('jarama'),
  'Circuit Ricardo Tormo': defineTrackMedia('ricardo_tormo'),
  'Circuito Mike G Guadix': defineTrackMedia('guadix'),
  'Autodromo Internacional do Algarve': defineTrackMedia('algarve'),
  Nurburgring: defineTrackMedia('nurburgring'),
  'Circuit de la Sarthe': defineTrackMedia('le_mans_sarthe'),
  'Bugatti Circuit': defineTrackMedia('le_mans_bugatti'),
  'Nurburgring Grand Prix-Strecke': defineTrackMedia('nurburgring_gp'),
  'Circuit de Barcelona-Catalunya': defineTrackMedia('barcelona'),
  'Circuito de Jerez - Angel Nieto': defineTrackMedia('jerez'),
  'MotorLand Aragon': defineTrackMedia('motorland'),
  'Circuito de Navarra': defineTrackMedia('navarra'),
  'Circuito de Albacete': defineTrackMedia('albacete'),
  'Circuito de Monteblanco': defineTrackMedia('monteblanco'),
  'Circuito de Cartagena': defineTrackMedia('cartagena'),
  'Circuito do Estoril': defineTrackMedia('estoril'),
  'Circuito Vasco Sameiro': defineTrackMedia('braga'),
  'Circuito de Vila Real': defineTrackMedia('vila_real'),
  'Circuito da Boavista': defineTrackMedia('boavista'),
  'Circuit de Spa-Francorchamps': defineTrackMedia('spa'),
  'Mugello Circuit': defineTrackMedia('mugello'),
  'Circuit Paul Ricard': defineTrackMedia('paul_ricard'),
}

function resolveTrackAsset(fileName: string): string | undefined {
  const normalizedFileName = fileName.toLowerCase()
  const assetPath = Object.keys(trackAssetUrls).find((path) =>
    path.toLowerCase().endsWith(`/${normalizedFileName}`),
  )

  return assetPath ? trackAssetUrls[assetPath] : undefined
}

export function getTrackMedia(trackName: string): TrackMedia {
  const manifestEntry = trackMediaManifest[trackName]

  if (!manifestEntry) {
    return { gallery: [] }
  }

  const gallery = manifestEntry.gallery
    .map(resolveTrackAsset)
    .filter((assetUrl): assetUrl is string => Boolean(assetUrl))

  return {
    gallery,
    coverImage: gallery[0],
    layoutImage: resolveTrackAsset(manifestEntry.layout),
  }
}
