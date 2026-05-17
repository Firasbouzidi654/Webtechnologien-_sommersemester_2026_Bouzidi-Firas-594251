# KinderCare Connect APIs and Services Overview

This document summarizes the APIs, external libraries, integrations, configuration files, and important services currently detected in the KinderCare Connect project.

## High-Level Architecture

- **Frontend:** Vue 3 single-page application built with Vite.
- **Backend:** Spring Boot REST API with JPA/Hibernate support and PostgreSQL/H2 database configuration.
- **Deployment style:** Docker multi-stage build compiles the frontend first, copies it into the Spring Boot static resources, then runs the backend JAR.
- **Current data flow note:** The backend exposes REST endpoints, but the current dashboards mainly use frontend mock data and a local reactive Vue store. A small frontend API wrapper exists, but it is not currently imported by the main pages.

## Frontend Libraries and Services

### Vue 3

- **Type:** Frontend library
- **Package:** `vue` (`^3.5.32`)
- **Free or paid:** Free, open source
- **Purpose:** Builds the single-page user interface with Vue components, reactivity, templates, props, events, and computed state.
- **Where it is used:**
  - `frontend/src/main.js`
  - `frontend/src/App.vue`
  - All Vue files in `frontend/src/views/` and `frontend/src/components/`
  - `frontend/src/state/kindercareStore.js`
- **Main components/pages using it:**
  - `SignInView.vue`
  - `SignupView.vue`
  - `ParentDashboard.vue`
  - `AdminDashboard.vue`
  - `NotificationCenter.vue`
  - `AuthShell.vue`
- **Import examples:**
  ```js
  import { createApp } from 'vue'
  import { reactive } from 'vue';
  ```
- **How it works in this project:** `main.js` mounts `App.vue`. `App.vue` handles route selection manually and renders the active view component. Shared app state is held in a reactive store in `kindercareStore.js`.

### Vite

- **Type:** Frontend build tool/dev server
- **Package:** `vite` (`^8.0.4`)
- **Free or paid:** Free, open source
- **Purpose:** Runs the local frontend dev server and builds production assets.
- **Where it is used:**
  - `frontend/package.json`
  - `frontend/vite.config.js`
  - `Dockerfile`
  - `backend/pom.xml` and `backend/build.gradle` indirectly through frontend build/copy steps
- **Import examples:**
  ```js
  import { defineConfig } from 'vite'
  ```
- **How it works in this project:** `npm run dev` starts Vite locally. `npm run build` outputs static files to `frontend/dist`, which are then copied into Spring Boot static resources for deployment.

### Vite Vue Plugin

- **Type:** Frontend build plugin
- **Package:** `@vitejs/plugin-vue` (`^6.0.5`)
- **Free or paid:** Free, open source
- **Purpose:** Allows Vite to compile `.vue` single-file components.
- **Where it is used:**
  - `frontend/vite.config.js`
- **Import examples:**
  ```js
  import vue from '@vitejs/plugin-vue'
  ```
- **How it works in this project:** Added to the Vite plugins array so Vue templates, scripts, and scoped styles are processed during dev and production builds.

### Leaflet

- **Type:** Frontend map library
- **Package:** `leaflet` (`^1.9.4`)
- **Free or paid:** Free, open source
- **Purpose:** Displays the emergency location map in the staff/admin dashboard.
- **Where it is used:**
  - `frontend/src/main.js`
  - `frontend/src/views/AdminDashboard.vue`
- **Main components/pages using it:**
  - `AdminDashboard.vue`
- **Import examples:**
  ```js
  import 'leaflet/dist/leaflet.css'
  import L from 'leaflet';
  ```
- **How it works in this project:** The admin dashboard creates a Leaflet map with `L.map`, adds OpenStreetMap tile layers with `L.tileLayer`, and places markers with `L.marker` for the child location and nearby emergency points.

### OpenStreetMap Tile Service

- **Type:** External map tile service
- **Service URL:** `https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png`
- **Free or paid:** Public/free with usage policy and rate expectations
- **Purpose:** Provides map tile images displayed inside Leaflet.
- **Where it is used:**
  - `frontend/src/views/AdminDashboard.vue`
- **Main components/pages using it:**
  - `AdminDashboard.vue`
- **Import/API example:**
  ```js
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  })
  ```
- **How it works in this project:** Leaflet requests map tiles from OpenStreetMap when emergency mode opens. If tile loading fails, the dashboard shows a map error message while still listing emergency POIs.

