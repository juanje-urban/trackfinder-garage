import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/theme.css'
import './styles/layout.css'
import './styles/patterns.css'

const app = createApp(App)

app.use(router)

app.mount('#app')
