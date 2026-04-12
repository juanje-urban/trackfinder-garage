<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import logoUrl from '@/assets/tfg_logo.svg'
import { isAdminRole, isOrganizerRole, isUserRole } from '@/utils/authRoles'

const auth = useAuth()
const isStandardUser = computed(() => isUserRole(auth.session.value?.roleName))
const isOrganizer = computed(() => isOrganizerRole(auth.session.value?.roleName))
const isAdmin = computed(() => isAdminRole(auth.session.value?.roleName))
</script>

<template>
  <footer class="footer-shell">
    <div class="footer-shell__inner shell-container">
      <RouterLink class="brand-lockup" to="/">
        <span class="brand-lockup__mark" aria-hidden="true">
          <img :src="logoUrl" alt="" class="brand-lockup__mark-icon" />
        </span>
        <span class="brand-lockup__name">TRACK<span>FINDER</span>GARAGE</span>
      </RouterLink>

      <nav class="footer-nav" aria-label="Footer navigation">
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/tracks">Circuitos</RouterLink>
        <RouterLink to="/events">Eventos</RouterLink>
        <RouterLink v-if="isOrganizer" to="/organizer">Organizaci&oacute;n</RouterLink>
        <RouterLink v-if="isAdmin" to="/admin">Administraci&oacute;n</RouterLink>
        <RouterLink v-if="isStandardUser" to="/profile">Mi perfil</RouterLink>
        <a href="#">Politica de privacidad</a>
        <a href="#">Soporte</a>
      </nav>

      <p class="footer-copy">TFG - Juanje Urban Gonzalez - 2026</p>
    </div>
  </footer>
</template>

<style scoped>
.footer-shell {
  border-top: 1px solid var(--line-faint);
  margin-top: 56px;
}

.footer-shell__inner {
  padding: var(--space-2xl) 0 var(--space-3xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
}

.footer-nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-lg);
  color: var(--text-muted);
  font-size: var(--fs-body);
}

.footer-copy {
  margin: 0;
  color: var(--text-muted);
  font-size: var(--fs-caption);
}

@media (max-width: 900px) {
  .footer-shell__inner {
    flex-direction: column;
    align-items: start;
  }
}
</style>
