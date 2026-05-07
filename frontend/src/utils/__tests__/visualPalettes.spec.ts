import { describe, expect, it } from 'vitest'
import { createVisualStyle, eventVisualPalettes } from '@/utils/visualPalettes'

describe('createVisualStyle', () => {
  it('maps a stable palette to CSS custom properties', () => {
    expect(createVisualStyle(1, eventVisualPalettes, '--start', '--end')).toEqual({
      '--start': '#203d56',
      '--end': '#6d1e18',
    })
  })

  it('wraps indexes using the palette length', () => {
    expect(createVisualStyle(5, eventVisualPalettes, '--start', '--end')).toEqual({
      '--start': '#203d56',
      '--end': '#6d1e18',
    })
  })

  it('keeps extra styles in the returned object', () => {
    expect(
      createVisualStyle(0, eventVisualPalettes, '--start', '--end', {
        backgroundImage: 'linear-gradient(red, blue)',
      }),
    ).toMatchObject({
      '--start': '#4c1717',
      '--end': '#24365a',
      backgroundImage: 'linear-gradient(red, blue)',
    })
  })

  it('uses fallback palette when the palette list is empty', () => {
    expect(createVisualStyle(0, [], '--start', '--end')).toEqual({
      '--start': '#4c1717',
      '--end': '#24365a',
    })
  })
})
