import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import HomeView from '@/views/HomeView.vue'
import TracksView from '@/views/TracksView.vue'
import FutureEventsView from '@/views/FutureEventsView.vue'
import EventDetailView from '@/views/EventDetailView.vue'
import UserProfileView from '@/views/UserProfileView.vue'
import PublicUserProfileView from '@/views/PublicUserProfileView.vue'
import AdminView from '@/views/AdminView.vue'
import OrganizerView from '@/views/OrganizerView.vue'
import MessagesView from '@/views/MessagesView.vue'
import { isAdminRole, isOrganizerRole, isUserRole } from '@/utils/authRoles'

// Declaro aquí el mapa de URLs. Cada ruta apunta a una vista y algunas llevan meta
// para que el guard de abajo sepa qué permisos tiene que comprobar.
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/events', name: 'events', component: FutureEventsView },
    { path: '/events/:id', name: 'event-detail', component: EventDetailView },
    { path: '/tracks', name: 'tracks', component: TracksView },
    { path: '/messages', name: 'messages', component: MessagesView, meta: { requiresAuth: true } },
    { path: '/profile', name: 'profile', component: UserProfileView, meta: { requiresProfile: true } },
    { path: '/organizer', name: 'organizer', component: OrganizerView, meta: { requiresOrganizer: true } },
    { path: '/admin', name: 'admin', component: AdminView, meta: { requiresAdmin: true } },
    { path: '/profiles/:displayName', name: 'public-profile', component: PublicUserProfileView },
  ],
})

// Este guard se ejecuta antes de entrar en cada ruta. Lo uso como portero sencillo:
// si no hay sesión o el rol no encaja, mando al usuario a la home.
router.beforeEach((to) => {
  const auth = useAuth()
  const session = auth.session.value

  // Si estoy viendo mi propio perfil público, prefiero llevarme al perfil privado.
  if (
    to.name === 'public-profile' &&
    session &&
    isUserRole(session.roleName) &&
    session.displayName === to.params.displayName
  ) {
    return { name: 'profile' }
  }

  // Las rutas con meta no duplican lógica en cada vista, centralizo aquí los permisos.
  if (to.meta.requiresProfile && !session) {
    return { name: 'home' }
  }

  if (to.meta.requiresProfile && !isUserRole(session?.roleName) && !isOrganizerRole(session?.roleName)) {
    return { name: 'home' }
  }

  if (to.meta.requiresAuth && !session) {
    return { name: 'home' }
  }

  if (to.meta.requiresOrganizer && !isOrganizerRole(session?.roleName)) {
    return { name: 'home' }
  }

  if (to.meta.requiresAdmin && !isAdminRole(session?.roleName)) {
    return { name: 'home' }
  }

  return true
})

export default router
