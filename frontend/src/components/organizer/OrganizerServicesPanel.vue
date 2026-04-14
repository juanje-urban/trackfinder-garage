<script setup lang="ts">
import { Trash2 } from 'lucide-vue-next'
import { computed } from 'vue'
import type { OrganizerCatalogService } from '@/types/organizerWorkspace'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'

const props = defineProps<{
  serviceError: string
  availableCatalogServices: ServiceCatalogItem[]
  selectedAvailableServiceId: number | ''
  serviceSubmitting: boolean
  organizerServices: OrganizerCatalogService[]
  removingOrganizerServiceId: number | null
  isOrganizerServiceLocked: (organizerServiceId: number) => boolean
}>()

const emit = defineEmits<{
  'update:selectedAvailableServiceId': [value: number | '']
  addService: []
  removeService: [organizerServiceId: number]
}>()

const selectedServiceId = computed({
  get: () => props.selectedAvailableServiceId,
  set: (value: number | '') => emit('update:selectedAvailableServiceId', value),
})
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

    <article class="panel panel-pad-lg panel-stack-md">
      <div class="section-header">
        <div class="panel-copy">
          <h3 class="ui-title-card">Catalogo de servicios del organizador</h3>
          <p class="ui-copy-muted">
            Anade servicios globales a tu oferta y retira solo los que no esten ligados a eventos
            futuros.
          </p>
        </div>
      </div>

      <div class="organizer-toolbar">
        <label class="surface-field organizer-toolbar__field">
          <span class="surface-field__label">Servicio disponible</span>
          <select v-model="selectedServiceId">
            <option value="">
              {{
                availableCatalogServices.length === 0
                  ? 'No quedan servicios disponibles'
                  : 'Selecciona un servicio'
              }}
            </option>
            <option
              v-for="service in availableCatalogServices"
              :key="service.id"
              :value="service.id"
            >
              {{ service.name }}
            </option>
          </select>
        </label>

        <button
          class="action-button organizer-toolbar__button"
          type="button"
          :disabled="
            serviceSubmitting ||
            selectedAvailableServiceId === '' ||
            availableCatalogServices.length === 0
          "
          @click="$emit('addService')"
        >
          Anadir servicio
        </button>
      </div>

      <p v-if="availableCatalogServices.length === 0" class="ui-copy-muted">
        Ya has incorporado todos los servicios disponibles del catalogo general.
      </p>

      <p v-if="organizerServices.length === 0" class="ui-copy-muted">
        Todavia no has anadido servicios a tu catalogo.
      </p>

      <div v-else class="surface-card-list">
        <article
          v-for="service in organizerServices"
          :key="service.id"
          class="surface-card surface-card--compact"
        >
          <div class="panel-copy surface-card__copy">
            <strong class="ui-title-info">{{ service.serviceName }}</strong>
            <p v-if="isOrganizerServiceLocked(service.id)" class="ui-copy-muted">
              Vinculado a eventos futuros
            </p>
          </div>

          <button
            class="icon-button icon-button--danger"
            type="button"
            :disabled="removingOrganizerServiceId === service.id"
            aria-label="Retirar servicio del catalogo"
            title="Retirar servicio del catalogo"
            @click="$emit('removeService', service.id)"
          >
            <Trash2 :size="16" aria-hidden="true" />
          </button>
        </article>
      </div>
    </article>
  </section>
</template>

<style scoped>
.organizer-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: var(--space-md);
}

.organizer-toolbar__field {
  flex: 1 1 320px;
}

.organizer-toolbar__button {
  flex: none;
}

@media (max-width: 720px) {
  .organizer-toolbar {
    align-items: stretch;
  }

  .organizer-toolbar__button {
    width: 100%;
  }
}
</style>
