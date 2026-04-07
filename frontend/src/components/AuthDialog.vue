<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, watch } from 'vue'
import { AxiosError } from 'axios'
import { Eye, EyeOff, Lock, Mail, User, X } from 'lucide-vue-next'
import heroImage from '@/assets/tracks/cheste_1.jpg'
import logoUrl from '@/assets/tfg_logo.svg'
import { useAuth } from '@/composables/useAuth'
import { login, register } from '@/services/authService'
import type { AuthRegisterPayload } from '@/types/auth'
import { getDisplayNameMonogram } from '@/utils/identity'

type AuthMode = 'login' | 'register'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const auth = useAuth()
const form = reactive<AuthRegisterPayload>({
  displayName: '',
  email: '',
  password: '',
  name: '',
  surname: '',
  address: '',
  phone: '',
})
const mode = reactive<{ value: AuthMode }>({
  value: 'login',
})
const uiState = reactive({
  isSubmitting: false,
  error: '',
  showPassword: false,
})

const dialogTitle = computed(() => {
  if (auth.isAuthenticated.value) {
    return 'Sesión iniciada'
  }

  return mode.value === 'login' ? 'Bienvenido de nuevo' : 'Crea tu cuenta'
})

const dialogSubtitle = computed(() => {
  if (auth.isAuthenticated.value) {
    return 'Ya tienes acceso al garage. Puedes cerrar sesión cuando quieras.'
  }

  return mode.value === 'login'
    ? 'Accede con tu correo y tu contraseña para entrar al garage.'
    : 'Regístrate con correo y contraseña para guardar tu acceso.'
})

const submitLabel = computed(() => (mode.value === 'login' ? 'Iniciar sesión' : 'Crear cuenta'))

const switchPrompt = computed(() =>
  mode.value === 'login'
    ? '¿Aún no tienes cuenta?'
    : '¿Ya tienes una cuenta creada?',
)

const switchActionLabel = computed(() =>
  mode.value === 'login' ? 'Regístrate' : 'Inicia sesión',
)

const profileMonogram = computed(() =>
  getDisplayNameMonogram(auth.session.value?.displayName ?? ''),
)

watch(
  () => auth.isDialogOpen.value,
  (isOpen) => {
    if (typeof document !== 'undefined') {
      document.body.style.overflow = isOpen ? 'hidden' : ''
    }

    if (!isOpen) {
      resetDialog()
      return
    }

    uiState.error = ''
  },
)

watch(
  () => auth.isAuthenticated.value,
  (isAuthenticated) => {
    if (isAuthenticated) {
      form.password = ''
      uiState.error = ''
    }
  },
)

onBeforeUnmount(() => {
  if (typeof document !== 'undefined') {
    document.body.style.overflow = ''
  }
})

async function submit() {
  uiState.error = ''

  if (!EMAIL_PATTERN.test(form.email.trim())) {
    uiState.error = 'Introduce un correo electrónico válido.'
    return
  }

  if (mode.value === 'register' && !form.displayName.trim()) {
    uiState.error = 'Introduce un alias para tu perfil.'
    return
  }

  if (mode.value === 'register' && !form.name.trim()) {
    uiState.error = 'Introduce tu nombre.'
    return
  }

  if (mode.value === 'register' && !form.surname.trim()) {
    uiState.error = 'Introduce tus apellidos.'
    return
  }

  if (mode.value === 'register' && !form.address.trim()) {
    uiState.error = 'Introduce tu dirección.'
    return
  }

  if (mode.value === 'register' && !form.phone.trim()) {
    uiState.error = 'Introduce tu teléfono.'
    return
  }

  if (!form.password.trim()) {
    uiState.error = 'Introduce una contraseña.'
    return
  }

  uiState.isSubmitting = true

  try {
    const payload = {
      email: form.email.trim().toLowerCase(),
      password: form.password,
    }

    const session =
      mode.value === 'login'
        ? await login(payload)
        : await register({
            ...payload,
            displayName: form.displayName.trim(),
            name: form.name.trim(),
            surname: form.surname.trim(),
            address: form.address.trim(),
            phone: form.phone.trim(),
          })

    auth.setSession(session)
    auth.closeAuthDialog()
    resetDialog()
  } catch (error) {
    uiState.error = getErrorMessage(error, mode.value)
  } finally {
    uiState.isSubmitting = false
  }
}

function switchMode(nextMode: AuthMode) {
  mode.value = nextMode
  uiState.error = ''
}

function togglePasswordVisibility() {
  uiState.showPassword = !uiState.showPassword
}

function closeDialog() {
  auth.closeAuthDialog()
}

