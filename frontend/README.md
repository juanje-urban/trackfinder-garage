# TrackFinder Garage Frontend

Frontend de TrackFinder Garage, construido con Vue 3, TypeScript y Vite.

Este README existe solo como guía rápida para quien entre directamente en la carpeta `frontend/`. La documentación principal del proyecto, las credenciales demo y las dos formas oficiales de arranque con Docker están en el README de la raíz.

## Stack

- Vue 3 con Composition API y `<script setup>`.
- TypeScript.
- Vite.
- Vue Router.
- Axios.
- Vitest y Vue Test Utils.
- ESLint y Oxlint.
- Nginx para servir el build Docker.

## Estructura

```text
src/
|-- App.vue          Shell principal de la aplicación
|-- main.ts          Punto de entrada de Vue
|-- router/          Rutas y guards de navegación
|-- views/           Páginas principales
|-- components/      Componentes reutilizables
|-- composables/     Estado y lógica reutilizable
|-- services/        Clientes HTTP hacia el backend
|-- types/           Tipos TypeScript compartidos
|-- utils/           Funciones auxiliares
|-- styles/          Estilos globales y tokens visuales
`-- assets/          Imágenes, logos y trazados de circuitos
```

## Configuración

El frontend necesita conocer la URL base del backend:

```text
VITE_API_BASE_URL=http://localhost:8080
```

En desarrollo se define en `.env.development`.

## Nota de uso

El arranque completo de la aplicación no se documenta aquí para evitar duplicidades. Usa el README de la raíz y elige uno de estos dos modos:

- `docker-compose.registry.yml`: usa imágenes publicadas.
- `docker-compose.local.yml`: compila imágenes Docker locales.

La imagen final del frontend usa Nginx y sirve la aplicación como SPA. La configuración está en `nginx.conf` y redirige las rutas de Vue Router hacia `index.html`.
