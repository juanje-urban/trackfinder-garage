```text
 _______             _    ______ _           _            _____
|__   __|           | |  |  ____(_)         | |          / ____|
   | |_ __ __ _  ___| | _| |__   _ _ __   __| | ___ _ __| |  __  __ _ _ __ __ _  __ _  ___
   | | '__/ _` |/ __| |/ /  __| | | '_ \ / _` |/ _ \ '__| | |_ |/ _` | '__/ _` |/ _` |/ _ \
   | | | | (_| | (__|   <| |    | | | | | (_| |  __/ |  | |__| | (_| | | | (_| | (_| |  __/
   |_|_|  \__,_|\___|_|\_\_|    |_|_| |_|\__,_|\___|_|   \_____|\__,_|_|  \__,_|\__, |\___|
                                                                                 __/ |
                                                                                |___/
```

# TrackFinderGarage

TrackFinderGarage es una aplicación web para contratar track days. Tiene tres actores principales:

- `Usuarios`: buscan eventos, reservan plazas, anulan reservas, publican tiempos por vuelta y pueden escribir a otros usuarios.
- `Organizadores`: organizan y gestionan sus eventos y servicios y pueden consultar estadísticas.
- `Administradores`: gestionan los catálogos maestros de circuitos y servicios. Gestionan solicitudes de organizador y cuentas de usuario.

El proyecto se divide en dos aplicaciones:

- `backend`: API REST con Spring Boot.
- `frontend`: aplicación Vue 3 servida en navegador.

Repositorio Git: https://github.com/juanje-urban/trackfinder-garage

## Autor y contexto

Este proyecto ha sido desarrollado por Juan Jesús Urbán González como Trabajo de Fin de Grado (TFG) en el Grado de Ingeniería Informática en la Universitat Oberta de Catalunya en el segundo semestre del curso 2025-2026.

## Tecnologías

- Backend: Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, MariaDB, H2 y Maven.
- Frontend: Vue 3, TypeScript, Vite, Vue Router, Axios y Vitest.
- Contenedores: Docker y Docker Compose.
- Calidad: JUnit, Mockito, JaCoCo, Vitest, ESLint, Oxlint y SonarCloud.

## Estructura

```text
trackfinder-garage/
|-- backend/                       API REST, dominio, casos de uso, persistencia y datos demo
|-- frontend/                      Aplicación Vue, componentes, vistas, servicios HTTP y tests
|-- docker-compose.registry.yml    Arranque usando imágenes publicadas en GHCR
|-- docker-compose.local.yml       Arranque compilando imágenes Docker locales
`-- README.md
```

El backend sigue una organización cercana a arquitectura hexagonal:

- `domain`: modelos principales de negocio.
- `application`: servicios de aplicación, casos de uso y puertos.
- `infrastructure`: controladores web, DTO, mappers, adaptadores de persistencia, configuración y seed demo.
- `common`: excepciones y utilidades transversales.

El frontend se organiza así:

- `views`: pantallas principales renderizadas por Vue Router.
- `components`: piezas reutilizables de interfaz.
- `services`: llamadas HTTP al backend.
- `composables`: estado y lógica reutilizable, por ejemplo `useAuth` o `useToast`.
- `types`: tipos TypeScript compartidos.
- `utils`: funciones auxiliares puras.
- `assets`: imágenes, logos y trazados de circuitos.

## Arranque del software

Recomiendo estas dos formas para arrancar el proyecto completo:

- Usar las imágenes Docker publicadas en GitHub Container Registry, que es público.
- Compilar tus propias imágenes Docker en local.

En ambos modos:

- El backend arranca siempre con `SPRING_PROFILES_ACTIVE=demo`.
- El perfil `demo` recrea el esquema y carga datos de demostración.
- MariaDB se monta en `tmpfs`, sin volumen persistente.
- Si usas los comandos indicados, cada arranque empieza con la base de datos restablecida.

Servicios publicados:

| Servicio | URL |
| --- | --- |
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

### Opción 1: usar imágenes del registry

Esta opción descarga las imágenes ya publicadas. Si hubiese una corrección de última hora, esta sería la mejor opción al encontrarse actualizada:

- `ghcr.io/juanje-urban/trackfinder-garage-backend:latest`
- `ghcr.io/juanje-urban/trackfinder-garage-frontend:latest`

Arrancar desde cero:

```powershell
docker compose -f docker-compose.registry.yml down --remove-orphans
docker compose -f docker-compose.registry.yml up -d --pull always
```

Ver logs:

```powershell
docker compose -f docker-compose.registry.yml logs -f
```

Parar:

```powershell
docker compose -f docker-compose.registry.yml down --remove-orphans
```

### Opción 2: compilar imágenes Docker en local

Esta opción construye las imágenes desde el código de `backend/` y `frontend/`.

Arrancar desde cero:

```powershell
docker compose -f docker-compose.local.yml down --remove-orphans
docker compose -f docker-compose.local.yml build --no-cache
docker compose -f docker-compose.local.yml up -d
```

Ver logs:

```powershell
docker compose -f docker-compose.local.yml logs -f
```

Parar:

```powershell
docker compose -f docker-compose.local.yml down --remove-orphans
```

## Tests y calidad

El backend se valida con JUnit, Mockito, Spring Security Test y JaCoCo. El análisis de calidad se publica en SonarCloud:

- Backend en SonarCloud: https://sonarcloud.io/project/overview?id=juanje-urban_trackfinder-garage-backend

El frontend se valida con Vitest, Vue Test Utils, TypeScript, ESLint, Oxlint y cobertura en formato LCOV. El análisis de calidad se publica en SonarCloud:

- Frontend en SonarCloud: https://sonarcloud.io/project/overview?id=juanje-urban_trackfinder-garage-frontend

Los pipelines de GitHub Actions ejecutan los tests, generan cobertura y publican el análisis de ambos proyectos en SonarCloud.

## Seguridad

- La API usa HTTP Basic.
- El frontend guarda la sesión en `localStorage` y reutiliza la cabecera `Authorization` en llamadas privadas.
- Spring Security está configurado como API stateless mediante `SessionCreationPolicy.STATELESS`.
- CSRF está desactivado porque no se usan cookies de sesión ni sesión HTTP de servidor.
- CORS permite los orígenes locales esperados del frontend: `http://localhost:5173` y `http://127.0.0.1:5173`.
- Los endpoints públicos permiten consultar eventos futuros, circuitos, perfiles públicos, rankings y servicios de eventos sin iniciar sesión.

