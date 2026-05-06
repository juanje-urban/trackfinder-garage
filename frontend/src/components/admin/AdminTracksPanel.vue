<script setup lang="ts">
import { Pencil, Plus, Trash2 } from 'lucide-vue-next'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'
import type { TrackServiceAssignment } from '@/types/trackService'
import type { Track } from '@/types/track'

// Panel de circuitos y sus servicios. Recibe datos ya filtrados desde AdminView.
const props = defineProps<{
  trackError: string
  serviceError: string
  sortedTracks: Track[]
  selectedTrackId: number | ''
  selectedServiceId: number | ''
  assignableServices: ServiceCatalogItem[]
  selectedTrackAssignments: TrackServiceAssignment[]
  assignmentSaving: boolean
  assignmentDeletingId: number | null
}>()

const emit = defineEmits<{
  // Uso emits para que este componente no tenga que conocer los servicios HTTP.
  openCreateTrack: []
  editTrack: [track: Track]
  'update:selectedTrackId': [value: number | '']
  'update:selectedServiceId': [value: number | '']
  addAssignment: []
  removeAssignment: [assignment: TrackServiceAssignment]
}>()

function readSelectedValue(event: Event): number | '' {
  // Los '<select>' devuelven strings. Convierto a número salvo la opción vacía.
  const value = (event.target as HTMLSelectElement).value
  return value === '' ? '' : Number(value)
}
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="trackError" class="status-message status-message--error">{{ trackError }}</p>
    <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

    <article class="panel panel-pad-lg panel-stack-md">
      <div class="section-header">
        <div class="panel-copy">
          <h3 class="ui-title-card">Circuitos</h3>
          <p class="ui-copy-muted">Edita los trazados existentes o da de alta uno nuevo.</p>
        </div>

        <button class="action-button admin-tracks__create" type="button" @click="$emit('openCreateTrack')">
          <Plus :size="16" aria-hidden="true" />
          Añadir circuito
        </button>
      </div>

      <div class="surface-card-list">
        <article v-for="track in sortedTracks" :key="track.id" class="surface-card">
          <div class="panel-copy surface-card__copy">
            <p class="ui-eyebrow">{{ track.location }}</p>
            <h4 class="ui-title-card">{{ track.name }}</h4>
            <p class="ui-copy-caption">Assets: {{ track.shortName }}</p>
            <p class="ui-copy-muted">{{ track.description }}</p>
          </div>

          <button
            class="icon-button icon-button--danger"
            type="button"
            aria-label="Editar circuito"
            title="Editar circuito"
            @click="$emit('editTrack', track)"
          >
            <Pencil :size="16" aria-hidden="true" />
          </button>
        </article>
      </div>
    </article>

    <article class="panel panel-pad-lg panel-stack-md">
      <div class="panel-copy">
        <h3 class="ui-title-card">Servicios ofrecidos por circuito</h3>
        <p class="ui-copy-muted">
          Vincula servicios activos a cada circuito y retira los que ya no deban mostrarse.
        </p>
      </div>

      <div class="admin-track-assignment-toolbar">
        <label class="surface-field">
          <span class="surface-field__label">Circuito</span>
          <select
            :value="selectedTrackId"
            @change="$emit('update:selectedTrackId', readSelectedValue($event))"
          >
            <option value="">Selecciona un circuito</option>
            <option v-for="track in sortedTracks" :key="track.id" :value="track.id">
              {{ track.name }}
            </option>
          </select>
        </label>

        <label class="surface-field">
          <span class="surface-field__label">Servicio</span>
          <select
            :value="selectedServiceId"
            :disabled="assignableServices.length === 0"
            @change="$emit('update:selectedServiceId', readSelectedValue($event))"
          >
            <option value="">Selecciona un servicio</option>
            <option
              v-for="service in assignableServices"
              :key="service.id"
              :value="service.id"
            >
              {{ service.name }}
            </option>
          </select>
        </label>

        <button
          class="action-button admin-track-assignment-toolbar__button"
          type="button"
          :disabled="assignmentSaving || !selectedTrackId || !selectedServiceId"
          @click="$emit('addAssignment')"
        >
          {{ assignmentSaving ? 'Vinculando...' : 'Añadir al circuito' }}
        </button>
      </div>

      <p v-if="selectedTrackAssignments.length === 0" class="ui-copy-muted">
        Este circuito todavía no tiene servicios vinculados.
      </p>

      <div v-else class="surface-card-list">
        <article
          v-for="assignment in selectedTrackAssignments"
          :key="assignment.id"
          class="surface-card surface-card--compact"
        >
          <div class="panel-copy surface-card__copy">
            <h4 class="ui-title-card">{{ assignment.serviceName }}</h4>
            <p class="ui-copy-muted">{{ assignment.trackName }}</p>
          </div>

          <button
            class="icon-button icon-button--danger"
            type="button"
            :disabled="assignmentDeletingId === assignment.id"
            aria-label="Eliminar servicio del circuito"
            title="Eliminar servicio del circuito"
            @click="$emit('removeAssignment', assignment)"
          >
            <Trash2 :size="16" aria-hidden="true" />
          </button>
        </article>
      </div>
    </article>
  </section>
</template>

<style scoped>
.admin-tracks__create {
  flex: none;
}

.admin-track-assignment-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: var(--space-md);
}

.admin-track-assignment-toolbar > * {
  flex: 1 1 220px;
}

.admin-track-assignment-toolbar__button {
  flex: none;
}

@media (max-width: 720px) {
  .admin-tracks__create,
  .admin-track-assignment-toolbar__button {
    width: 100%;
  }
}
</style>
