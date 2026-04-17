<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { Track } from '@/types/track'
import { getTrackMedia } from '@/utils/trackMedia'
import { createVisualStyle, trackVisualPalettes } from '@/utils/visualPalettes'

const props = defineProps<{
  track: Track
}>()

const trackMedia = computed(() => getTrackMedia(props.track.name))

const visualStyle = computed(() => {
  if (trackMedia.value.coverImage) {
    return {
      backgroundImage: `linear-gradient(180deg, rgba(10, 12, 16, 0.2) 0%, rgba(10, 12, 16, 0.78) 100%), url(${trackMedia.value.coverImage})`,
      backgroundPosition: 'center',
      backgroundSize: 'cover',
    }
  }

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
        <RouterLink
          class="action-button action-button--ghost"
          :to="{ path: '/events', query: { trackId: String(track.id) } }"
        >
          Ver eventos
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
