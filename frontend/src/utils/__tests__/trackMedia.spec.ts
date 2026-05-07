import { describe, expect, it } from 'vitest'
import { getTrackMedia } from '@/utils/trackMedia'

describe('getTrackMedia', () => {
  it('returns empty media when the short name is missing', () => {
    expect(getTrackMedia('')).toEqual({ gallery: [] })
    expect(getTrackMedia(null)).toEqual({ gallery: [] })
  })

  it('resolves known track assets from the short name', () => {
    const media = getTrackMedia(' prueba ')

    expect(media.gallery).toHaveLength(2)
    expect(media.coverImage).toBe(media.gallery[0])
    expect(media.layoutImage).toBeTruthy()
  })

  it('returns an empty gallery for unknown track assets', () => {
    expect(getTrackMedia('no_existe')).toEqual({
      gallery: [],
      coverImage: undefined,
      layoutImage: undefined,
    })
  })
})