### OpenStreetMap Directions Page

- **Type:** External web navigation link
- **Service URL:** `https://www.openstreetmap.org/directions`
- **Free or paid:** Free public web service
- **Purpose:** Opens driving directions from a child/emergency location to a selected emergency POI.
- **Where it is used:**
  - `frontend/src/services/emergencyService.js`
  - `frontend/src/views/AdminDashboard.vue`
  - `frontend/src/components/EmergencyPoiCard.vue`
- **Main components/pages using it:**
  - `AdminDashboard.vue`
  - `EmergencyPoiCard.vue`
- **Import/API example:**
  ```js
  import { buildEmergencyRouteLink } from '../services/emergencyService';
  ```
- **How it works in this project:** `buildEmergencyRouteLink(from, to)` builds an OpenStreetMap directions URL using latitude/longitude pairs and opens it in a new browser tab.

### Overpass API

- **Type:** External HTTP API
- **Service URL:** `https://overpass-api.de/api/interpreter`
- **Free or paid:** Free public API with fair-use limits
- **Purpose:** Fetches nearby hospitals, pharmacies, and police stations for emergency mode.
- **Where it is used:**
  - `frontend/src/services/emergencyService.js`
  - `frontend/src/views/AdminDashboard.vue`
- **Main components/pages using it:**
  - `AdminDashboard.vue`
- **Import/API example:**
  ```js
  import { fetchNearbyEmergencyPOIs } from '../services/emergencyService';
  const response = await fetch(url, { cache: 'no-store' });
  ```
- **How it works in this project:** The service builds an Overpass query for `hospital`, `pharmacy`, and `police` amenities within a radius. Results are normalized, distance-sorted, and shown in the emergency panel and on the Leaflet map.

### Unsplash Image URLs

- **Type:** External image/CDN usage
- **Service URL:** `https://images.unsplash.com/...`
- **Free or paid:** Public image URLs; usage depends on Unsplash terms
- **Purpose:** Provides remote imagery for the auth hero and emergency POI cards.
- **Where it is used:**
  - `frontend/src/content/siteContent.js`
  - `frontend/src/views/AdminDashboard.vue`
  - `frontend/src/components/EmergencyPoiCard.vue`
- **Main components/pages using it:**
  - `AuthShell.vue` through `siteContent.js`
  - `AdminDashboard.vue`
  - `EmergencyPoiCard.vue`
- **How it works in this project:** Static remote URLs are stored in content/constants and rendered as image backgrounds or card images. No API key is used.

### Custom Frontend Store and Notification System

- **Type:** Frontend local service/state module
- **Package:** No external notification library; uses Vue reactivity
- **Free or paid:** Internal project code
- **Purpose:** Stores children, medication tasks, parent notes, verification logs, notifications, and toast messages.
- **Where it is used:**
  - `frontend/src/state/kindercareStore.js`
  - `frontend/src/views/AdminDashboard.vue`
  - `frontend/src/views/ParentDashboard.vue`
  - `frontend/src/components/NotificationCenter.vue`
- **Main components/pages using it:**
  - `AdminDashboard.vue`
  - `ParentDashboard.vue`
  - `NotificationCenter.vue`
- **Import examples:**
  ```js
  import { reactive } from 'vue';
  import { addNotification, kindercareStore } from '../state/kindercareStore';
  ```
- **How it works in this project:** `kindercareStore` is a Vue reactive object seeded from mock data. Helper functions mutate the store and create local notifications/toasts. Notifications are not push notifications and are not persisted to a backend.

### Frontend Mock API Wrapper

- **Type:** Internal frontend service module
- **Package:** No external package
- **Free or paid:** Internal project code
- **Purpose:** Provides async functions that mimic API calls using mock data.
- **Where it is used:**
  - `frontend/src/services/kindercareApi.js`
- **Current usage status:** Exported but not currently imported by the main dashboard pages.
- **Import examples:**
  ```js
  import { getChildren, getMedicationTasks } from '../services/kindercareApi';
  ```
- **How it works in this project:** Functions return cloned data from `frontend/src/data/kindercareMockData.js` and simulate simple medication status updates without making real HTTP requests.

### Browser APIs

- **Type:** Built-in browser APIs
- **Free or paid:** Free/built into browsers
- **Purpose:** Handles routing, tab opening, form interaction, and Caps Lock detection.
- **Where it is used:**
  - `frontend/src/App.vue` uses `window.location`, `window.history`, `hashchange`, and `popstate`.
  - `frontend/src/views/SignInView.vue` and `frontend/src/views/SignupView.vue` use keyboard modifier detection for Caps Lock.
  - `frontend/src/views/AdminDashboard.vue` uses `window.open` for route links.
