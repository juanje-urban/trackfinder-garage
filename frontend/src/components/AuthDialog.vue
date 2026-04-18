<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, watch } from 'vue'
import { X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import heroImage from '@/assets/tracks/ricardo_tormo_cover_1.jpg'
import logoUrl from '@/assets/tfg_logo.svg'
import AuthAccessPanel from '@/components/auth/AuthAccessPanel.vue'
import AuthSessionPanel from '@/components/auth/AuthSessionPanel.vue'
import { useAuth } from '@/composables/useAuth'
import { login, register, registerOrganizer } from '@/services/authService'
import type { AuthOrganizerRegisterPayload } from '@/types/auth'
import type { AuthMode } from '@/types/authDialog'
import { resolveApiErrorMessage } from '@/utils/apiErrors'
import { getDisplayNameMonogram } from '@/utils/identity'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MAX_LONG_FIELD_LENGTH = 255
const MAX_SHORT_FIELD_LENGTH = 20

const auth = useAuth()
const router = useRouter()
const form = reactive<AuthOrganizerRegisterPayload>({
  displayName: '',
  email: '',
  password: '',
  name: '',
  surname: '',
  address: '',
  phone: '',
  legalName: '',
  cif: '',
})
const mode = reactive<{ value: AuthMode }>({
  value: 'login',
})
const uiState = reactive({
  isSubmitting: false,
  error: '',
  showPassword: false,
})

const isRegistrationMode = computed(() => mode.value !== 'login')
const isOrganizerRegisterMode = computed(() => mode.value === 'organizer-register')

const dialogTitle = computed(() => {
  if (auth.isAuthenticated.value) {
    return 'Sesión iniciada'
  }

  if (mode.value === 'organizer-register') {
    return 'Crea tu cuenta de organizador'
  }

  return mode.value === 'login' ? 'Bienvenido de nuevo' : 'Crea tu cuenta'
})

const dialogSubtitle = computed(() => {
  if (auth.isAuthenticated.value) {
    return 'Ya tienes acceso al garage. Puedes cerrar sesión cuando quieras.'
  }

  if (mode.value === 'organizer-register') {
    return 'Podrás iniciar sesión de inmediato. Tu perfil de organizador quedará pendiente de aprobación.'
  }

  return mode.value === 'login'
    ? 'Accede con tu correo y tu contraseña para entrar al garage.'
    : 'Regístrate con correo y contraseña para guardar tu acceso.'
})

const submitLabel = computed(() => {
  if (mode.value === 'organizer-register') {
    return 'Crear cuenta de organizador'
  }

  return mode.value === 'login' ? 'Iniciar sesión' : 'Crear cuenta'
})

const switchPrompt = computed(() =>
  mode.value === 'organizer-register'
    ? 'Prefieres una cuenta estandar?'
    : 'Quieres trabajar con nosotros?',
)

const switchActionLabel = computed(() =>
  mode.value === 'organizer-register'
    ? 'Volver al registro'
    : 'Hazte organizador',
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

  const validationError = validateForm(mode.value)
  if (validationError) {
    uiState.error = validationError
    return
  }

  uiState.isSubmitting = true

  try {
    const credentials = {
      email: form.email.trim().toLowerCase(),
      password: form.password,
    }

    const session =
      mode.value === 'login'
        ? await login(credentials)
        : mode.value === 'organizer-register'
          ? await registerOrganizer({
              ...credentials,
              displayName: form.displayName.trim(),
              name: form.name.trim(),
              surname: form.surname.trim(),
              address: form.address.trim(),
              phone: form.phone.trim(),
              legalName: form.legalName.trim(),
              cif: form.cif.trim(),
            })
          : await register({
              ...credentials,
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

function setMode(nextMode: AuthMode) {
  mode.value = nextMode
  uiState.error = ''
}

function togglePasswordVisibility() {
  uiState.showPassword = !uiState.showPassword
}

function closeDialog() {
  auth.closeAuthDialog()
}

async function logout() {
  auth.clearSession()
  auth.closeAuthDialog()
  mode.value = 'login'
  uiState.error = ''
  await router.push('/')
}

function handleDialogKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeDialog()
  }
}

function resetDialog() {
  mode.value = 'login'
  Object.assign(form, {
    displayName: '',
    email: '',
    password: '',
    name: '',
    surname: '',
    address: '',
    phone: '',
    legalName: '',
    cif: '',
  })
  uiState.error = ''
  uiState.showPassword = false
  uiState.isSubmitting = false
}

function getErrorMessage(error: unknown, currentMode: AuthMode): string {
  return resolveApiErrorMessage(error, {
    fallback:
      currentMode === 'login'
        ? 'No se pudo iniciar sesión.'
        : currentMode === 'organizer-register'
          ? 'No se pudo crear la cuenta de organizador.'
          : 'No se pudo crear la cuenta.',
    statusMessages: {
      401: 'Correo o contraseña incorrectos.',
      409:
        currentMode === 'organizer-register'
          ? 'No se pudo crear la cuenta de organizador porque ya existe un dato duplicado.'
          : 'No se pudo crear la cuenta porque ya existe un dato duplicado.',
    },
    matches: [
      { includes: 'display name', message: 'Ese alias ya esta en uso.' },
      { includes: 'email', message: 'Ya existe una cuenta registrada con ese correo.' },
      { includes: 'phone', message: 'Ya existe una cuenta registrada con ese teléfono.' },
      { includes: 'legal name', message: 'Ya existe un organizador con esa razón social.' },
      { includes: 'cif', message: 'Ya existe un organizador con ese CIF.' },
    ],
  })
}

function validateForm(currentMode: AuthMode): string {
  const email = form.email.trim()

  if (!email) {
    return 'Introduce tu correo electrónico.'
  }

  if (email.length > MAX_LONG_FIELD_LENGTH) {
    return 'El correo electrónico no puede superar 255 caracteres.'
  }

  if (!EMAIL_PATTERN.test(email)) {
    return 'Introduce un correo electrónico válido.'
  }

  if (!form.password.trim()) {
    return 'Introduce una contraseña.'
  }

  if (form.password.length > MAX_LONG_FIELD_LENGTH) {
    return 'La contraseña no puede superar 255 caracteres.'
  }

  if (currentMode === 'login') {
    return ''
  }

  const commonRegisterError =
    requireValue(form.displayName, 'Introduce un alias para tu perfil.') ||
    validateMaxLength(form.displayName, MAX_LONG_FIELD_LENGTH, 'El alias') ||
    requireValue(form.name, 'Introduce tu nombre.') ||
    validateMaxLength(form.name, MAX_LONG_FIELD_LENGTH, 'El nombre') ||
    requireValue(form.surname, 'Introduce tus apellidos.') ||
    validateMaxLength(form.surname, MAX_LONG_FIELD_LENGTH, 'Los apellidos') ||
    requireValue(form.address, 'Introduce tu dirección.') ||
    validateMaxLength(form.address, MAX_LONG_FIELD_LENGTH, 'La dirección') ||
    requireValue(form.phone, 'Introduce tu teléfono.') ||
    validateMaxLength(form.phone, MAX_SHORT_FIELD_LENGTH, 'El teléfono')

  if (commonRegisterError) {
    return commonRegisterError
  }

  if (currentMode === 'organizer-register') {
    return (
      requireValue(form.legalName, 'Introduce la razón social.') ||
      validateMaxLength(form.legalName, MAX_LONG_FIELD_LENGTH, 'La razón social') ||
      requireValue(form.cif, 'Introduce el CIF.') ||
      validateMaxLength(form.cif, MAX_SHORT_FIELD_LENGTH, 'El CIF')
    )
  }

  return ''
}

function requireValue(value: string, message: string): string {
  return value.trim() ? '' : message
}

function validateMaxLength(value: string, maxLength: number, label: string): string {
  return value.length > maxLength ? `${label} no puede superar ${maxLength} caracteres.` : ''
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

          <AuthSessionPanel
            v-if="auth.isAuthenticated.value"
            :session="auth.session.value"
            :dialog-title="dialogTitle"
            :dialog-subtitle="dialogSubtitle"
            :profile-monogram="profileMonogram"
            @close="closeDialog"
            @logout="logout"
          />

          <AuthAccessPanel
            v-else
            :mode="mode.value"
            :is-registration-mode="isRegistrationMode"
            :is-organizer-register-mode="isOrganizerRegisterMode"
            :submit-label="submitLabel"
            :switch-prompt="switchPrompt"
            :switch-action-label="switchActionLabel"
            :is-submitting="uiState.isSubmitting"
            :error="uiState.error"
            :show-password="uiState.showPassword"
            :form="form"
            @set-mode="setMode"
            @toggle-password-visibility="togglePasswordVisibility"
            @submit="submit"
          >
            <template #title>
              <h2 id="auth-dialog-title" class="ui-title-section">{{ dialogTitle }}</h2>
            </template>
            <template #subtitle>
              <p class="ui-copy-muted">{{ dialogSubtitle }}</p>
            </template>
          </AuthAccessPanel>
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
  border: 1px solid rgba(255, 114, 114, 0.5);
  border-radius: 999px;
  color: #fff4f4;
  background: linear-gradient(135deg, rgba(214, 31, 31, 0.94), rgba(146, 12, 12, 0.94));
  box-shadow: 0 14px 30px rgba(146, 12, 12, 0.24);
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
}
</style>
