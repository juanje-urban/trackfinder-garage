<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { Bell, User } from 'lucide-vue-next'
import logoUrl from '@/assets/tfg_logo.svg'
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
      </nav>

      <div class="header-actions">
        <button class="header-action" type="button" aria-label="Notifications">
          <Bell :size="18" :stroke-width="2.2" />
        </button>

        <button class="header-action header-action--profile" type="button" aria-label="Profile">
           <User :size="18" :stroke-width="2.2" />
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

.header-action {
  width: 44px;
  height: 44px;
  border: 1px solid var(--line-faint);
  border-radius: var(--radius-sm);
  color: var(--text-strong);
  background: var(--surface-glass-strong);
  display: grid;
  place-items: center;
}


.header-action--profile {
  border-color: var(--line-accent-soft);
  background: var(--surface-light);
  color: var(--text-on-light);
  font-weight: 800;
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
}
</style>
