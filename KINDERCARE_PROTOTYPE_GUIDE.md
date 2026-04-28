# KinderCare Connect - First Prototype Guide

## Recommended structure

```text
backend/src/main/java/de/htw_berlin/KinderCareConnect
  rest/controller        REST endpoints
  rest/model             DTO/response objects returned as JSON
  business/service       Application logic
  business/error         API error handling
  persistence/entity     JPA entity skeletons for future PostgreSQL
  persistence/repository Existing JPA repositories and current mock repository

frontend/src
  views                  Page-level screens
  components             Reusable dashboard cards and lists
  data                   Mock frontend data
  services               API/mock replacement layer
  content                Existing auth page text
```

## Main files

- `backend/.../ChildController.java`: `GET /api/children` and `GET /api/children/{id}`.
- `backend/.../MedicationController.java`: `GET /api/medications` and `PUT /api/medications/{medicationId}/taken`.
- `backend/.../AdminController.java`: admin endpoints for today's tasks and statistics.
- `backend/.../HealthPrototypeService.java`: service layer used by the new prototype endpoints.
- `backend/.../HealthPrototypeRepository.java`: in-memory mock data. This is the file to replace later with real Spring Data JPA queries.
- `backend/.../persistence/entity/*.java`: prepared entities for `User`, `Child`, `Medication`, `MedicationSchedule`, `MedicationLog`, and `EmergencyContact`.
- `frontend/src/App.vue`: simple route mapping for `/login`, `/parent`, and `/admin`.
- `frontend/src/views/LoginPage.vue`: mock login with role selection.
- `frontend/src/views/ParentDashboard.vue`: child health profile, medications, history, emergency contacts, and QR cards.
- `frontend/src/views/AdminDashboard.vue`: medication control center with filters, status cards, reminders, missed alerts, and QR verification.
- `frontend/src/components/*.vue`: reusable UI components for children, medication tasks, QR cards, emergency contacts, and medication history.
- `frontend/src/data/kindercareMockData.js`: mock frontend data that can later be replaced by API calls.
- `frontend/src/services/kindercareApi.js`: placeholder service layer for future backend integration.

## First implementation plan

1. Keep the existing project intact.
2. Add backend DTOs and in-memory mock health data.
3. Add the requested REST endpoints.
4. Add Vue dashboard pages and reusable components.
5. Keep authentication simple with role selection.
6. Verify with frontend build and backend tests.
7. Later replace mock data with PostgreSQL-backed repositories.

## Current prototype code

The initial prototype has been added directly to the repository. The most important code paths are:

```text
backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller
backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/model
backend/src/main/java/de/htw_berlin/KinderCareConnect/business/service/HealthPrototypeService.java
backend/src/main/java/de/htw_berlin/KinderCareConnect/persistence/repository/HealthPrototypeRepository.java
frontend/src/views
frontend/src/components
frontend/src/data/kindercareMockData.js
frontend/src/services/kindercareApi.js
```

## How to run and test

Backend:

```powershell
cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned"
.\gradlew.bat :backend:bootRun
```

Frontend:

```powershell
cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned\frontend"
npm install
npm run dev
```

Open:

```text
http://localhost:5173/login
```

Build/test:

```powershell
cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned\frontend"
npm run build

cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned"
.\gradlew.bat :backend:test
```

## What to push to GitHub

Push the source code and documentation:

```text
backend/src/main/java/...
frontend/src/...
GUIDE_LANCEMENT.md
KINDERCARE_PROTOTYPE_GUIDE.md
frontend/package.json
frontend/package-lock.json
backend/build.gradle
```

Do not push generated build output unless your professor explicitly asks for it:

```text
frontend/dist
backend/build
.gradle
node_modules
```

## Next development steps

1. Replace `kindercareMockData.js` calls with real `fetch()` calls to the Spring endpoints.
2. Add request DTOs for creating/updating children, allergies, diseases, medications, and emergency contacts.
3. Add real authentication and role checks.
4. Add PostgreSQL configuration and Spring Data JPA repositories.
5. Add database migrations with Flyway or Liquibase.
6. Generate real QR codes from the immutable `medicationId`.
7. Add validation and tests for medication status changes.
