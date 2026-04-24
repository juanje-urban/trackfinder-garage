import { isAxiosError } from 'axios'

type ApiErrorMatch = {
  includes: string
  message: string
}

type ResolveApiErrorOptions = {
  fallback: string
  statusMessages?: Partial<Record<number, string>>
  matches?: ApiErrorMatch[]
}

function getBackendErrorMessage(error: unknown): string | null {
  if (!isAxiosError(error)) {
    return null
  }

  const responseData = error.response?.data
  const backendMessage = responseData?.error

  if (typeof backendMessage === 'string' && backendMessage.trim() !== '') {
    return backendMessage
  }

  if (!responseData || typeof responseData !== 'object') {
    return null
  }

  const firstFieldError = Object.values(responseData).find(
    (value): value is string => typeof value === 'string' && value.trim() !== '',
  )

  return firstFieldError ?? null
}

export function resolveApiErrorMessage(
  error: unknown,
  { fallback, statusMessages = {}, matches = [] }: ResolveApiErrorOptions,
): string {
  const backendMessage = getBackendErrorMessage(error)

  if (backendMessage) {
    const matchedMessage = matches.find(({ includes }) => backendMessage.includes(includes))
    return matchedMessage?.message ?? backendMessage
  }

  if (isAxiosError(error)) {
    const statusMessage = statusMessages[error.response?.status ?? 0]
    if (statusMessage) {
      return statusMessage
    }
  }

  return fallback
}
