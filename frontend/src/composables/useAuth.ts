import { computed, reactive } from 'vue'
import { getCurrentSession } from '@/services/authService'
import type { AuthSession } from '@/types/auth'

const STORAGE_KEY = 'trackfinder-garage.auth'

type AuthState = {
  session: AuthSession | null
  isDialogOpen: boolean
}

const state = reactive<AuthState>({
  session: loadStoredSession(),
  isDialogOpen: false,
})

export function useAuth() {
  return {
    session: computed(() => state.session),
    isAuthenticated: computed(() => state.session !== null),
    isDialogOpen: computed(() => state.isDialogOpen),
    openAuthDialog,
    closeAuthDialog,
    setSession,
    clearSession,
    refreshSession,
  }
}

export function getAuthorizationHeader(): string | null {
  return state.session?.authorizationHeader ?? null
}

function openAuthDialog() {
  state.isDialogOpen = true
}

function closeAuthDialog() {
  state.isDialogOpen = false
}

function setSession(session: AuthSession) {
  state.session = session
  persistSession(session)
}

function clearSession() {
  state.session = null
  removeStoredSession()
}

async function refreshSession() {
  if (!state.session) {
    return
  }

  try {
    const nextSession = await getCurrentSession()
    setSession(nextSession)
  } catch {
    clearSession()
  }
}

function loadStoredSession(): AuthSession | null {
  if (typeof window === 'undefined') {
    return null
  }

  const rawValue = window.localStorage.getItem(STORAGE_KEY)

  if (!rawValue) {
    return null
  }

  try {
    const parsedValue = JSON.parse(rawValue) as Partial<AuthSession>

    if (
      typeof parsedValue.userId !== 'number' ||
      typeof parsedValue.displayName !== 'string' ||
      typeof parsedValue.email !== 'string' ||
      typeof parsedValue.authorizationHeader !== 'string'
    ) {
      removeStoredSession()
      return null
    }

    return {
      userId: parsedValue.userId,
      displayName: parsedValue.displayName,
      email: parsedValue.email,
      roleName: typeof parsedValue.roleName === 'string' ? parsedValue.roleName : null,
      authorizationHeader: parsedValue.authorizationHeader,
    }
  } catch {
    removeStoredSession()
    return null
  }
}

function persistSession(session: AuthSession) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

function removeStoredSession() {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.removeItem(STORAGE_KEY)
}
