<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { Track } from '@/types/track'
import { createVisualStyle, trackVisualPalettes } from '@/utils/visualPalettes'

const props = defineProps<{
  track: Track
}>()

const visualStyle = computed(() => {
  return createVisualStyle(
    props.track.id,
    trackVisualPalettes,
    '--track-start',
    '--track-end',
  )
})
</script>

<template>
  <article class="track-card media-card panel">
    <div class="track-card__visual" :style="visualStyle">
      <span class="badge badge--soft">{{ track.location }}</span>
    </div>

    <div class="media-card__body">
      <h3 class="ui-title-card">{{ track.name }}</h3>
      <p class="track-card__description ui-copy-body">{{ track.description }}</p>

      <div class="media-card__footer">
        <span class="track-card__catalog ui-copy-caption">Public circuit catalog</span>
        <RouterLink class="action-button action-button--ghost" to="/events">
          Browse events
        </RouterLink>
      </div>
    </div>
  </article>
</template>

<style scoped>
.track-card__visual {
  min-height: 190px;
  padding: var(--space-lg);
  display: flex;
  align-items: start;
  background:
    linear-gradient(180deg, var(--surface-glass), var(--media-overlay-bottom-soft)),
    linear-gradient(135deg, var(--track-start), var(--track-end));
  position: relative;
}

.track-card__visual::before {
  content: "";
  position: absolute;
  inset: auto 0 0 0;
  height: 78px;
  background:
    linear-gradient(180deg, transparent, var(--media-fade-soft)),
    radial-gradient(circle at 18% 30%, var(--white-highlight), transparent 20%);
}

</style>
