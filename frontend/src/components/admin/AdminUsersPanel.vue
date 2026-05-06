<script setup lang="ts">
import AdminUserCard from '@/components/admin/AdminUserCard.vue'
import type { Organizer } from '@/types/organizer'
import type { AdminUser } from '@/types/user'

type UserManagementTab = 'admins' | 'organizers' | 'users'
type OrganizerAdminRecord = {
  user: AdminUser
  organizer: Organizer | null
}

// Panel presentacional de usuarios. AdminView le pasa listas ya separadas por rol.
const props = defineProps<{
  userError: string
  activeUserTab: UserManagementTab
  userBusyId: number | null
  adminUsers: AdminUser[]
  organizerUsers: OrganizerAdminRecord[]
  standardUsers: AdminUser[]
  isProtectedDefaultAdmin: (user: AdminUser) => boolean
}>()

const emit = defineEmits<{
  // 'update:activeUserTab' implementa el patrón de v-model personalizado.
  'update:activeUserTab': [value: UserManagementTab]
  toggleUser: [user: AdminUser]
}>()

const userTabItems: Array<{ id: UserManagementTab; label: string }> = [
  { id: 'admins', label: 'Administradores' },
  { id: 'organizers', label: 'Organizadores' },
  { id: 'users', label: 'Usuarios' },
]

function resolveUserTabToneClass(tabId: UserManagementTab): string {
  // Devuelvo una clase por pestaña para poder colorearlas de forma distinta.
  return `admin-subtabs__tab--${tabId}`
}

function formatUserName(user: Pick<AdminUser, 'name' | 'surname'>): string {
  // Uno nombre y apellidos evitando espacios vacíos.
  return [user.name, user.surname].filter((value) => value.trim() !== '').join(' ')
}

function formatUserSummaryLine(user: Pick<AdminUser, 'email' | 'phone' | 'address'>): string {
  return [user.email, user.phone, user.address]
    .filter((value) => value.trim() !== '')
    .join(' - ')
}

function formatOrganizerSummaryLine(organizer: Organizer | null): string {
  if (organizer === null) {
    return ''
  }

  return [organizer.legalName, `CIF: ${organizer.cif}`]
    .filter((value) => value.trim() !== '')
    .join(' - ')
}

function resolveOrganizerNoteLabel(organizer: Organizer | null): string | undefined {
  if (organizer === null || organizer.organizerEnabled) {
    return undefined
  }

  return 'Solicitud de organizador pendiente de aprobación'
}
</script>

<template>
  <section class="panel-stack-lg">
    <p v-if="userError" class="status-message status-message--error">{{ userError }}</p>

    <article class="panel panel-pad-lg panel-stack-md">
      <div class="panel-copy">
        <h3 class="ui-title-card">Gestion general de usuarios</h3>
        <p class="ui-copy-muted">
          Consulta sus datos y habilita o deshabilita la cuenta cuando sea necesario.
        </p>
      </div>

      <div class="pill-tabs" role="tablist" aria-label="Tipos de cuenta">
        <button
          v-for="tab in userTabItems"
          :key="tab.id"
          class="pill-tab admin-subtabs__tab"
          :class="[resolveUserTabToneClass(tab.id), { 'pill-tab--active': activeUserTab === tab.id }]"
          type="button"
          @click="$emit('update:activeUserTab', tab.id)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-if="activeUserTab === 'admins'" class="admin-users-list">
        <p v-if="adminUsers.length === 0" class="ui-copy-muted">
          No hay administradores registrados en este momento.
        </p>

        <AdminUserCard
          v-for="user in adminUsers"
          :key="user.id"
          :display-name="user.displayName"
          :real-name="formatUserName(user)"
          :summary="formatUserSummaryLine(user)"
          :enabled="user.enabled"
          :disabled="userBusyId === user.id || isProtectedDefaultAdmin(user)"
          :protected-label="isProtectedDefaultAdmin(user) ? 'Protegido' : undefined"
          @toggle="$emit('toggleUser', user)"
        />
      </div>

      <div v-else-if="activeUserTab === 'organizers'" class="admin-users-list">
        <p v-if="organizerUsers.length === 0" class="ui-copy-muted">
          No hay organizadores registrados en este momento.
        </p>

        <AdminUserCard
          v-for="item in organizerUsers"
          :key="item.user.id"
          :display-name="item.user.displayName"
          :real-name="formatUserName(item.user)"
          :summary="formatUserSummaryLine(item.user)"
          :detail="formatOrganizerSummaryLine(item.organizer)"
          :note-label="resolveOrganizerNoteLabel(item.organizer)"
          :note-tone="item.organizer && !item.organizer.organizerEnabled ? 'warning' : undefined"
          :enabled="item.user.enabled"
          :disabled="userBusyId === item.user.id"
          @toggle="$emit('toggleUser', item.user)"
        />
      </div>

      <div v-else class="admin-users-list">
        <p v-if="standardUsers.length === 0" class="ui-copy-muted">
          No hay usuarios registrados en este momento.
        </p>

        <AdminUserCard
          v-for="user in standardUsers"
          :key="user.id"
          :display-name="user.displayName"
          :real-name="formatUserName(user)"
          :summary="formatUserSummaryLine(user)"
          :enabled="user.enabled"
          :disabled="userBusyId === user.id"
          @toggle="$emit('toggleUser', user)"
        />
      </div>
    </article>
  </section>
</template>

<style scoped>
.admin-users-list {
  display: grid;
  gap: var(--space-md);
}

.admin-subtabs__tab {
  --admin-subtab-border: rgba(255, 255, 255, 0.12);
  --admin-subtab-bg: rgba(255, 255, 255, 0.04);
  --admin-subtab-active-border: rgba(255, 91, 60, 0.28);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(62, 19, 16, 0.94), rgba(28, 10, 10, 0.96));
  border: 1px solid var(--admin-subtab-border);
  background: var(--admin-subtab-bg);
}

.admin-subtabs__tab.pill-tab--active {
  border-color: var(--admin-subtab-active-border);
  background: var(--admin-subtab-active-bg);
  box-shadow: 0 14px 28px rgba(0, 0, 0, 0.18);
}

.admin-subtabs__tab--admins {
  --admin-subtab-border: rgba(255, 90, 90, 0.24);
  --admin-subtab-bg: rgba(107, 21, 21, 0.22);
  --admin-subtab-active-border: rgba(255, 114, 114, 0.44);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(123, 26, 26, 0.96), rgba(62, 13, 13, 0.98));
}

.admin-subtabs__tab--organizers {
  --admin-subtab-border: rgba(255, 190, 92, 0.24);
  --admin-subtab-bg: rgba(100, 58, 11, 0.22);
  --admin-subtab-active-border: rgba(255, 195, 92, 0.42);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(120, 71, 13, 0.96), rgba(59, 35, 8, 0.98));
}

.admin-subtabs__tab--users {
  --admin-subtab-border: rgba(88, 188, 255, 0.24);
  --admin-subtab-bg: rgba(12, 51, 83, 0.22);
  --admin-subtab-active-border: rgba(98, 194, 255, 0.42);
  --admin-subtab-active-bg: linear-gradient(135deg, rgba(13, 81, 120, 0.96), rgba(8, 39, 59, 0.98));
}
</style>
