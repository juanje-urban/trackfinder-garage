<script setup lang="ts">
import { Pencil } from 'lucide-vue-next'
import type { ProfileFormState } from '@/types/profile'
import type { UserProfile } from '@/types/user'
import { formatDisplayDate } from '@/utils/format'

defineProps<{
  profile: UserProfile
  profileForm: ProfileFormState
  editMode: boolean
  saving: boolean
  errorMessage: string
}>()

defineEmits<{
  edit: []
  cancel: []
  save: []
}>()
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="errorMessage" class="status-message status-message--error">{{ errorMessage }}</p>

    <article class="panel panel-pad-lg panel-stack-lg">
      <div class="section-header">
        <div class="panel-copy">
          <h3 class="ui-title-card">Datos personales</h3>
        </div>

        <div class="section-header__actions">
          <button
            v-if="!editMode"
            class="icon-button icon-button--danger"
            type="button"
            aria-label="Editar perfil"
            title="Editar perfil"
            @click="$emit('edit')"
          >
            <Pencil :size="16" aria-hidden="true" />
          </button>

          <template v-else>
            <button class="action-button action-button--ghost" type="button" @click="$emit('cancel')">
              Cancelar
            </button>
            <button class="action-button" type="button" :disabled="saving" @click="$emit('save')">
              {{ saving ? 'Guardando...' : 'Guardar cambios' }}
            </button>
          </template>
        </div>
      </div>

      <div class="surface-detail-grid">
        <div class="surface-detail-item">
          <span class="surface-detail-item__label">Alias</span>
          <strong class="surface-detail-item__value">{{ profile.displayName }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Nombre</span>
          <input v-model="profileForm.name" type="text" autocomplete="given-name" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Nombre</span>
          <strong class="surface-detail-item__value">{{ profile.name }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Apellidos</span>
          <input v-model="profileForm.surname" type="text" autocomplete="family-name" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Apellidos</span>
          <strong class="surface-detail-item__value">{{ profile.surname }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Correo electr&oacute;nico</span>
          <input v-model="profileForm.email" type="email" autocomplete="email" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Correo electr&oacute;nico</span>
          <strong class="surface-detail-item__value">{{ profile.email }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Teléfono</span>
          <input v-model="profileForm.phone" type="tel" autocomplete="tel" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Teléfono</span>
          <strong class="surface-detail-item__value">{{ profile.phone }}</strong>
        </div>

        <div class="surface-detail-item">
          <span class="surface-detail-item__label">Alta</span>
          <strong class="surface-detail-item__value">
            {{ formatDisplayDate(profile.created.slice(0, 10)) }}
          </strong>
        </div>

        <div class="surface-detail-item">
          <span class="surface-detail-item__label">Estado</span>
          <strong class="surface-detail-item__value">
            {{ profile.enabled ? 'Activa' : 'Inactiva' }}
          </strong>
        </div>

        <label v-if="editMode" class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Dirección</span>
          <input
            v-model="profileForm.address"
            type="text"
            autocomplete="street-address"
          />
        </label>
        <div v-else class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Dirección</span>
          <strong class="surface-detail-item__value">{{ profile.address }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Nueva contraseña</span>
          <input
            v-model="profileForm.password"
            type="password"
            autocomplete="new-password"
            placeholder="Dejala en blanco si no quieres cambiarla"
          />
        </label>
        <label v-if="editMode" class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Confirmar nueva contraseña</span>
          <input
            v-model="profileForm.passwordConfirmation"
            type="password"
            autocomplete="new-password"
            placeholder="Repite la nueva contraseña"
          />
        </label>
        <div v-else class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Contraseña</span>
          <strong class="surface-detail-item__value">************</strong>
        </div>
      </div>
    </article>
  </section>
</template>
