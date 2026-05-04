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

// Esto es un composable: una función que puedo llamar desde cualquier componente, como los hook
// de React.
// Mantiene el 'state' fuera para que todos compartan la misma sesión, no una copia por componente.
export function useAuth() {
  return {
    // Expongo 'computed' para que Vue actualice la pantalla cuando cambie la sesión.
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
    // Valido la identidad contra el backend, pero conservo la cabecera Basic ya calculada.
    const nextIdentity = await getCurrentSession()
    setSession({
      ...nextIdentity,
      authorizationHeader: state.session.authorizationHeader,
    })
  } catch {
    clearSession()
  }
}

function loadStoredSession(): AuthSession | null {
  // En tests o renderizados fuera del navegador puede no existir window.
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
      // Si el localStorage está corrupto, limpiamos y empezamos sin sesión.
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
