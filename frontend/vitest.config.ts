import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.spec.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'lcov'],
      reportsDirectory: 'coverage',
      include: [
        'src/components/admin/AdminUserCard.vue',
        'src/components/AppToast.vue',
        'src/components/ContentSection.vue',
        'src/components/DetailInfoCard.vue',
        'src/components/EventAvailabilityBadge.vue',
        'src/components/event-detail/EventServiceConfigurator.vue',
        'src/components/MetricCard.vue',
        'src/components/RankingPositionBadge.vue',
        'src/components/SectionCard.vue',
        'src/composables/useToast.ts',
        'src/utils/apiErrors.ts',
        'src/utils/authRoles.ts',
        'src/utils/date.ts',
        'src/utils/eventAvailability.ts',
        'src/utils/format.ts',
        'src/utils/identity.ts',
        'src/utils/messageFormatting.ts',
        'src/utils/messageThreads.ts',
        'src/utils/visualPalettes.ts',
      ],
      exclude: [
        'src/main.ts',
        'src/router/**',
        'src/types/**',
        'src/assets/**',
        'src/**/__tests__/**',
      ],
    },
  },
})
