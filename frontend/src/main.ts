import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/theme.css'
import './styles/layout.css'
import './styles/patterns.css'

// Arranco Vue desde aquí: 'App' es el componente raíz y el router decide qué pantalla se ve dentro.
const app = createApp(App)

// Conecto Vue Router para poder usar <RouterLink> y <RouterView> en toda la aplicación.
app.use(router)

// Monto la app en el <div id="app"> que existe en index.html.
app.mount('#app')
