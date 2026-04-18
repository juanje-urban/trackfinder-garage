<script setup lang="ts">
import { Trash2 } from 'lucide-vue-next'
import type { LapTimeFormState } from '@/types/profile'
import type { LapTime } from '@/types/lapTime'
import type { Track } from '@/types/track'
import { formatDisplayDate, formatLapTime } from '@/utils/format'

defineProps<{
  lapError: string
  lapSaving: boolean
  lapForm: LapTimeFormState
  tracks: Track[]
  sortedLapTimes: LapTime[]
}>()

defineEmits<{
  submit: []
  remove: [lapTime: LapTime]
}>()
</script>

<template>
  <section class="panel-stack-lg">
    <div class="panel-copy">
      <p class="ui-eyebrow">Vueltas</p>
      <h2 class="ui-title-section">Tus tiempos por vuelta</h2>
      <p class="ui-copy-muted">
        Registra nuevas vueltas y elimina las que ya no quieras conservar en tu historial.
      </p>
    </div>

    <p v-if="lapError" class="status-message status-message--error">{{ lapError }}</p>

    <div class="profile-grid">
      <article class="panel panel-pad-lg panel-stack-md">
        <div class="panel-copy">
          <p class="ui-eyebrow">Añadir</p>
          <h3 class="ui-title-card">Nueva vuelta</h3>
        </div>

        <form class="surface-form-grid" @submit.prevent="$emit('submit')">
          <label class="surface-field">
            <span class="surface-field__label">Circuito</span>
            <select v-model="lapForm.trackId">
              <option value="">Selecciona un circuito</option>
              <option
                v-for="trackOption in tracks"
                :key="trackOption.id"
                :value="String(trackOption.id)"
              >
                {{ trackOption.name }}
              </option>
            </select>
          </label>

          <label class="surface-field">
            <span class="surface-field__label">Fecha</span>
            <input v-model="lapForm.lapDate" type="date" />
          </label>

          <label class="surface-field">
            <span class="surface-field__label">Tiempo</span>
            <input v-model="lapForm.lapTimeText" type="text" placeholder="1:52.340" />
          </label>

          <label class="surface-field">
            <span class="surface-field__label">Coche</span>
            <input
              v-model="lapForm.vehicle"
              type="text"
              maxlength="30"
              placeholder="BMW M2"
            />
          </label>

          <button class="action-button profile-form__submit surface-field--full" type="submit" :disabled="lapSaving">
            {{ lapSaving ? 'Guardando...' : 'Registrar vuelta' }}
          </button>
        </form>
      </article>

      <article class="panel panel-pad-lg panel-stack-md">
        <div class="panel-copy">
          <p class="ui-eyebrow">Historial</p>
          <h3 class="ui-title-card">Vueltas registradas</h3>
        </div>

        <p v-if="sortedLapTimes.length === 0" class="ui-copy-muted">
          Todavía no has registrado tiempos por vuelta.
        </p>

        <div v-else class="surface-card-list">
          <article v-for="lapTime in sortedLapTimes" :key="lapTime.id" class="surface-card">
            <div class="panel-copy surface-card__copy">
              <p class="ui-eyebrow">{{ formatDisplayDate(lapTime.lapDate) }}</p>
              <strong class="profile-lap-card__time">{{ formatLapTime(lapTime.lapTimeMs) }}</strong>
              <p class="ui-copy-muted">
                {{ lapTime.trackName }}<span v-if="lapTime.vehicle"> &middot; {{ lapTime.vehicle }}</span>
              </p>
            </div>

            <button
              class="icon-button icon-button--danger"
              type="button"
              aria-label="Eliminar vuelta"
              title="Eliminar vuelta"
              @click="$emit('remove', lapTime)"
            >
              <Trash2 :size="16" aria-hidden="true" />
            </button>
          </article>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.profile-grid {
  display: grid;
  align-items: start;
  gap: var(--space-lg);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.profile-lap-card__time {
  color: var(--text-strong);
  font-size: var(--fs-title-info);
}

@media (max-width: 980px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
