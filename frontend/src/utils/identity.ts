// Saco iniciales legibles para avatares cuando no tengo una imagen real del usuario.
export function getDisplayNameMonogram(displayName: string): string {
  const parts = displayName
    .replaceAll(/[_./-]+/g, ' ')
    .trim()
    .split(/\s+/)
    .filter(Boolean)

  if (parts.length >= 2) {
    return `${parts[0]?.[0] ?? ''}${parts[1]?.[0] ?? ''}`.toUpperCase()
  }

  return displayName.replaceAll(/\s+/g, '').slice(0, 2).toUpperCase()
}