- **How it works in this project:** The app has a manual hash/history router in `App.vue` rather than Vue Router. Auth forms use browser keyboard events for Caps Lock warnings.

## Frontend Features Without External Libraries

### Routing

- **External library:** None detected; Vue Router is not installed.
- **Where it is implemented:** `frontend/src/App.vue`
- **Routes currently handled:**
  - `/` -> sign in
  - `/login` -> sign in
  - `/signin` -> sign in
  - `/signup` -> sign up
  - `/parent` -> parent dashboard
  - `/admin` -> admin dashboard
  - `/forgot-password` -> password recovery
  - `/privacy` -> privacy page
- **How it works:** `App.vue` maps paths to component names and uses hash-based navigation for static host compatibility.

### Authentication UI

- **External authentication library:** None detected
- **Backend authentication:** None detected
- **Where it is implemented:**
  - `frontend/src/views/SignInView.vue`
  - `frontend/src/views/SignupView.vue`
  - `frontend/src/views/ForgotPasswordView.vue`
- **How it works:** Authentication pages currently validate forms and simulate loading/success/invalid states on the frontend. There is no JWT, OAuth, Firebase Auth, Spring Security, session management, or password hashing flow detected.

### QR Medication Cards

- **External QR library:** None detected
- **Where it is implemented:**
  - `frontend/src/components/QRMedicationCard.vue`
  - `frontend/src/views/ParentDashboard.vue`
  - `frontend/src/views/AdminDashboard.vue`
  - `frontend/src/data/kindercareMockData.js`
  - `frontend/src/state/kindercareStore.js`
- **How it works:** QR payload strings use the format `kindercare-connect:medication:MED-001`. The UI displays CSS-generated placeholder QR grids rather than generating scannable QR codes with a QR library.

### Charts

- **External chart library:** None detected
- **Where chart-like UI is implemented:** Dashboard counters and cards are custom Vue/CSS.

### Icons

- **External icon library:** None detected
- **Where icons are implemented:** Emoji characters, text initials, and custom CSS shapes are used across components.

### CSS Frameworks

- **External CSS framework:** None detected
- **Where styling is implemented:**
  - `frontend/src/style.css`
  - `frontend/src/styles/dashboard.css`
  - Scoped `<style>` blocks inside Vue components
- **How it works:** The UI uses custom CSS variables, scoped component styles, responsive media queries, shadows, gradients, and glassmorphism effects.

## Backend Libraries and Services

### Spring Boot

- **Type:** Backend application framework
- **Package/build files:**
  - `backend/pom.xml` uses Spring Boot parent `3.4.5`
  - `backend/build.gradle` uses Spring Boot plugin `3.3.0`
- **Free or paid:** Free, open source
- **Purpose:** Runs the backend web application and REST API.
- **Where it is used:**
  - `backend/src/main/java/de/htw_berlin/KinderCareConnect/KinderCareConnectApplication.java`
  - Controllers in `backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/`
- **Import examples:**
  ```java
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.web.bind.annotation.RestController;
  ```
- **How it works in this project:** Spring Boot starts the Java application, serves REST endpoints under `/api/**`, and serves the built Vue frontend from `src/main/resources/static`.

### Spring Web / Spring MVC

- **Type:** Backend web/API framework
- **Package:** `spring-boot-starter-web`
- **Free or paid:** Free, open source
- **Purpose:** Provides REST controllers, request mappings, JSON responses, CORS config, and static file serving.
- **Where it is used:**
  - `ChildController.java`
  - `MedicationController.java`
  - `AdminController.java`
  - `FrontendForwardController.java`
  - `CorsConfig.java`
  - `GlobalExceptionHandler.java`
- **Import examples:**
  ```java
  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;
  ```
- **How it works in this project:** Controllers return response DTOs directly as JSON. `FrontendForwardController` forwards non-file paths to `index.html` so the Vue SPA can handle routing.

### Spring Data JPA / Hibernate

- **Type:** Backend persistence library
- **Package:** `spring-boot-starter-data-jpa`
- **Free or paid:** Free, open source
- **Purpose:** Provides JPA repositories and ORM support for database entities.
- **Where it is used:**
  - `backend/src/main/java/de/htw_berlin/KinderCareConnect/persistence/entity/`
  - `backend/src/main/java/de/htw_berlin/KinderCareConnect/persistence/repository/ChildRepository.java`
  - `backend/src/main/resources/application*.properties`
