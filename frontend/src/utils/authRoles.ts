// Normalizo los roles por seguridad para que el front no dependa de mayúsculas/minúsculas del backend.
export function normalizeRoleName(roleName: string | null | undefined): string {
  return roleName?.trim().toUpperCase() ?? ''
}

export function isAdminRole(roleName: string | null | undefined): boolean {
  return normalizeRoleName(roleName) === 'ADMIN'
}

export function isUserRole(roleName: string | null | undefined): boolean {
  return normalizeRoleName(roleName) === 'USER'
}

export function isOrganizerRole(roleName: string | null | undefined): boolean {
  return normalizeRoleName(roleName) === 'ORGANIZER'
}
