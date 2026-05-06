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
        'src/components/AppToast.vue',
        'src/components/EventAvailabilityBadge.vue',
        'src/components/RankingPositionBadge.vue',
        'src/composables/useToast.ts',
        'src/utils/authRoles.ts',
        'src/utils/date.ts',
        'src/utils/eventAvailability.ts',
        'src/utils/format.ts',
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
