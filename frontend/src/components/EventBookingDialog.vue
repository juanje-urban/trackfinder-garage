<script setup lang="ts">
const props = defineProps<{
  isOpen: boolean
  mode: 'checkout' | 'cancel'
  trackName: string
  eventDate: string
  basePriceLabel: string
  totalPriceLabel: string
  selectedServices: Array<{
    id: number
    name: string
    priceLabel: string
  }>
  isSubmitting: boolean
  errorMessage: string
}>()

defineEmits<{
  close: []
  confirm: []
}>()
</script>

<template>
  <div
    v-if="props.isOpen"
    class="booking-dialog"
    role="dialog"
    aria-modal="true"
    :aria-label="`${props.mode === 'cancel' ? 'Anular reserva de' : 'Confirmar reserva de'} ${props.trackName}`"
    @click.self="$emit('close')"
  >
    <section class="booking-dialog__panel panel panel-pad-lg panel-stack-lg">
      <header class="booking-dialog__header">
        <div class="panel-copy">
          <p class="ui-eyebrow">
            {{ props.mode === 'cancel' ? 'Anular reserva' : 'Confirmar reserva' }}
          </p>
          <h2 class="ui-title-section">{{ props.trackName }}</h2>
          <p class="ui-copy-muted">{{ props.eventDate }}</p>
        </div>

        <button class="booking-dialog__close" type="button" @click="$emit('close')">
          Cerrar
        </button>
      </header>

      <div class="booking-dialog__lines">
        <div class="booking-dialog__line">
          <span>Entrada base</span>
          <strong>{{ props.basePriceLabel }}</strong>
        </div>

        <div
          v-for="service in props.selectedServices"
          :key="service.id"
          class="booking-dialog__line booking-dialog__line--service"
        >
          <span>{{ service.name }}</span>
          <strong>{{ service.priceLabel }}</strong>
        </div>
      </div>

      <p v-if="props.selectedServices.length === 0" class="ui-copy-muted">
        {{
          props.mode === 'cancel'
            ? 'Esta reserva no tiene servicios adicionales contratados.'
            : 'No has seleccionado servicios adicionales para esta reserva.'
        }}
      </p>

      <div class="booking-dialog__total">
        <span>{{ props.mode === 'cancel' ? 'Total contratado' : 'Total estimado' }}</span>
        <strong>{{ props.totalPriceLabel }}</strong>
      </div>

      <p v-if="props.mode === 'cancel'" class="ui-copy-muted">
        Podras anular la reserva siempre que falten al menos 14 dias para el evento.
      </p>

      <p v-if="props.errorMessage" class="status-message status-message--error">
        {{ props.errorMessage }}
      </p>

      <div class="booking-dialog__actions">
        <button class="action-button action-button--ghost" type="button" @click="$emit('close')">
          Volver
        </button>
        <button class="action-button" type="button" :disabled="props.isSubmitting" @click="$emit('confirm')">
          {{
            props.isSubmitting
              ? props.mode === 'cancel'
                ? 'Anulando...'
                : 'Confirmando...'
              : props.mode === 'cancel'
                ? 'Confirmar anulacion'
                : 'Confirmar reserva'
          }}
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.booking-dialog {
  position: fixed;
  inset: 0;
  z-index: 55;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(8, 8, 10, 0.74);
  backdrop-filter: blur(10px);
}

.booking-dialog__panel {
  width: min(640px, calc(100vw - 32px));
  max-height: calc(100vh - 32px);
  overflow: auto;
}

.booking-dialog__header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: var(--space-lg);
}

.booking-dialog__close {
  padding: 10px 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background: var(--surface-glass);
  color: var(--text-body);
}

.booking-dialog__lines {
  display: grid;
  gap: var(--space-sm);
}

.booking-dialog__line,
.booking-dialog__total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  background: var(--surface-glass);
}

.booking-dialog__line--service {
  border-color: var(--line-strong);
  background: var(--accent-soft);
}

.booking-dialog__line strong,
.booking-dialog__total strong {
  color: var(--text-strong);
}

.booking-dialog__total {
  border-color: var(--line-strong);
}

.booking-dialog__actions {
  display: flex;
  justify-content: end;
  gap: var(--space-md);
}

@media (max-width: 640px) {
  .booking-dialog {
    padding: 16px;
  }

  .booking-dialog__header,
  .booking-dialog__actions,
  .booking-dialog__line,
  .booking-dialog__total {
    flex-direction: column;
    align-items: start;
  }

  .booking-dialog__actions .action-button {
    width: 100%;
  }
}
</style>