- **Import examples:**
  ```java
  import jakarta.persistence.Entity;
  import org.springframework.data.jpa.repository.JpaRepository;
  ```
- **How it works in this project:** Entity classes define database tables. `ChildRepository` extends `JpaRepository`. The active REST endpoints mostly use `HealthPrototypeRepository`, which is an in-memory repository, while JPA entities/config are present for database-backed expansion.

### PostgreSQL JDBC Driver

- **Type:** Backend database driver
- **Package:** `org.postgresql:postgresql`
- **Free or paid:** Free, open source
- **Purpose:** Allows Spring Boot/JPA to connect to PostgreSQL.
- **Where it is used:**
  - `backend/pom.xml`
  - `backend/build.gradle`
  - `backend/src/main/resources/application.properties`
  - `backend/src/main/resources/application-production.properties`
  - `backend/src/main/resources/application-postgresql.properties`
- **Config examples:**
  ```properties
  spring.datasource.driver-class-name=org.postgresql.Driver
  spring.datasource.url=${DATABASE_URL}
  ```
- **How it works in this project:** The app expects database connection settings from environment variables in production and can use local PostgreSQL settings with the `postgresql` profile.

### H2 Database

- **Type:** In-memory/local test database
- **Package:** `com.h2database:h2`
- **Free or paid:** Free, open source
- **Purpose:** Supports testing and local in-memory database scenarios.
- **Where it is used:**
  - `backend/pom.xml`
  - `backend/build.gradle`
  - `backend/src/test/resources/application.properties`
- **Config examples:**
  ```properties
  spring.datasource.url=jdbc:h2:mem:kindercare-test;MODE=PostgreSQL
  spring.jpa.hibernate.ddl-auto=create-drop
  spring.flyway.enabled=false
  ```
- **How it works in this project:** Tests use an H2 in-memory database configured to behave similarly to PostgreSQL.

### Flyway

- **Type:** Database migration tool
- **Package:** Present in `backend/pom.xml`
- **Free or paid:** Free, open source
- **Purpose:** Runs versioned database migrations.
- **Where it is used:**
  - `backend/pom.xml`
  - `backend/src/main/resources/db/migration/V1__Initial_Schema.sql`
  - `backend/src/main/resources/application.properties`
  - `backend/src/main/resources/application-production.properties`
  - `backend/src/main/resources/application-postgresql.properties`
- **Config examples:**
  ```properties
  spring.flyway.enabled=true
  spring.flyway.locations=classpath:db/migration
  ```
- **How it works in this project:** Flyway runs SQL migration files from `db/migration`. The current migration creates the `children` table.

### HikariCP

- **Type:** JDBC connection pool
- **Package:** `com.zaxxer:HikariCP`
- **Free or paid:** Free, open source
- **Purpose:** Manages efficient database connections.
- **Where it is used:**
  - `backend/build.gradle`
  - `backend/src/main/java/de/htw_berlin/KinderCareConnect/config/DatabaseConfig.java`
  - `backend/src/main/resources/application-production.properties`
  - `backend/src/main/resources/application-postgresql.properties`
- **Import examples:**
  ```java
  import com.zaxxer.hikari.HikariDataSource;
  ```
- **How it works in this project:** The production/postgresql profiles create a Hikari-backed `DataSource` and configure pool size, idle timeout, connection timeout, max lifetime, and SSL mode for Render.

### JUnit / Spring Boot Test

- **Type:** Backend testing library
- **Package:** `spring-boot-starter-test`, `junit-platform-launcher`
- **Free or paid:** Free, open source
- **Purpose:** Runs backend tests.
- **Where it is used:**
  - `backend/pom.xml`
  - `backend/build.gradle`
  - `backend/src/test/java/de/htw_berlin/KinderCareConnect/KinderCareConnectApplicationTests.java`
  - `backend/src/test/resources/application.properties`
- **How it works in this project:** Provides Spring test context loading and JUnit execution.

## Backend REST API Endpoints

### Children API

- **Base path:** `/api/children`
- **Controller:** `backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/ChildController.java`
- **Service:** `HealthPrototypeService`
- **Repository:** `HealthPrototypeRepository`
- **Endpoints:**
  - `GET /api/children` returns all child health records.
  - `GET /api/children/{id}` returns one child health record by ID.
