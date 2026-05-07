<script setup lang="ts">
import { Eye, EyeOff, Lock, Mail, User } from 'lucide-vue-next'
import type { AuthOrganizerRegisterPayload } from '@/types/auth'
import type { AuthMode } from '@/types/authDialog'

// Formulario de acceso controlado por el padre. Aquí pinto campos y emito acciones.
const props = defineProps<{
  mode: AuthMode
  isRegistrationMode: boolean
  isOrganizerRegisterMode: boolean
  submitLabel: string
  switchPrompt: string
  switchActionLabel: string
  isSubmitting: boolean
  error: string
  showPassword: boolean
  form: AuthOrganizerRegisterPayload
}>()

defineEmits<{
  // El padre cambia modo, envía formulario y alterna visibilidad de contraseña.
  setMode: [mode: AuthMode]
  togglePasswordVisibility: []
  submit: []
}>()

function updateField(
  field: keyof AuthOrganizerRegisterPayload,
  event: Event,
) {
  // Mutar 'props.form' aquí funciona porque el objeto reactivo viene del padre.
  props.form[field] = (event.target as HTMLInputElement).value
}

function toggleOrganizerMode() {
  // Alterno entre registro normal y registro de organizador.
  const nextMode: AuthMode = props.mode === 'organizer-register' ? 'register' : 'organizer-register'
  return nextMode
}
</script>

<template>
  <div class="auth-dialog__content">
    <div class="auth-dialog__header">
      <p class="ui-eyebrow">Acceso al garage</p>
      <slot name="title" />
      <slot name="subtitle" />
    </div>

    <div
      v-if="!isOrganizerRegisterMode"
      class="auth-tabs"
      role="tablist"
      aria-label="Seleccionar modo de autenticacion"
    >
      <button
        class="auth-tab"
        :class="{ 'auth-tab--active': mode === 'login' }"
        type="button"
        @click="$emit('setMode', 'login')"
      >
        Iniciar sesión
      </button>
      <button
        class="auth-tab"
        :class="{ 'auth-tab--active': mode === 'register' }"
        type="button"
        @click="$emit('setMode', 'register')"
      >
        Registrarse
      </button>
    </div>

    <form
      class="auth-form"
      :class="{ 'auth-form--register': isRegistrationMode }"
      novalidate
      @submit.prevent="$emit('submit')"
    >
      <label v-if="isRegistrationMode" class="auth-field">
        <span class="auth-field__label">Alias público</span>
        <span class="auth-field__control">
          <User :size="16" class="auth-field__icon" />
          <input
            :value="form.displayName"
            type="text"
            autocomplete="nickname"
            placeholder="Elige tu alias de comunidad"
            @input="updateField('displayName', $event)"
          />
        </span>
      </label>

      <label v-if="isRegistrationMode" class="auth-field">
        <span class="auth-field__label">Nombre</span>
        <span class="auth-field__control">
          <User :size="16" class="auth-field__icon" />
          <input
            :value="form.name"
            type="text"
            autocomplete="given-name"
            placeholder="Tu nombre"
            @input="updateField('name', $event)"
          />
        </span>
      </label>

      <label v-if="isRegistrationMode" class="auth-field">
        <span class="auth-field__label">Apellidos</span>
        <span class="auth-field__control">
          <User :size="16" class="auth-field__icon" />
          <input
            :value="form.surname"
            type="text"
            autocomplete="family-name"
            placeholder="Tus apellidos"
            @input="updateField('surname', $event)"
          />
        </span>
      </label>

      <label v-if="isRegistrationMode" class="auth-field">
        <span class="auth-field__label">Teléfono</span>
        <span class="auth-field__control">
          <Lock :size="16" class="auth-field__icon" />
          <input
            :value="form.phone"
            type="tel"
            autocomplete="tel"
            placeholder="Tu teléfono"
            @input="updateField('phone', $event)"
          />
        </span>
      </label>

      <label v-if="isOrganizerRegisterMode" class="auth-field">
        <span class="auth-field__label">Razon social</span>
        <span class="auth-field__control">
          <User :size="16" class="auth-field__icon" />
          <input
            :value="form.legalName"
            type="text"
            autocomplete="organization"
            placeholder="Nombre legal de la empresa"
            @input="updateField('legalName', $event)"
          />
        </span>
      </label>

      <label v-if="isOrganizerRegisterMode" class="auth-field">
        <span class="auth-field__label">CIF</span>
        <span class="auth-field__control">
          <Lock :size="16" class="auth-field__icon" />
          <input
            :value="form.cif"
            type="text"
            autocomplete="off"
            placeholder="B12345678"
            @input="updateField('cif', $event)"
          />
        </span>
      </label>

      <label v-if="isRegistrationMode" class="auth-field auth-field--full">
        <span class="auth-field__label">Dirección</span>
        <span class="auth-field__control">
          <Mail :size="16" class="auth-field__icon" />
          <input
            :value="form.address"
            type="text"
            name="address-line1"
            autocomplete="address-line1"
            placeholder="Tu dirección"
            @input="updateField('address', $event)"
          />
        </span>
      </label>

      <label class="auth-field" :class="{ 'auth-field--full': isRegistrationMode }">
        <span class="auth-field__label">Correo electrónico</span>
        <span class="auth-field__control">
          <Mail :size="16" class="auth-field__icon" />
          <input
            :value="form.email"
            type="email"
            inputmode="email"
            autocomplete="email"
            placeholder="tu-correo@ejemplo.com"
            @input="updateField('email', $event)"
          />
        </span>
      </label>

      <label class="auth-field" :class="{ 'auth-field--full': isRegistrationMode }">
        <span class="auth-field__label">Contraseña</span>
        <span class="auth-field__control">
          <Lock :size="16" class="auth-field__icon" />
          <input
            :value="form.password"
            :type="showPassword ? 'text' : 'password'"
            :autocomplete="mode === 'login' ? 'current-password' : 'new-password'"
            placeholder="Introduce tu contraseña"
            @input="updateField('password', $event)"
          />
          <button
            class="auth-field__visibility"
            type="button"
            :aria-label="showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
            @click="$emit('togglePasswordVisibility')"
          >
            <EyeOff v-if="showPassword" :size="16" />
            <Eye v-else :size="16" />
          </button>
        </span>
      </label>

      <p v-if="error" class="auth-form__error auth-form__full">{{ error }}</p>

      <button
        class="action-button auth-form__submit auth-form__full"
        type="submit"
        :disabled="isSubmitting"
      >
        {{ isSubmitting ? 'Procesando...' : submitLabel }}
      </button>
    </form>

    <p class="auth-dialog__switch">
      {{ switchPrompt }}
      <button
        class="auth-dialog__switch-action"
        type="button"
        @click="$emit('setMode', toggleOrganizerMode())"
      >
        {{ switchActionLabel }}
      </button>
    </p>
  </div>
</template>

<style scoped>
.auth-dialog__content {
  display: grid;
  gap: var(--space-2xl);
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

@media (max-width: 640px) {
  .auth-form--register {
    grid-template-columns: 1fr;
  }

  .auth-form__full {
    grid-column: auto;
  }
}
</style>