## Credenciales demo

El inicio de sesión se hace con email y contraseña.

### Administrador

| Rol | Display name | Email | Contraseña |
| --- | --- | --- | --- |
| ADMIN | `admin` | `admin@example.com` | `admin123` |

### Usuarios

Todos estos usuarios tienen contraseña `user123`.

| Rol | Display name | Email |
| --- | --- | --- |
| USER | `juanje` | `juanje@example.com` |
| USER | `maria` | `maria@example.com` |
| USER | `carlos` | `carlos@example.com` |
| USER | `fernando.alonso` | `fernando.alonso@example.com` |
| USER | `alex.palau` | `alex.palau@example.com` |
| USER | `latebraker88` | `laura.sanz@example.com` |
| USER | `curva_peraltada` | `sergio.rivas@example.com` |
| USER | `apexhunter` | `diego.mena@example.com` |
| USER | `pitlane_junkie` | `ines.duarte@example.com` |
| USER | `kerb_rider` | `marta.nogueira@example.com` |
| USER | `flatout_marta` | `marta.cabrera@example.com` |
| USER | `heeltoe_dani` | `daniel.pardo@example.com` |
| USER | `trackrat_77` | `raul.vega@example.com` |
| USER | `boxbox_raul` | `raul.ochoa@example.com` |
| USER | `redflag_ines` | `ines.pastor@example.com` |
| USER | `chicane_chaser` | `pablo.ordonez@example.com` |
| USER | `fullthrottle_eva` | `eva.silva@example.com` |
| USER | `gridwalker` | `hugo.lemos@example.com` |
| USER | `oversteer_miguel` | `miguel.costa@example.com` |
| USER | `tyresmoke_lucia` | `lucia.roman@example.com` |
| USER | `curb_attack` | `adrian.prieto@example.com` |
| USER | `brakepoint_nora` | `nora.campos@example.com` |
| USER | `paddock_paula` | `paula.freitas@example.com` |
| USER | `stintmaster` | `alvaro.nieto@example.com` |
| USER | `apex_luso` | `tiago.martins@example.com` |

### Organizadores

Todos estos organizadores tienen contraseña `org123`.

| Rol | Display name | Email | Organizador |
| --- | --- | --- | --- |
| ORGANIZER | `trackevents` | `trackevents@example.com` | TrackEvents S.L. |
| ORGANIZER | `racingpro` | `racingpro@example.com` | RacingPro S.L. |
| ORGANIZER | `iberianmotorsport` | `iberianmotorsport@example.com` | Iberian Motorsport Events S.L. |
| ORGANIZER | `tracklimits.iberia` | `tracklimits.iberia@example.com` | TrackLimits Iberia S.L. |
| ORGANIZER | `apex.iberia` | `apex.iberia@example.com` | Apex Iberia Track Days S.L. |
| ORGANIZER | `lusitania.racing` | `lusitania.racing@example.com` | Lusitania Racing Experience Lda. |
| ORGANIZER | `mediterranean.motorsport` | `mediterranean.motorsport@example.com` | Mediterranean Motorsport Club S.L. |
| ORGANIZER | `nordic.apex` | `nordic.apex@example.com` | Nordic Apex Track Days S.L. |

Nota: `nordic.apex` se crea como organizador no habilitado para probar el flujo de aprobación desde administración.

## Base de datos demo

| Dato | Valor |
| --- | --- |
| Base de datos | `trackfinder` |
| Usuario | `trackuser` |
| Contraseña | `trackpass` |
| Contraseña root | `rootpass` |

## Notas de uso

- Los assets de circuitos del frontend dependen del `shortName` del circuito.
- Si das de alta un circuito nuevo y quieres imágenes locales, el `shortName` debe coincidir con los nombres de assets esperados.