- **Response models:**
  - `ChildHealthResponse`
  - `EmergencyContactResponse`
  - `MedicationResponse`

### Medications API

- **Base path:** `/api/medications`
- **Controller:** `backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/MedicationController.java`
- **Service:** `HealthPrototypeService`
- **Repository:** `HealthPrototypeRepository`
- **Endpoints:**
  - `GET /api/medications` returns all medication records.
  - `PUT /api/medications/{medicationId}/taken` marks a medication task as taken.
- **Response models:**
  - `MedicationResponse`
  - `MedicationTaskResponse`

### Admin API

- **Base path:** `/api/admin`
- **Controller:** `backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/AdminController.java`
- **Service:** `HealthPrototypeService`
- **Repository:** `HealthPrototypeRepository`
- **Endpoints:**
  - `GET /api/admin/tasks/today` returns today's medication tasks.
  - `GET /api/admin/stats/today` returns daily pending/taken/missed counters.
- **Response models:**
  - `MedicationTaskResponse`
  - `DailyStatsResponse`

### SPA Static Forwarding

- **Controller:** `backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/FrontendForwardController.java`
- **Routes:**
  - `GET /` returns the frontend index view.
  - `GET /{path:[^\\.]*}` forwards non-file paths to `/index.html`.
- **Purpose:** Supports direct browser refreshes on frontend routes.

### Error Handling

- **Controller advice:** `backend/src/main/java/de/htw_berlin/KinderCareConnect/business/error/GlobalExceptionHandler.java`
- **Handled error:**
  - `ResourceNotFoundException` -> HTTP `404` JSON response with timestamp, status, error, and message.

## Configuration and Environment Files

### Frontend Configuration

- `frontend/package.json`
  - Defines npm scripts and frontend dependencies.
  - Scripts: `dev`, `build`, `preview`.
- `frontend/package-lock.json`
  - Locks exact npm dependency versions.
- `frontend/vite.config.js`
  - Registers the Vue plugin for Vite.
- `frontend/index.html`
  - Frontend HTML entry point.
- `frontend/src/main.js`
  - Imports global CSS, Leaflet CSS, and mounts Vue.
- `frontend/src/style.css`
  - Global design tokens and base styles.
- `frontend/src/styles/dashboard.css`
  - Dashboard-level custom CSS.

### Backend Configuration

- `backend/pom.xml`
  - Maven build configuration.
  - Used by the Docker build via `./mvnw clean package -DskipTests`.
  - Copies `frontend/dist` into backend static resources during `process-resources`.
- `backend/build.gradle`
  - Gradle build configuration.
  - Includes Spring Boot, JPA, PostgreSQL, H2, HikariCP, tests, and Node Gradle plugin.
  - Also defines a frontend build/copy path.
- `backend/src/main/resources/application.properties`
  - Default app config.
  - Uses `DATABASE_URL`.
  - Enables Flyway.
  - Sets `server.port=${PORT:10000}`.
- `backend/src/main/resources/application-production.properties`
  - Render/production PostgreSQL config.
  - Uses `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD`.
  - Enables SSL mode for PostgreSQL.
  - Configures HikariCP and Flyway.
- `backend/src/main/resources/application-postgresql.properties`
  - Local PostgreSQL profile config.
  - Uses local database URL, username, and password values.
  - Enables SQL logging for development.
- `backend/src/test/resources/application.properties`
  - Test config using H2.
- `backend/src/main/resources/db/migration/V1__Initial_Schema.sql`
  - Flyway migration creating the `children` table.
- `backend/system.properties`
  - Runtime/platform configuration file for backend hosting.
- `Dockerfile`
  - Multi-stage build for frontend and backend deployment.

### Environment Variables and Secret Locations

No frontend API keys are required for Leaflet, OpenStreetMap tiles, OpenStreetMap directions, Overpass API, or Unsplash URLs in the current implementation.

Sensitive or environment-specific values are referenced in:

- `.env.example`
  - Contains environment variable names for Render/PostgreSQL deployment.
  - Important keys: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `SPRING_PROFILES_ACTIVE`, `PORT`, `SERVER_PORT`, `FLYWAY_ENABLED`, logging variables, and `VUE_APP_API_URL`.
  - Do not expose real database credentials in documentation, screenshots, commits, or reports.
- `backend/src/main/resources/application.properties`
  - References `DATABASE_URL` and `PORT`.
- `backend/src/main/resources/application-production.properties`
  - References `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD`.
- `backend/src/main/resources/application-postgresql.properties`
  - Contains local development database values.

