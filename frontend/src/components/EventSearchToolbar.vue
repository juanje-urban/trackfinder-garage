<script setup lang="ts">
// Toolbar controlada: el padre guarda los filtros y este componente solo emite cambios.
defineProps<{
  selectedStartDate: string
  selectedEndDate: string
  selectedTrackId: string
  maxBasePrice: string
  trackOptions: Array<{ id: number; name: string }>
  resultCount: number
  totalCount: number
}>()

const emit = defineEmits<{
  // Uso el patrón 'update:...' para que el padre pueda escribir '@update:campo'.
  'update:selectedStartDate': [value: string]
  'update:selectedEndDate': [value: string]
  'update:selectedTrackId': [value: string]
  'update:maxBasePrice': [value: string]
  reset: []
}>()
</script>

<template>
  <section class="panel filter-strip filter-strip--search">
    <div class="search-panel__accent" aria-hidden="true"></div>

    <div class="search-stack">
      <div class="search-row search-row--primary">
        <label class="search-field">
          <span class="search-field__label">Desde</span>
          <input
            :value="selectedStartDate"
            type="date"
            @input="emit('update:selectedStartDate', ($event.target as HTMLInputElement).value)"
          />
        </label>

        <label class="search-field">
          <span class="search-field__label">Hasta</span>
          <input
            :value="selectedEndDate"
            type="date"
            @input="emit('update:selectedEndDate', ($event.target as HTMLInputElement).value)"
          />
        </label>

        <label class="search-field">
          <span class="search-field__label">Circuito</span>
          <select
            :value="selectedTrackId"
            @change="emit('update:selectedTrackId', ($event.target as HTMLSelectElement).value)"
          >
            <option value="">Todos los circuitos</option>
            <option v-for="track in trackOptions" :key="track.id" :value="String(track.id)">
              {{ track.name }}
            </option>
          </select>
        </label>

        <label class="search-field">
          <span class="search-field__label">Precio máximo</span>
          <input
            :value="maxBasePrice"
            type="number"
            min="0"
            step="1"
            placeholder="Sin limite"
            @input="emit('update:maxBasePrice', ($event.target as HTMLInputElement).value)"
          />
        </label>

        <button
          class="action-button search-toolbar__reset"
          type="button"
          :disabled="!selectedStartDate && !selectedEndDate && !selectedTrackId && !maxBasePrice"
          @click="emit('reset')"
        >
          Limpiar filtros
        </button>
      </div>

      <div class="search-row search-row--secondary">
        <p class="subtle-note search-toolbar__note">
          Mostrando {{ resultCount }} de {{ totalCount }} eventos.
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.filter-strip--search {
  position: relative;
  gap: var(--space-2xl);
  padding: var(--space-2xl);
  overflow: hidden;
  border-color: var(--line-strong);
  background:
    linear-gradient(180deg, rgba(45, 17, 17, 0.98) 0%, rgba(25, 10, 10, 0.98) 100%);
}

.search-panel__accent {
  position: absolute;
  inset: 0 auto 0 0;
  width: 6px;
  background:
    linear-gradient(180deg, var(--accent-strong) 0%, var(--racing-amber) 55%, var(--success-text) 100%);
}

.search-stack {
  display: grid;
  gap: var(--space-xl);
}

.search-row {
  display: grid;
  gap: var(--space-md);
  align-items: end;
}

.search-row--primary {
  column-gap: var(--space-lg);
  grid-template-columns:
    minmax(140px, 0.9fr)
    minmax(140px, 0.9fr)
    minmax(220px, 1.35fr)
    minmax(170px, 0.9fr)
    minmax(228px, max-content);
}

.search-row--secondary {
  grid-template-columns: 1fr;
  align-items: center;
}

.search-field {
  display: grid;
  gap: var(--space-xs);
}

.search-field__label {
  color: var(--text-on-media-soft);
  font-size: var(--fs-caption);
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.search-field input,
.search-field select {
  min-height: 52px;
  padding: 0 var(--space-lg);
  border: 1px solid var(--line-faint);
  border-radius: 14px;
  color: var(--text-strong);
  background: rgba(255, 255, 255, 0.04);
  outline: 0;
}

.search-field select {
  appearance: none;
  background:
    linear-gradient(180deg, rgba(39, 16, 16, 0.98) 0%, rgba(24, 10, 10, 0.98) 100%);
}

.search-field select option {
  color: var(--text-strong);
  background: #1b0c0c;
}

.search-field input::placeholder {
  color: var(--text-muted);
}

.search-field input:focus,
.search-field select:focus {
  border-color: var(--line-strong);
  box-shadow: 0 0 0 3px rgba(255, 45, 32, 0.12);
}

.search-toolbar__reset {
  min-width: 184px;
  min-height: 52px;
  align-self: end;
  justify-self: end;
  padding-inline: var(--space-xl);
  background:
    linear-gradient(90deg, var(--racing-amber) 0%, var(--accent-strong) 100%);
  color: #2a0d0d;
  box-shadow: 0 14px 28px rgba(255, 128, 36, 0.24);
}

.search-toolbar__note {
  margin: 0;
  text-align: left;
}

@media (max-width: 900px) {
  .search-row--secondary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1120px) {
  .search-row--primary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .search-toolbar__reset {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .search-row--primary,
  .search-row--secondary {
    grid-template-columns: 1fr;
  }

  .search-toolbar__reset {
    width: 100%;
  }

  .search-toolbar__note {
    text-align: left;
  }
}
</style>
