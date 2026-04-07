<script setup lang="ts">
import { RouterView } from 'vue-router'
import AuthDialog from '@/components/AuthDialog.vue'
import AppFooter from '@/components/AppFooter.vue'
import AppHeader from '@/components/AppHeader.vue'
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
