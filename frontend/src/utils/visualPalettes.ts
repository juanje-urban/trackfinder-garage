const fallbackPalette = ['#4c1717', '#24365a'] as const

export const eventVisualPalettes = [
  ['#4c1717', '#24365a'],
  ['#203d56', '#6d1e18'],
  ['#1b283f', '#6b3a1d'],
  ['#5a1717', '#15444d'],
] as const

export const trackVisualPalettes = [
  ['#2a3e58', '#5a1b1b'],
  ['#204857', '#7a2c1d'],
  ['#3f2d63', '#15444f'],
  ['#5b2118', '#243a63'],
] as const

export function createVisualStyle(
  index: number,
  palettes: readonly (readonly [string, string])[],
  startVar: string,
  endVar: string,
): Record<string, string> {
  const palette = palettes[index % palettes.length] ?? fallbackPalette
  const [start, end] = palette

  return {
    [startVar]: start,
    [endVar]: end,
  }
}
