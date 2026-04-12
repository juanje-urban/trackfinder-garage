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
import { isAdminRole, isOrganizerRole, isUserRole } from '@/utils/authRoles'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/events', name: 'events', component: FutureEventsView },
    { path: '/events/:id', name: 'event-detail', component: EventDetailView },
    { path: '/tracks', name: 'tracks', component: TracksView },
    { path: '/profile', name: 'profile', component: UserProfileView, meta: { requiresUser: true } },
    { path: '/organizer', name: 'organizer', component: OrganizerView, meta: { requiresOrganizer: true } },
    { path: '/admin', name: 'admin', component: AdminView, meta: { requiresAdmin: true } },
    { path: '/profiles/:displayName', name: 'public-profile', component: PublicUserProfileView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuth()
  const session = auth.session.value

  if (
    to.name === 'public-profile' &&
    session &&
    isUserRole(session.roleName) &&
    session.displayName === to.params.displayName
  ) {
    return { name: 'profile' }
  }

  if (to.meta.requiresUser && !isUserRole(session?.roleName)) {
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
