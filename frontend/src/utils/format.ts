// Formateo fechas sin UTC para evitar que un cambio de zona horaria mueva el día.
export function formatDisplayDate(date: string): string {
  const [year, month, day] = date.split('-').map(Number)

  if (
    year === undefined ||
    month === undefined ||
    day === undefined ||
    Number.isNaN(year) ||
    Number.isNaN(month) ||
    Number.isNaN(day)
  ) {
    return date
  }

  return new Intl.DateTimeFormat('es-ES', {
    dateStyle: 'long',
  }).format(new Date(year, month - 1, day))
}

// Mantengo los formatos visuales en un solo sitio para que toda la app hable igual.
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount)
}

// El backend guarda la vuelta en milisegundos; aquí la convierto a 'm:ss.mmm'.
export function formatLapTime(lapTimeMs: number): string {
  const totalMinutes = Math.floor(lapTimeMs / 60000)
  const totalSeconds = Math.floor((lapTimeMs % 60000) / 1000)
  const milliseconds = lapTimeMs % 1000

  return `${totalMinutes}:${String(totalSeconds).padStart(2, '0')}.${String(milliseconds).padStart(3, '0')}`
}