function logout() {
  auth.clearSession()
  mode.value = 'login'
  uiState.error = ''
}

function handleDialogKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeDialog()
  }
}

function resetDialog() {
  mode.value = 'login'
  form.email = ''
  form.password = ''
  form.displayName = ''
  form.name = ''
  form.surname = ''
  form.address = ''
  form.phone = ''
  uiState.error = ''
  uiState.showPassword = false
  uiState.isSubmitting = false
}

function getErrorMessage(error: unknown, currentMode: AuthMode): string {
  if (error instanceof AxiosError) {
    const status = error.response?.status

    if (status === 401) {
      return 'Correo o contraseña incorrectos.'
    }

    if (status === 409 && currentMode === 'register') {
      const conflictMessage = extractBackendErrorMessage(error.response?.data)

      if (conflictMessage.includes('display name')) {
        return 'Ese alias ya está en uso.'
      }

      if (conflictMessage.includes('email')) {
        return 'Ya existe una cuenta registrada con ese correo.'
      }

      if (conflictMessage.includes('phone')) {
        return 'Ya existe una cuenta registrada con ese teléfono.'
      }

      return 'No se pudo crear la cuenta porque ya existe un dato duplicado.'
    }

    const backendMessage = extractBackendErrorMessage(error.response?.data)

    if (backendMessage) {
      return backendMessage
    }
  }

  return currentMode === 'login'
    ? 'No se pudo iniciar sesión.'
    : 'No se pudo crear la cuenta.'
}

