<script setup lang="ts">
import { computed } from 'vue'
import {
  getEventAvailabilityLabel,
  getEventAvailabilityState,
} from '@/utils/eventAvailability'

// Badge puramente visual. Recibe plazas restantes y decide color + texto.
const props = defineProps<{
  remainingCapacity: number
}>()

const availabilityState = computed(() =>
  getEventAvailabilityState(props.remainingCapacity),
)

const availabilityLabel = computed(() =>
  getEventAvailabilityLabel(props.remainingCapacity),
)
</script>

<template>
  <span
    class="badge event-availability-badge"
    :class="`event-availability-badge--${availabilityState}`"
  >
    {{ availabilityLabel }}
  </span>
</template>

<style scoped>
.event-availability-badge {
  --availability-surface: rgba(17, 92, 55, 0.86);
  --availability-border: rgba(122, 246, 184, 0.58);
  --availability-text: #d6ffe6;
  --availability-glow: rgba(58, 215, 134, 0.24);
  background: var(--availability-surface);
  color: var(--availability-text);
  border: 1px solid var(--availability-border);
  box-shadow: 0 12px 22px var(--availability-glow);
  backdrop-filter: blur(8px);
}

.event-availability-badge--urgent {
  --availability-surface: rgba(122, 22, 22, 0.88);
  --availability-border: rgba(255, 128, 118, 0.6);
  --availability-text: #fff0ed;
  --availability-glow: rgba(255, 45, 32, 0.3);
}

.event-availability-badge--full {
  --availability-surface: rgba(73, 20, 20, 0.94);
  --availability-border: rgba(255, 119, 119, 0.62);
  --availability-text: #fff1ef;
  --availability-glow: rgba(255, 45, 32, 0.34);
}

.event-availability-badge--limited {
  --availability-surface: rgba(104, 67, 14, 0.88);
  --availability-border: rgba(255, 205, 92, 0.56);
  --availability-text: #ffe5a5;
  --availability-glow: rgba(255, 191, 60, 0.24);
}
</style>
