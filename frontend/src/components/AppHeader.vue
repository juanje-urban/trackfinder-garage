<script setup lang="ts">
import { computed, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { Bell, User } from 'lucide-vue-next'
import { useAuth } from '@/composables/useAuth'
import { useMessageInbox } from '@/composables/useMessageInbox'
import logoUrl from '@/assets/tfg_logo.svg'
import { isAdminRole, isOrganizerRole, isUserRole, normalizeRoleName } from '@/utils/authRoles'
import { getDisplayNameMonogram } from '@/utils/identity'

const auth = useAuth()
const messageInbox = useMessageInbox()

const profileMonogram = computed(() =>
  getDisplayNameMonogram(auth.session.value?.displayName ?? ''),
)

const isOrganizer = computed(() => isOrganizerRole(auth.session.value?.roleName))
const isAdmin = computed(() => isAdminRole(auth.session.value?.roleName))
const canAccessOwnProfile = computed(
  () => isUserRole(auth.session.value?.roleName) || isOrganizerRole(auth.session.value?.roleName),
)

const roleLabel = computed(() => {
  const roleName = normalizeRoleName(auth.session.value?.roleName)

  if (isAdminRole(roleName)) {
    return 'Administrador'
  }

  if (isOrganizerRole(roleName)) {
    return 'Organizador'
  }

  return ''
})

const unreadCountLabel = computed(() =>
  messageInbox.unreadCount.value > 99 ? '99+' : String(messageInbox.unreadCount.value),
)

watch(
  () => auth.session.value?.userId ?? null,
  async (userId) => {
    if (!userId) {
      messageInbox.clearUnreadCount()
      return
    }

    await messageInbox.refreshUnreadCount(userId)
  },
  { immediate: true },
)
</script>

<template>
  <header class="header-shell">
    <div class="header-shell__inner shell-container shell-container--wide">
      <RouterLink class="brand-lockup brand-lockup--header" to="/">
        <span class="brand-lockup__mark" aria-hidden="true">
          <img :src="logoUrl" alt="" class="brand-lockup__mark-icon" />
        </span>
        <span class="brand-lockup__name">TRACK<span>FINDER</span>GARAGE</span>
      </RouterLink>

      <nav class="nav-links" aria-label="Primary navigation">
        <RouterLink class="nav-link" to="/">Home</RouterLink>
        <RouterLink class="nav-link" to="/tracks">Circuitos</RouterLink>
        <RouterLink class="nav-link" to="/events">Eventos</RouterLink>
        <RouterLink v-if="auth.isAuthenticated.value" class="nav-link" to="/messages">Mensajes</RouterLink>
        <RouterLink v-if="isOrganizer" class="nav-link" to="/organizer">Organizaci&oacute;n</RouterLink>
        <RouterLink v-if="isAdmin" class="nav-link" to="/admin">Administraci&oacute;n</RouterLink>
        <RouterLink v-if="canAccessOwnProfile" class="nav-link" to="/profile">Mi perfil</RouterLink>
      </nav>

      <div class="header-actions">
        <span v-if="roleLabel" class="header-role-label">{{ roleLabel }}</span>

        <RouterLink
          v-if="auth.isAuthenticated.value"
          class="header-action header-action--notifications"
          to="/messages"
          aria-label="Mensajes"
        >
          <Bell :size="18" :stroke-width="2.2" />
          <span v-if="messageInbox.unreadCount.value > 0" class="header-action__badge">
            {{ unreadCountLabel }}
          </span>
        </RouterLink>

        <button v-else class="header-action" type="button" aria-label="Notifications">
          <Bell :size="18" :stroke-width="2.2" />
        </button>

        <button
          class="header-action header-action--profile"
          type="button"
          :aria-label="auth.isAuthenticated.value ? 'Cuenta activa' : 'Acceso de usuario'"
          @click="auth.openAuthDialog"
        >
          <span v-if="auth.isAuthenticated.value" class="header-action__monogram">
            {{ profileMonogram }}
          </span>
          <User v-else :size="18" :stroke-width="2.2" />
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header-shell {
  position: sticky;
  top: 0;
  z-index: 30;
  border-bottom: 1px solid var(--line-faint);
  background: var(--surface-header);
  backdrop-filter: blur(18px);
}

.header-shell__inner {
  min-height: 86px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: var(--space-lg);
}

.nav-links {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
}

.nav-link {
  padding: var(--space-sm) 14px;
  border-radius: var(--radius-pill);
  color: var(--text-muted);
  font-size: var(--fs-body);
  font-weight: 600;
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--text-strong);
  background: var(--surface-glass-strong);
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  justify-self: end;
}

.header-role-label {
  color: var(--text-strong);
  font-size: var(--fs-caption);
  font-weight: 700;
  letter-spacing: 0.04em;
}

.header-action {
  position: relative;
  width: 44px;
  height: 44px;
  border: 1px solid var(--line-faint);
  border-radius: var(--radius-sm);
  color: var(--text-strong);
  background: var(--surface-glass-strong);
  display: grid;
  place-items: center;
}

.header-action--notifications {
  text-decoration: none;
}

.header-action--profile {
  border-color: var(--line-accent-soft);
  background: var(--surface-light);
  color: var(--text-on-light);
  font-weight: 800;
}

.header-action__monogram {
  font-size: var(--fs-caption);
  letter-spacing: 0.08em;
}

.header-action__badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border: 2px solid rgba(20, 8, 8, 0.92);
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 74, 58, 1) 0%, rgba(220, 38, 38, 1) 100%);
  color: #fff7f5;
  display: inline-grid;
  place-items: center;
  font-size: 0.68rem;
  font-weight: 800;
  line-height: 1;
}

@media (max-width: 980px) {
  .header-shell__inner {
    grid-template-columns: auto 1fr auto;
    grid-template-areas: "brand nav actions";
    padding: var(--space-lg) 0;
  }

  .brand-lockup {
    grid-area: brand;
  }

  .nav-links {
    grid-area: nav;
    justify-self: center;
  }

  .header-actions {
    grid-area: actions;
    justify-self: end;
  }
}

@media (max-width: 640px) {
  .header-shell__inner {
    min-height: 74px;
    gap: var(--space-md);
  }

  .nav-links {
    gap: var(--space-3xs);
    justify-self: start;
    overflow-x: auto;
  }

  .nav-link {
    padding-inline: 10px;
    font-size: var(--fs-caption);
  }

  .header-role-label {
    display: none;
  }
}
</style>
