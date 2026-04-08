<script setup lang="ts">
import CatalogToolbar from '@/components/CatalogToolbar.vue'
import ContentSection from '@/components/ContentSection.vue'
import HeroInfoPanel from '@/components/HeroInfoPanel.vue'
import PageHero from '@/components/PageHero.vue'

withDefaults(
  defineProps<{
    heroEyebrow: string
    heroTitle: string
    heroDescription: string
    infoLabel: string
    infoCaption: string
    toolbarChips?: Array<{ label: string; accent?: boolean }>
    toolbarNote?: string
    sectionEyebrow?: string
    sectionTitle?: string
    sectionHint?: string
    loading: boolean
    loadingMessage: string
    error: string
    empty: boolean
    emptyMessage: string
  }>(),
  {
    toolbarChips: () => [],
    toolbarNote: '',
    sectionEyebrow: '',
    sectionTitle: '',
    sectionHint: '',
  },
)
</script>

<template>
  <main class="page-shell section-stack">
    <PageHero
      :eyebrow="heroEyebrow"
      :title="heroTitle"
      :description="heroDescription"
    >
      <template #aside>
        <HeroInfoPanel :label="infoLabel" :caption="infoCaption">
          <slot name="hero-value" />
        </HeroInfoPanel>
      </template>
    </PageHero>

    <section v-if="$slots.metrics" class="metrics-grid">
      <slot name="metrics" />
    </section>

    <slot name="summary" />

    <slot name="toolbar">
      <CatalogToolbar :chips="toolbarChips" :note="toolbarNote" />
    </slot>

    <ContentSection
      :eyebrow="sectionEyebrow"
      :title="sectionTitle"
      :hint="sectionHint"
      :loading="loading"
      :loading-message="loadingMessage"
      :error="error"
      :empty="empty"
      :empty-message="emptyMessage"
    >
      <slot />
    </ContentSection>
  </main>
</template>