## Deployment and Hosting Services

### Render

- **Type:** Cloud hosting/deployment target
- **Free or paid:** Has free and paid tiers
- **Where it appears:**
  - `backend/src/main/resources/application-production.properties`
  - `backend/src/main/java/de/htw_berlin/KinderCareConnect/config/CorsConfig.java`
  - `.env.example`
  - `Dockerfile`
- **Purpose:** Hosts the Spring Boot backend/static frontend and PostgreSQL database in production.
- **How it works in this project:** The app reads Render-provided environment variables such as `PORT` and database connection settings. CORS allows Render domains and local development origins.

### Docker

- **Type:** Container build/deployment tool
- **Free or paid:** Free, open source
- **Where it is used:**
  - `Dockerfile`
- **Purpose:** Builds a deployable image containing both frontend static assets and the backend JAR.
- **How it works in this project:** Stage 1 builds the Vue frontend with Node 20. Stage 2 builds the backend with Eclipse Temurin JDK 21 and Maven. Stage 3 runs the final JAR on port `10000`.

## Not Detected in the Current Codebase

- **Firebase:** No Firebase package, config, or imports detected.
- **Spring Security:** No Spring Security dependency or auth filter chain detected.
- **JWT/OAuth/Auth0:** No JWT, OAuth, Auth0, or external authentication library detected.
- **Axios:** No Axios package/import detected; the only real HTTP call uses browser `fetch`.
- **Vue Router:** Not installed; routing is implemented manually in `App.vue`.
- **Pinia/Vuex:** Not installed; state is managed with Vue `reactive`.
- **QR generation library:** Not installed; QR visuals are CSS/mock placeholders.
- **Chart library:** No Chart.js, ECharts, Recharts, or similar package detected.
- **Icon library:** No Lucide, Font Awesome, Material Icons, or Bootstrap Icons package detected.
- **CSS framework:** No Tailwind, Bootstrap, Bulma, Vuetify, or other CSS framework detected.
- **Push notification service:** No Firebase Cloud Messaging, Web Push, or backend notification system detected. Notifications are local frontend state.

## Important File Map

- `frontend/src/App.vue` - Manual frontend route mapping and top-level view selection.
- `frontend/src/main.js` - Vue mount point and global CSS imports.
- `frontend/src/views/SignInView.vue` - Sign-in UI and simulated auth states.
- `frontend/src/views/SignupView.vue` - Registration UI and validation.
- `frontend/src/views/AdminDashboard.vue` - Staff dashboard, emergency map, medication actions, verification flow.
- `frontend/src/views/ParentDashboard.vue` - Parent dashboard and medication QR display.
- `frontend/src/components/NotificationCenter.vue` - Local notifications/toasts.
- `frontend/src/components/QRMedicationCard.vue` - Mock QR medication card.
- `frontend/src/components/EmergencyPoiCard.vue` - Emergency POI card and route link.
- `frontend/src/services/emergencyService.js` - Overpass API, OpenStreetMap route links, distance calculations.
- `frontend/src/services/kindercareApi.js` - Mock async data wrapper.
- `frontend/src/state/kindercareStore.js` - Reactive frontend store and medication/notification operations.
- `frontend/src/data/kindercareMockData.js` - Mock children, medication, emergency contact, and QR payload data.
- `backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/` - Backend REST controllers.
- `backend/src/main/java/de/htw_berlin/KinderCareConnect/business/service/HealthPrototypeService.java` - Backend service layer for current REST endpoints.
- `backend/src/main/java/de/htw_berlin/KinderCareConnect/persistence/repository/HealthPrototypeRepository.java` - In-memory backend prototype repository.
- `backend/src/main/java/de/htw_berlin/KinderCareConnect/persistence/entity/` - JPA entity definitions.
- `backend/src/main/resources/application*.properties` - Runtime, profile, database, Flyway, logging, and server config.
- `backend/src/main/resources/db/migration/` - Flyway SQL migrations.
- `Dockerfile` - Deployment image build.

## Notes for Students and Professors

- The project already has a clear frontend/backend split, but the frontend dashboards are currently mostly prototype-driven with mock data.
- The backend REST API exists and is suitable for connecting the frontend later.
- Public map services are used without API keys, which keeps setup simple for student projects.
- Database credentials should always be passed through environment variables and never shown in reports or screenshots.
- The Maven and Gradle backend build files both exist. The Dockerfile currently uses Maven, so `backend/pom.xml` is the deployment-critical backend dependency file.
