type TrackMedia = {
  gallery: string[]
  coverImage?: string
  layoutImage?: string
}

// Vite resuelve estos assets en build. Así puedo buscar imágenes por nombre corto del circuito.
const trackAssetUrls = import.meta.glob('../assets/tracks/*.{jpg,jpeg,png,webp,avif,svg}', {
  eager: true,
  import: 'default',
}) as Record<string, string>

function resolveTrackAsset(fileName: string): string | undefined {
  // Comparo en minúsculas para que el nombre del archivo no sea frágil por mayúsculas.
  const normalizedFileName = fileName.toLowerCase()
  const assetPath = Object.keys(trackAssetUrls).find((path) =>
    path.toLowerCase().endsWith(`/${normalizedFileName}`),
  )

  return assetPath ? trackAssetUrls[assetPath] : undefined
}

function resolveTrackAssetByBaseName(baseName: string): string | undefined {
  const layoutExtensions = ['svg', 'jpg', 'jpeg', 'png', 'webp', 'avif']

  // Pruebo varias extensiones para poder cambiar el formato del layout sin tocar código.
  return layoutExtensions
    .map((extension) => resolveTrackAsset(`${baseName}.${extension}`))
    .find((assetUrl): assetUrl is string => Boolean(assetUrl))
}

export function getTrackMedia(shortName?: string | null): TrackMedia {
  const normalizedShortName = shortName?.trim()

  if (!normalizedShortName) {
    return { gallery: [] }
  }

  // Convención de assets: 'shortName_cover_1.jpg', 'shortName_cover_2.jpg' y 'shortName_layout.*'.
  const gallery = [1, 2]
    .map((index) => resolveTrackAsset(`${normalizedShortName}_cover_${index}.jpg`))
    .filter((assetUrl): assetUrl is string => Boolean(assetUrl))

  return {
    gallery,
    coverImage: gallery[0],
    layoutImage: resolveTrackAssetByBaseName(`${normalizedShortName}_layout`),
  }
}
