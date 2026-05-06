import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// Configuro Vite, que es quien levanta y empaqueta el front de Vue.
export default defineConfig({
  plugins: [
    // 'vue()' permite que Vite entienda archivos '.vue'.
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      // Con '@' puedo importar desde 'src' sin rutas largas tipo '../../../'.
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
