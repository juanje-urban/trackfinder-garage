<script setup lang="ts">
import { Mail } from 'lucide-vue-next'
import profileHeroBackground from '@/assets/profile/profile_hero.jpg'

withDefaults(
  defineProps<{
    displayName: string
    completedEvents: number
    visitedCircuits: number
    topFiveLapTimes: number
    poleCount: number
    showEmailAction?: boolean
  }>(),
  {
    showEmailAction: false,
  },
)
</script>

<template>
  <section
    class="profile-hero panel panel-pad-xl"
    :style="{ backgroundImage: `url(${profileHeroBackground})` }"
  >
    <div class="profile-hero__identity">
      <div class="profile-hero__portrait" aria-hidden="true">
        <svg viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="profilePortraitGradient" x1="18" y1="12" x2="101" y2="108" gradientUnits="userSpaceOnUse">
              <stop stop-color="#ff5b45" />
              <stop offset="0.55" stop-color="#182535" />
              <stop offset="1" stop-color="#2f8f59" />
            </linearGradient>
          </defs>

          <circle cx="60" cy="60" r="60" fill="url(#profilePortraitGradient)" />
          <path
            d="M60 65.5C72.1503 65.5 82 55.6503 82 43.5C82 31.3497 72.1503 21.5 60 21.5C47.8497 21.5 38 31.3497 38 43.5C38 55.6503 47.8497 65.5 60 65.5Z"
            fill="rgba(255,255,255,0.86)"
          />
          <path
            d="M25 101C25 84.9837 40.2223 72 59 72H61C79.7777 72 95 84.9837 95 101V104H25V101Z"
            fill="rgba(255,255,255,0.86)"
          />
        </svg>
      </div>

      <div class="panel-copy profile-hero__alias">
        <h1 class="ui-title-hero">{{ displayName }}</h1>
        <button
          v-if="showEmailAction"
          class="profile-hero__contact-button"
          type="button"
          aria-label="Contacto por correo proximamente"
          title="Contacto por correo proximamente"
        >
          <Mail :size="18" aria-hidden="true" />
        </button>
      </div>
    </div>

    <div class="profile-hero__stats">
      <ul class="profile-stat-list">
        <li class="profile-stat-row">
          <span class="profile-stat-row__label">Eventos completados</span>
          <strong class="profile-stat-row__value">{{ completedEvents }}</strong>
        </li>
        <li class="profile-stat-row">
          <span class="profile-stat-row__label">Circuitos visitados</span>
          <strong class="profile-stat-row__value">{{ visitedCircuits }}</strong>
        </li>
        <li class="profile-stat-row">
          <span class="profile-stat-row__label">Vueltas en el top 5</span>
          <strong class="profile-stat-row__value">{{ topFiveLapTimes }}</strong>
        </li>
        <li class="profile-stat-row">
          <span class="profile-stat-row__label">Poles</span>
          <strong class="profile-stat-row__value">{{ poleCount }}</strong>
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped>
.profile-hero {
  position: relative;
  display: grid;
  grid-template-columns: minmax(250px, 0.78fr) minmax(0, 1.22fr);
  gap: var(--space-2xl);
  align-items: center;
  overflow: hidden;
  background-position: center;
  background-size: cover;
}

.profile-hero > * {
  position: relative;
  z-index: 1;
}

.profile-hero__identity {
  display: grid;
  gap: var(--space-lg);
  justify-items: start;
  align-content: start;
}

.profile-hero__portrait {
  width: clamp(132px, 17vw, 174px);
  aspect-ratio: 1;
  padding: 6px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(255, 91, 69, 0.92), rgba(54, 149, 92, 0.86));
  box-shadow: 0 18px 44px rgba(5, 10, 18, 0.36);
}

.profile-hero__portrait svg {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 999px;
  background: rgba(9, 15, 24, 0.76);
}

.profile-hero__alias :deep(.ui-title-hero) {
  color: white;
  -webkit-text-stroke: 2px rgba(0, 0, 0, 0.82);
  paint-order: stroke fill;
  text-shadow:
    0 3px 10px rgba(0, 0, 0, 0.2),
    0 0 3px rgba(0, 0, 0, 0.56);
}

.profile-hero__contact-button {
  width: 42px;
  height: 42px;
  padding: 0;
  display: inline-grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  background: rgba(8, 14, 22, 0.62);
  color: white;
  box-shadow: 0 12px 30px rgba(5, 10, 18, 0.24);
}

.profile-hero__stats {
  display: grid;
  width: min(100%, 320px);
  justify-self: end;
  align-content: start;
}

.profile-stat-list {
  margin: 0;
  padding: 0;
  list-style: none;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  background: rgba(10, 16, 26, 0.68);
  backdrop-filter: blur(10px);
  box-shadow: 0 18px 42px rgba(5, 10, 18, 0.24);
}

.profile-stat-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  padding: var(--space-lg) var(--space-xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.profile-stat-row:last-child {
  border-bottom: 0;
}

.profile-stat-row__label {
  color: rgba(255, 255, 255, 0.78);
  font-size: var(--fs-body);
  font-weight: 600;
}

.profile-stat-row__value {
  color: white;
  font-size: clamp(1.45rem, 2vw, 1.9rem);
  line-height: 1;
}

@media (max-width: 980px) {
  .profile-hero {
    grid-template-columns: 1fr;
  }

  .profile-hero__stats {
    width: 100%;
    justify-self: stretch;
  }
}

@media (max-width: 720px) {
  .profile-stat-row {
    padding: var(--space-lg);
  }
}
</style>
