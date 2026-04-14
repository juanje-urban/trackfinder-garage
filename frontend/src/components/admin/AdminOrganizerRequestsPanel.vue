<script setup lang="ts">
import { Check, X } from 'lucide-vue-next'
import type { Organizer } from '@/types/organizer'

defineProps<{
  organizerError: string
  pendingOrganizers: Organizer[]
  organizerBusyId: number | null
}>()

defineEmits<{
  approve: [organizer: Organizer]
  deny: [organizer: Organizer]
}>()
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="organizerError" class="status-message status-message--error">{{ organizerError }}</p>

    <article class="panel panel-pad-lg panel-stack-md">
      <div class="panel-copy">
        <h3 class="ui-title-card">Solicitudes de organizador</h3>
        <p class="ui-copy-muted">
          Revisa las altas pendientes y decide si las aceptas o las deniegas.
        </p>
      </div>

      <p v-if="pendingOrganizers.length === 0" class="ui-copy-muted">
        No hay solicitudes pendientes ahora mismo.
      </p>

      <div v-else class="surface-card-list">
        <article
          v-for="organizer in pendingOrganizers"
          :key="organizer.idUser"
          class="surface-card"
        >
          <div class="panel-copy surface-card__copy">
            <p class="ui-eyebrow">{{ organizer.legalName }}</p>
            <h4 class="ui-title-card">{{ organizer.displayName }}</h4>
            <p class="ui-copy-muted">
              {{ organizer.name }} {{ organizer.surname }} - {{ organizer.email }}
            </p>
            <p class="ui-copy-muted">CIF: {{ organizer.cif }} - Telefono: {{ organizer.phone }}</p>
          </div>

          <div class="admin-request-actions">
            <button
              class="action-button admin-request-actions__success"
              type="button"
              :disabled="organizerBusyId === organizer.idUser"
              @click="$emit('approve', organizer)"
            >
              <Check :size="16" aria-hidden="true" />
              Aceptar
            </button>

            <button
              class="action-button action-button--ghost admin-request-actions__danger"
              type="button"
              :disabled="organizerBusyId === organizer.idUser"
              @click="$emit('deny', organizer)"
            >
              <X :size="16" aria-hidden="true" />
              Denegar
            </button>
          </div>
        </article>
      </div>
    </article>
  </section>
</template>

<style scoped>
.admin-request-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-sm);
}

.admin-request-actions__success {
  min-width: 136px;
  background: linear-gradient(135deg, rgba(20, 150, 78, 0.94), rgba(11, 98, 49, 0.94));
  color: #f2fff5;
  box-shadow: 0 14px 30px rgba(11, 98, 49, 0.24);
}

.admin-request-actions__danger {
  min-width: 136px;
  background: linear-gradient(135deg, rgba(214, 31, 31, 0.94), rgba(146, 12, 12, 0.94));
  color: #fff4f4;
  box-shadow: 0 14px 30px rgba(146, 12, 12, 0.24);
}

@media (max-width: 720px) {
  .admin-request-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .admin-request-actions__success,
  .admin-request-actions__danger {
    width: 100%;
  }
}
</style>
