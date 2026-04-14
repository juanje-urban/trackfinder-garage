<script setup lang="ts">
import { Pencil, Power, PowerOff } from 'lucide-vue-next'
import type { ServiceCatalogItem } from '@/types/serviceCatalog'

defineProps<{
  serviceError: string
  sortedServices: ServiceCatalogItem[]
  serviceBusyId: number | null
}>()

defineEmits<{
  openCreateService: []
  editService: [service: ServiceCatalogItem]
  toggleService: [service: ServiceCatalogItem]
}>()
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="serviceError" class="status-message status-message--error">{{ serviceError }}</p>

    <article class="panel panel-pad-lg panel-stack-md">
      <div class="section-header">
        <div class="panel-copy">
          <h3 class="ui-title-card">Catalogo de servicios</h3>
          <p class="ui-copy-muted">
            Crea servicios nuevos, ajusta su alcance y activa o desactiva su disponibilidad.
          </p>
        </div>

        <button class="action-button admin-services__create" type="button" @click="$emit('openCreateService')">
          Anadir servicio
        </button>
      </div>

      <div class="surface-card-list">
        <article v-for="service in sortedServices" :key="service.id" class="surface-card">
          <div class="panel-copy surface-card__copy">
            <div class="admin-service-badges">
              <span v-if="service.allowedForTrack" class="badge badge--soft">Circuito</span>
              <span v-if="service.allowedForOrganizer" class="badge badge--soft">Organizador</span>
              <span class="badge" :class="service.enabled ? 'badge--success' : 'badge--soft'">
                {{ service.enabled ? 'Activo' : 'Inactivo' }}
              </span>
            </div>
            <h4 class="ui-title-card">{{ service.name }}</h4>
            <p class="ui-copy-muted">{{ service.description }}</p>
          </div>

          <div class="surface-card__actions">
            <button
              class="icon-button icon-button--danger"
              type="button"
              aria-label="Editar servicio"
              title="Editar servicio"
              @click="$emit('editService', service)"
            >
              <Pencil :size="16" aria-hidden="true" />
            </button>

            <button
              class="icon-button"
              :class="service.enabled ? 'icon-button--danger' : 'icon-button--success'"
              type="button"
              :disabled="serviceBusyId === service.id"
              :aria-label="service.enabled ? 'Deshabilitar servicio' : 'Habilitar servicio'"
              :title="service.enabled ? 'Deshabilitar servicio' : 'Habilitar servicio'"
              @click="$emit('toggleService', service)"
            >
              <component :is="service.enabled ? PowerOff : Power" :size="16" aria-hidden="true" />
            </button>
          </div>
        </article>
      </div>
    </article>
  </section>
</template>

<style scoped>
.admin-services__create {
  flex: none;
}

.admin-service-badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
}

@media (max-width: 720px) {
  .admin-services__create {
    width: 100%;
  }
}
</style>
