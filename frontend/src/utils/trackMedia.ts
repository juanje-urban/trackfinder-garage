type TrackMediaManifestEntry = {
  gallery: string[]
  layoutBaseName: string
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
    layoutBaseName: `${slug}_layout`,
  }
}

const trackMediaManifest = {
  calafat: defineTrackMedia('calafat'),
  jarama: defineTrackMedia('jarama'),
  ricardoTormo: defineTrackMedia('ricardo_tormo'),
  guadix: defineTrackMedia('guadix'),
  algarve: defineTrackMedia('algarve'),
  nurburgring: defineTrackMedia('nurburgring'),
  leMansSarthe: defineTrackMedia('le_mans_sarthe'),
  leMansBugatti: defineTrackMedia('le_mans_bugatti'),
  nurburgringGp: defineTrackMedia('nurburgring_gp'),
  barcelona: defineTrackMedia('barcelona'),
  jerez: defineTrackMedia('jerez'),
  motorland: defineTrackMedia('motorland'),
  navarra: defineTrackMedia('navarra'),
  albacete: defineTrackMedia('albacete'),
  monteblanco: defineTrackMedia('monteblanco'),
  cartagena: defineTrackMedia('cartagena'),
  estoril: defineTrackMedia('estoril'),
  braga: defineTrackMedia('braga'),
  vilaReal: defineTrackMedia('vila_real'),
  boavista: defineTrackMedia('boavista'),
  spa: defineTrackMedia('spa'),
  mugello: defineTrackMedia('mugello'),
  paulRicard: defineTrackMedia('paul_ricard'),
}

type TrackMediaKey = keyof typeof trackMediaManifest

// Relacion entre el nombre real del circuito y el slug de assets del front.
const trackMediaNameMap: Record<string, TrackMediaKey> = {
  'Circuit Calafat': 'calafat',
  'Circuito de Madrid Jarama - RACE': 'jarama',
  'Circuit Ricardo Tormo': 'ricardoTormo',
  'Circuito Mike G Guadix': 'guadix',
  'Autódromo Internacional do Algarve': 'algarve',
  'Autodromo Internacional do Algarve': 'algarve',
  'Nürburgring': 'nurburgring',
  Nurburgring: 'nurburgring',
  'Circuit de la Sarthe': 'leMansSarthe',
  'Bugatti Circuit': 'leMansBugatti',
  'Nürburgring Grand Prix-Strecke': 'nurburgringGp',
  'Nurburgring Grand Prix-Strecke': 'nurburgringGp',
  'Circuit de Barcelona-Catalunya': 'barcelona',
  'Circuito de Jerez - Ángel Nieto': 'jerez',
  'Circuito de Jerez - Angel Nieto': 'jerez',
  'MotorLand Aragón': 'motorland',
  'MotorLand Aragon': 'motorland',
  'Circuito de Navarra': 'navarra',
  'Circuito de Albacete': 'albacete',
  'Circuito de Monteblanco': 'monteblanco',
  'Circuito de Cartagena': 'cartagena',
  'Circuito do Estoril': 'estoril',
  'Circuito Vasco Sameiro': 'braga',
  'Circuito de Vila Real': 'vilaReal',
  'Circuito da Boavista': 'boavista',
  'Circuit de Spa-Francorchamps': 'spa',
  'Mugello Circuit': 'mugello',
  'Circuit Paul Ricard': 'paulRicard',
}

function resolveTrackAsset(fileName: string): string | undefined {
  const normalizedFileName = fileName.toLowerCase()
  const assetPath = Object.keys(trackAssetUrls).find((path) =>
    path.toLowerCase().endsWith(`/${normalizedFileName}`),
  )

  return assetPath ? trackAssetUrls[assetPath] : undefined
}

function resolveTrackAssetByBaseName(baseName: string): string | undefined {
  const layoutExtensions = ['svg', 'jpg', 'jpeg', 'png', 'webp', 'avif']

  return layoutExtensions
    .map((extension) => resolveTrackAsset(`${baseName}.${extension}`))
    .find((assetUrl): assetUrl is string => Boolean(assetUrl))
}

export function getTrackMedia(trackName: string): TrackMedia {
  const mediaKey = trackMediaNameMap[trackName]
  const manifestEntry = mediaKey ? trackMediaManifest[mediaKey] : undefined

  if (!manifestEntry) {
    return { gallery: [] }
  }

  const gallery = manifestEntry.gallery
    .map(resolveTrackAsset)
    .filter((assetUrl): assetUrl is string => Boolean(assetUrl))

  return {
    gallery,
    coverImage: gallery[0],
    layoutImage: resolveTrackAssetByBaseName(manifestEntry.layoutBaseName),
  }
}
