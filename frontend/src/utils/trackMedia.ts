export type TrackMedia = {
  gallery: string[]
  coverImage?: string
  layoutImage?: string
}

const trackAssetUrls = import.meta.glob('../assets/tracks/*.{jpg,jpeg,png,webp,avif,svg}', {
  eager: true,
  import: 'default',
}) as Record<string, string>

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

export function getTrackMedia(shortName?: string | null): TrackMedia {
  const normalizedShortName = shortName?.trim()

  if (!normalizedShortName) {
    return { gallery: [] }
  }

  const gallery = [1, 2]
    .map((index) => resolveTrackAsset(`${normalizedShortName}_cover_${index}.jpg`))
    .filter((assetUrl): assetUrl is string => Boolean(assetUrl))

  return {
    gallery,
    coverImage: gallery[0],
    layoutImage: resolveTrackAssetByBaseName(`${normalizedShortName}_layout`),
  }
}
