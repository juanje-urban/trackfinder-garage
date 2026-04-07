export function getDisplayNameMonogram(displayName: string): string {
  const parts = displayName
    .replace(/[_./-]+/g, ' ')
    .trim()
    .split(/\s+/)
    .filter(Boolean)

  if (parts.length >= 2) {
    return `${parts[0]?.[0] ?? ''}${parts[1]?.[0] ?? ''}`.toUpperCase()
  }

  return displayName.replace(/\s+/g, '').slice(0, 2).toUpperCase()
}