function extractBackendErrorMessage(responseData: unknown): string {
  if (!responseData || typeof responseData !== 'object') {
    return ''
  }

  if ('error' in responseData && typeof responseData.error === 'string') {
    return responseData.error
  }

  const firstFieldError = Object.values(responseData).find(
    (value): value is string => typeof value === 'string',
  )

  return firstFieldError ?? ''
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="auth.isDialogOpen.value"
      class="auth-overlay"
      @keydown="handleDialogKeydown"
    >
      <button
        class="auth-overlay__backdrop"
        type="button"
        aria-label="Cerrar cuadro de autenticación"
        @click="closeDialog"
      ></button>

      <section class="auth-dialog" role="dialog" aria-modal="true" aria-labelledby="auth-dialog-title">
        <div class="auth-dialog__media" :style="{ backgroundImage: `url(${heroImage})` }"></div>

        <div class="auth-dialog__panel">
          <button class="auth-dialog__close" type="button" aria-label="Cerrar" @click="closeDialog">
            <X :size="18" />
          </button>

          <div class="auth-dialog__brand brand-lockup brand-lockup--header">
            <span class="brand-lockup__mark" aria-hidden="true">
              <img :src="logoUrl" alt="" class="auth-dialog__brand-icon" />
            </span>
            <span class="brand-lockup__name">TRACK<span>FINDER</span>GARAGE</span>
          </div>

          <div v-if="auth.isAuthenticated.value" class="auth-dialog__content auth-dialog__content--account">
            <div class="auth-account__avatar">{{ profileMonogram }}</div>
            <p class="ui-eyebrow">Acceso activo</p>
            <h2 id="auth-dialog-title" class="ui-title-section">{{ dialogTitle }}</h2>
            <p class="ui-copy-muted">{{ dialogSubtitle }}</p>

            <div class="auth-account__summary">
              <p><strong>Alias:</strong> {{ auth.session.value?.displayName }}</p>
              <p><strong>Correo:</strong> {{ auth.session.value?.email }}</p>
            </div>

            <div class="auth-dialog__actions">
              <button class="action-button" type="button" @click="closeDialog">Seguir navegando</button>
              <button class="action-button action-button--ghost" type="button" @click="logout">
                Cerrar sesión
              </button>
            </div>
          </div>

          <div v-else class="auth-dialog__content">
            <div class="auth-dialog__header">
              <p class="ui-eyebrow">Acceso al garage</p>
              <h2 id="auth-dialog-title" class="ui-title-section">{{ dialogTitle }}</h2>
              <p class="ui-copy-muted">{{ dialogSubtitle }}</p>
            </div>

            <div class="auth-tabs" role="tablist" aria-label="Seleccionar modo de autenticación">
              <button
                class="auth-tab"
                :class="{ 'auth-tab--active': mode.value === 'login' }"
                type="button"
                @click="switchMode('login')"
              >
                Iniciar sesión
              </button>
              <button
                class="auth-tab"
                :class="{ 'auth-tab--active': mode.value === 'register' }"
                type="button"
                @click="switchMode('register')"
              >
                Registrarse
              </button>
            </div>

            <form
              class="auth-form"
              :class="{ 'auth-form--register': mode.value === 'register' }"
              novalidate
              @submit.prevent="submit"
            >
              <label v-if="mode.value === 'register'" class="auth-field">
                <span class="auth-field__label">Alias público</span>
                <span class="auth-field__control">
                  <User :size="16" class="auth-field__icon" />
                  <input
                    v-model="form.displayName"
                    type="text"
                    autocomplete="nickname"
                    placeholder="Elige tu alias de comunidad"
                  />
                </span>
              </label>

              <label v-if="mode.value === 'register'" class="auth-field">
                <span class="auth-field__label">Nombre</span>
                <span class="auth-field__control">
                  <User :size="16" class="auth-field__icon" />
                  <input
                    v-model="form.name"
                    type="text"
                    autocomplete="given-name"
                    placeholder="Tu nombre"
                  />
                </span>
              </label>

              <label v-if="mode.value === 'register'" class="auth-field">
                <span class="auth-field__label">Apellidos</span>
                <span class="auth-field__control">
                  <User :size="16" class="auth-field__icon" />
                  <input
                    v-model="form.surname"
                    type="text"
                    autocomplete="family-name"
                    placeholder="Tus apellidos"
                  />
                </span>
              </label>

              <label v-if="mode.value === 'register'" class="auth-field">
                <span class="auth-field__label">Teléfono</span>
                <span class="auth-field__control">
                  <Lock :size="16" class="auth-field__icon" />
                  <input
                    v-model="form.phone"
                    type="tel"
                    autocomplete="tel"
                    placeholder="Tu teléfono"
                  />
                </span>
              </label>

              <label v-if="mode.value === 'register'" class="auth-field auth-field--full">
                <span class="auth-field__label">Dirección</span>
                <span class="auth-field__control">
                  <Mail :size="16" class="auth-field__icon" />
                  <input
                    v-model="form.address"
                    type="text"
                    autocomplete="street-address"
                    placeholder="Tu dirección"
                  />
                </span>
              </label>

              <label class="auth-field" :class="{ 'auth-field--full': mode.value === 'register' }">
                <span class="auth-field__label">Correo electrónico</span>
                <span class="auth-field__control">
                  <Mail :size="16" class="auth-field__icon" />
                  <input
                    v-model="form.email"
                    type="email"
                    inputmode="email"
                    autocomplete="email"
                    placeholder="tu-correo@ejemplo.com"
                  />
                </span>
              </label>

              <label class="auth-field" :class="{ 'auth-field--full': mode.value === 'register' }">
                <span class="auth-field__label">Contraseña</span>
                <span class="auth-field__control">
                  <Lock :size="16" class="auth-field__icon" />
                  <input
                    v-model="form.password"
                    :type="uiState.showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    placeholder="Introduce tu contraseña"
                  />
                  <button
                    class="auth-field__visibility"
                    type="button"
                    :aria-label="uiState.showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
                    @click="togglePasswordVisibility"
                  >
                    <EyeOff v-if="uiState.showPassword" :size="16" />
                    <Eye v-else :size="16" />
                  </button>
                </span>
              </label>

              <p v-if="uiState.error" class="auth-form__error auth-form__full">{{ uiState.error }}</p>

              <button
                class="action-button auth-form__submit auth-form__full"
                type="submit"
                :disabled="uiState.isSubmitting"
              >
                {{ uiState.isSubmitting ? 'Procesando...' : submitLabel }}
              </button>
            </form>

            <p class="auth-dialog__switch">
              {{ switchPrompt }}
              <button class="auth-dialog__switch-action" type="button" @click="switchMode(mode.value === 'login' ? 'register' : 'login')">
                {{ switchActionLabel }}
              </button>
            </p>
          </div>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.auth-overlay {
  position: fixed;
  inset: 0;
  z-index: 120;
  display: grid;
  place-items: center;
  padding: var(--space-2xl);
}

.auth-overlay__backdrop {
  position: absolute;
  inset: 0;
  border: 0;
  background: rgba(10, 4, 4, 0.76);
  backdrop-filter: blur(14px);
}

.auth-dialog {
  position: relative;
  z-index: 1;
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 420px);
  overflow: hidden;
  border: 1px solid var(--line-strong);
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(44, 17, 17, 0.98) 0%, rgba(24, 10, 10, 0.98) 100%);
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.5);
}

.auth-dialog__media {
  min-height: 620px;
  background-position: center;
  background-size: cover;
  position: relative;
}

