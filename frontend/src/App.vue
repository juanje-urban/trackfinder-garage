<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import AuthDialog from '@/components/AuthDialog.vue'
import AppFooter from '@/components/AppFooter.vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()

onMounted(() => {
  void auth.refreshSession()
})
</script>

<template>
  <div class="app-shell">
    <AppHeader />
    <AuthDialog />
    <div class="app-shell__body">
      <RouterView />
    </div>
    <AppFooter />
  </div>
</template>
