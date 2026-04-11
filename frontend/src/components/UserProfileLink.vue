<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const props = defineProps<{
  displayName: string
}>()

const auth = useAuth()

const profileTarget = computed(() => {
  if (
    auth.session.value?.roleName === 'USER' &&
    auth.session.value.displayName === props.displayName
  ) {
    return '/profile'
  }

  return `/profiles/${encodeURIComponent(props.displayName)}`
})
</script>

<template>
  <RouterLink :to="profileTarget" class="user-profile-link">
    <slot>{{ displayName }}</slot>
  </RouterLink>
</template>

<style scoped>
.user-profile-link {
  color: inherit;
  text-decoration: none;
  transition:
    color 160ms ease,
    opacity 160ms ease;
}

.user-profile-link:hover,
.user-profile-link:focus-visible {
  color: var(--accent-strong);
}
</style>
