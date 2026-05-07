<script setup lang="ts">
import { computed } from 'vue'
import { X } from 'lucide-vue-next'

// Este componente es "tonto". Recibe datos por props y avisa al padre con eventos.
const props = defineProps<{
  isOpen: boolean
  mode: 'checkout' | 'cancel'
  trackName: string
  eventDate: string
  canConfirm: boolean
  basePriceLabel: string
  totalPriceLabel: string
  selectedServices: Array<{
    id: number
    name: string
    priceLabel: string
  }>
  isSubmitting: boolean
  errorMessage: string
  isVisibleOnPublicProfile: boolean
}>()

defineEmits<{
  // Usamos emits para notificar eventos al padre.
  close: []
  confirm: []
  'update:isVisibleOnPublicProfile': [value: boolean]
}>()

const confirmButtonLabel = computed(() => {
  if (props.isSubmitting) {
    return props.mode === 'cancel' ? 'Anulando...' : 'Confirmando...'
  }

  return props.mode === 'cancel' ? 'Confirmar anulación' : 'Confirmar reserva'
})
</script>

<template>
  <dialog
    v-if="props.isOpen"
    open
    class="booking-dialog"
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

        <button
          class="booking-dialog__close"
          type="button"
          aria-label="Cerrar"
          title="Cerrar"
          @click="$emit('close')"
        >
          <X :size="18" aria-hidden="true" />
        </button>
      </header>

      <div v-if="props.mode === 'checkout'" class="booking-dialog__lines">
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

      <div v-else class="booking-dialog__lines">
        <div
          v-for="service in props.selectedServices"
          :key="service.id"
          class="booking-dialog__line booking-dialog__line--service"
        >
          <span>{{ service.name }}</span>
        </div>
      </div>

      <p v-if="props.selectedServices.length === 0" class="ui-copy-muted">
        {{
          props.mode === 'cancel'
            ? 'Esta reserva no tiene servicios adicionales contratados.'
            : 'No has seleccionado servicios adicionales para esta reserva.'
        }}
      </p>

      <div v-if="props.mode === 'checkout'" class="booking-dialog__total">
        <span>Total estimado</span>
        <strong>{{ props.totalPriceLabel }}</strong>
      </div>

      <label v-if="props.mode === 'checkout'" class="booking-dialog__visibility">
        <input
          class="booking-dialog__visibility-checkbox"
          type="checkbox"
          :checked="props.isVisibleOnPublicProfile"
          @change="
            $emit(
              'update:isVisibleOnPublicProfile',
              ($event.target as HTMLInputElement).checked,
            )
          "
        />
        <span class="booking-dialog__visibility-copy">
          ¿Deseas que este evento aparezca en tu perfil
          <strong class="booking-dialog__visibility-word">P&Uacute;BLICO</strong>?
        </span>
      </label>

      <p v-if="props.mode === 'cancel'" class="ui-copy-muted">
        {{
          props.canConfirm
            ? 'Podrás anular la reserva siempre que falten al menos 14 días para el evento.'
            : 'La anulación ya no está disponible porque faltan menos de 14 días para el evento.'
        }}
      </p>

      <p v-if="props.errorMessage" class="status-message status-message--error">
        {{ props.errorMessage }}
      </p>

      <div class="booking-dialog__actions">
        <button class="action-button action-button--ghost" type="button" @click="$emit('close')">
          Volver
        </button>
        <button
          class="action-button"
          type="button"
          :disabled="props.isSubmitting || !props.canConfirm"
          @click="$emit('confirm')"
        >
          {{ confirmButtonLabel }}
        </button>
      </div>
    </section>
  </dialog>
</template>

<style scoped>
.booking-dialog {
  position: fixed;
  inset: 0;
  z-index: 55;
  width: auto;
  height: auto;
  max-width: none;
  max-height: none;
  margin: 0;
  border: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(8, 8, 10, 0.74);
  color: inherit;
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
  width: 40px;
  height: 40px;
  border: 1px solid rgba(255, 114, 114, 0.5);
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(214, 31, 31, 0.94), rgba(146, 12, 12, 0.94));
  color: #fff4f4;
  box-shadow: 0 14px 30px rgba(146, 12, 12, 0.24);
  display: grid;
  place-items: center;
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

.booking-dialog__visibility {
  display: flex;
  align-items: start;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  background: var(--surface-glass-subtle);
}

.booking-dialog__visibility-checkbox {
  margin-top: 3px;
  accent-color: var(--success-text);
}

.booking-dialog__visibility-copy {
  color: var(--text-body);
  line-height: 1.5;
}

.booking-dialog__visibility-word {
  color: var(--success-text);
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
  .booking-dialog__total,
  .booking-dialog__visibility {
    flex-direction: column;
    align-items: start;
  }

  .booking-dialog__actions .action-button {
    width: 100%;
  }
}
</style>
