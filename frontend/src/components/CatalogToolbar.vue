<script setup lang="ts">
defineProps<{
  chips: Array<{ label: string; accent?: boolean }>
  note: string
}>()
</script>

<template>
  <section class="panel filter-strip">
    <div class="filter-chips">
      <span
        v-for="chip in chips"
        :key="chip.label"
        class="filter-chip"
        :class="{ 'filter-chip--accent': chip.accent }"
      >
        {{ chip.label }}
      </span>
    </div>

    <span class="subtle-note">{{ note }}</span>
  </section>
</template>
