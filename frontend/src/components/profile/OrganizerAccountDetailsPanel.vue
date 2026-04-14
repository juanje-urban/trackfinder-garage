<script setup lang="ts">
import { Pencil } from 'lucide-vue-next'
import type { OrganizerProfile } from '@/types/organizer'
import type { ProfileFormState } from '@/types/profile'
import { formatDisplayDate } from '@/utils/format'

defineProps<{
  organizerProfile: OrganizerProfile
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
          <h3 class="ui-title-card">Datos de organizador</h3>
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
          <strong class="surface-detail-item__value">{{ organizerProfile.displayName }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Nombre</span>
          <input v-model="profileForm.name" type="text" autocomplete="given-name" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Nombre</span>
          <strong class="surface-detail-item__value">{{ organizerProfile.name }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Apellidos</span>
          <input v-model="profileForm.surname" type="text" autocomplete="family-name" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Apellidos</span>
          <strong class="surface-detail-item__value">{{ organizerProfile.surname }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Correo electr&oacute;nico</span>
          <input v-model="profileForm.email" type="email" autocomplete="email" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Correo electr&oacute;nico</span>
          <strong class="surface-detail-item__value">{{ organizerProfile.email }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Telefono</span>
          <input v-model="profileForm.phone" type="tel" autocomplete="tel" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Telefono</span>
          <strong class="surface-detail-item__value">{{ organizerProfile.phone }}</strong>
        </div>

        <div class="surface-detail-item">
          <span class="surface-detail-item__label">Alta</span>
          <strong class="surface-detail-item__value">
            {{ organizerProfile.created ? formatDisplayDate(organizerProfile.created.slice(0, 10)) : 'Sin fecha' }}
          </strong>
        </div>

        <div class="surface-detail-item">
          <span class="surface-detail-item__label">Estado de cuenta</span>
          <strong class="surface-detail-item__value">
            {{ organizerProfile.userEnabled ? 'Activa' : 'Inactiva' }}
          </strong>
        </div>

        <label v-if="editMode" class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Direccion</span>
          <input
            v-model="profileForm.address"
            type="text"
            autocomplete="street-address"
          />
        </label>
        <div v-else class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Direccion</span>
          <strong class="surface-detail-item__value">{{ organizerProfile.address }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">Razon social</span>
          <input v-model="profileForm.legalName" type="text" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">Razon social</span>
          <strong class="surface-detail-item__value">{{ organizerProfile.legalName }}</strong>
        </div>

        <label v-if="editMode" class="surface-detail-item">
          <span class="surface-detail-item__label">CIF</span>
          <input v-model="profileForm.cif" type="text" />
        </label>
        <div v-else class="surface-detail-item">
          <span class="surface-detail-item__label">CIF</span>
          <strong class="surface-detail-item__value">{{ organizerProfile.cif }}</strong>
        </div>

        <div class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Estado de organizador</span>
          <strong class="surface-detail-item__value">
            {{ organizerProfile.organizerEnabled ? 'Validado' : 'Pendiente de validacion' }}
          </strong>
        </div>

        <label v-if="editMode" class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Nueva contrasena</span>
          <input
            v-model="profileForm.password"
            type="password"
            autocomplete="new-password"
            placeholder="Dejala en blanco si no quieres cambiarla"
          />
        </label>
        <label v-if="editMode" class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Confirmar nueva contrasena</span>
          <input
            v-model="profileForm.passwordConfirmation"
            type="password"
            autocomplete="new-password"
            placeholder="Repite la nueva contrasena"
          />
        </label>
        <div v-else class="surface-detail-item surface-detail-item--full">
          <span class="surface-detail-item__label">Contrasena</span>
          <strong class="surface-detail-item__value">************</strong>
        </div>
      </div>
    </article>
  </section>
</template>
