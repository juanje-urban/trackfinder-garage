// Mantengo el formato de mensajes fuera de la vista para que la plantilla sea más fácil de leer.
export function formatMessageTimestamp(value: string): string {
  return new Date(value).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Traduzco los roles técnicos a etiquetas humanas en la bandeja de mensajes.
export function formatMessageRoleLabel(roleName: string | null): string {
  switch ((roleName ?? '').trim().toUpperCase()) {
    case 'ADMIN':
      return 'Administrador'
    case 'ORGANIZER':
      return 'Organizador'
    case 'USER':
      return 'Usuario'
    default:
      return ''
  }
}
