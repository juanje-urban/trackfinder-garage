<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import AuthDialog from '@/components/AuthDialog.vue'
import AppFooter from '@/components/AppFooter.vue'
import AppHeader from '@/components/AppHeader.vue'
import AppToast from '@/components/AppToast.vue'
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()

// Cuando el componente raíz aparece en pantalla, intento recuperar la sesión guardada.
onMounted(() => {
  void auth.refreshSession()
})
</script>

<template>
  <div class="app-shell">
    <!-- Estos componentes permanecen siempre en la app, cambie o no la página actual. -->
    <AppHeader />
    <AuthDialog />
    <AppToast />
    <div class="app-shell__body">
      <!-- RouterView nos renderizará la vista correspondiente a la URL. -->
      <RouterView />
    </div>
    <AppFooter />
  </div>
</template>
