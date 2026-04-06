import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import TracksView from '@/views/TracksView.vue'
import FutureEventsView from '@/views/FutureEventsView.vue'
import EventDetailView from '@/views/EventDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/events', name: 'events', component: FutureEventsView },
    { path: '/events/:id', name: 'event-detail', component: EventDetailView },
    { path: '/tracks', name: 'tracks', component: TracksView },
  ],
})

export default router