.auth-dialog__media::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(9, 5, 5, 0.26) 0%, rgba(16, 8, 8, 0.78) 82%),
    linear-gradient(90deg, rgba(17, 7, 7, 0.12) 0%, rgba(17, 7, 7, 0.68) 100%);
}

.auth-dialog__panel {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: var(--space-3xl);
  background:
    linear-gradient(180deg, rgba(37, 15, 15, 0.96) 0%, rgba(21, 8, 8, 0.98) 100%);
}

.auth-dialog__close {
  position: absolute;
  top: var(--space-xl);
  right: var(--space-xl);
  width: 40px;
  height: 40px;
  border: 1px solid var(--line-faint);
  border-radius: 12px;
  color: var(--text-strong);
  background: var(--surface-glass-strong);
  display: grid;
  place-items: center;
}

.auth-dialog__brand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-4xl);
}

.auth-dialog__brand-icon {
  width: 18px;
  height: 18px;
  display: block;
}

.auth-dialog__content {
  display: grid;
  gap: var(--space-2xl);
}

.auth-dialog__content--account {
  justify-items: start;
}

.auth-dialog__header {
  display: grid;
  gap: var(--space-sm);
}

.auth-tabs {
  display: inline-grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-xs);
  padding: var(--space-xs);
  border: 1px solid var(--line-faint);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.02);
}

.auth-tab {
  min-height: 42px;
  border: 0;
  border-radius: 12px;
  color: var(--text-muted);
  background: transparent;
  font-weight: 700;
}

.auth-tab--active {
  color: var(--text-strong);
  background: var(--accent-gradient-horizontal);
  box-shadow: var(--accent-shadow);
}

.auth-form {
  display: grid;
  gap: var(--space-lg);
}

.auth-form--register {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: var(--space-lg);
}

.auth-field {
  display: grid;
  gap: var(--space-xs);
}

.auth-field--full {
  grid-column: 1 / -1;
}

.auth-form__full {
  grid-column: 1 / -1;
}

.auth-field__label {
  color: var(--text-on-media-soft);
  font-size: var(--fs-caption);
  font-weight: 600;
}

.auth-field__control {
  min-height: 54px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: var(--space-md);
  padding: 0 var(--space-lg);
  border: 1px solid var(--line-faint);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
}

.auth-field__control:focus-within {
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

.auth-field__icon,
.auth-field__visibility {
  color: var(--text-muted);
}

.auth-field__visibility {
  padding: 0;
  border: 0;
  background: transparent;
  display: inline-flex;
  align-items: center;
}

.auth-field input {
  min-width: 0;
  border: 0;
  outline: 0;
  color: var(--text-strong);
  background: transparent;
}

.auth-field input::placeholder {
  color: rgba(230, 213, 207, 0.52);
}

.auth-form__error {
  margin: 0;
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--error-border);
  border-radius: 14px;
  color: var(--error-text);
  background: var(--error-surface);
}

.auth-form__submit {
  width: 100%;
  margin-top: var(--space-xs);
}

.auth-dialog__switch {
  margin: 0;
  color: var(--text-muted);
  font-size: var(--fs-caption);
}

.auth-dialog__switch-action {
  margin-left: var(--space-xs);
  padding: 0;
  border: 0;
  color: var(--accent-strong);
  background: transparent;
  font-weight: 700;
}

.auth-account__avatar {
  width: 74px;
  height: 74px;
  display: grid;
  place-items: center;
  border: 1px solid var(--line-strong);
  border-radius: 22px;
  color: var(--text-strong);
  font-size: 1.4rem;
  font-weight: 800;
  background: var(--accent-gradient-horizontal);
  box-shadow: var(--accent-shadow);
}

.auth-account__summary {
  width: 100%;
  display: grid;
  gap: var(--space-sm);
  padding: var(--space-xl);
  border: 1px solid var(--line-faint);
  border-radius: 18px;
  background: var(--surface-glass);
}

.auth-account__summary p {
  margin: 0;
  color: var(--text-body);
}

.auth-dialog__actions {
  width: 100%;
  display: grid;
  gap: var(--space-md);
}

@media (max-width: 920px) {
  .auth-dialog {
    grid-template-columns: 1fr;
  }

  .auth-dialog__media {
    min-height: 220px;
  }
}

@media (max-width: 640px) {
  .auth-overlay {
    padding: var(--space-lg);
  }

  .auth-dialog__panel {
    padding: var(--space-xl);
  }

  .auth-dialog__brand {
    margin-bottom: var(--space-3xl);
  }

  .auth-form--register {
    grid-template-columns: 1fr;
  }

  .auth-form__full {
    grid-column: auto;
  }
}
</style>
