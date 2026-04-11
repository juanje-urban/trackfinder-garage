import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import HomeView from '@/views/HomeView.vue'
import TracksView from '@/views/TracksView.vue'
import FutureEventsView from '@/views/FutureEventsView.vue'
import EventDetailView from '@/views/EventDetailView.vue'
import UserProfileView from '@/views/UserProfileView.vue'
import PublicUserProfileView from '@/views/PublicUserProfileView.vue'
import AdminView from '@/views/AdminView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/events', name: 'events', component: FutureEventsView },
    { path: '/events/:id', name: 'event-detail', component: EventDetailView },
    { path: '/tracks', name: 'tracks', component: TracksView },
    { path: '/profile', name: 'profile', component: UserProfileView, meta: { requiresUser: true } },
    { path: '/admin', name: 'admin', component: AdminView, meta: { requiresAdmin: true } },
    { path: '/profiles/:displayName', name: 'public-profile', component: PublicUserProfileView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuth()

  if (
    to.name === 'public-profile' &&
    auth.session.value?.roleName === 'USER' &&
    auth.session.value.displayName === to.params.displayName
  ) {
    return { name: 'profile' }
  }

  if (to.meta.requiresUser && auth.session.value?.roleName !== 'USER') {
    return { name: 'home' }
  }

  if (to.meta.requiresAdmin && auth.session.value?.roleName !== 'ADMIN') {
    return { name: 'home' }
  }

  return true
})

export default router
