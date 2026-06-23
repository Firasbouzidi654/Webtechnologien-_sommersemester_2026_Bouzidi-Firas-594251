# KinderCare Connect

KinderCare Connect is a university web project for coordinating child profiles, allergies, and medication schedules between parents and the care team. It is a prototype and must not be used with real personal or medical data.

## Features

- Parent and staff sign-in with role-based prototype access
- Child profiles with allergy information
- Medication plans with dosage, time, date, status, and recurrence: daily, weekly, weekdays only, or every X days
- Parent medication entry and calendar-based staff medication management
- Emergency map and nearby points of interest
- Light/dark themes and English/German interface text

## Technology

- Vue 3 + Vite frontend
- Java 21, Spring Boot, Spring Data JPA backend
- PostgreSQL database
- Flyway schema migrations
- Docker/Render deployment configuration

## Database tables

| Table | Purpose |
| --- | --- |
| `users` | Stores sign-in email, BCrypt password hash, and prototype role. |
| `children` | Stores registered child names and allergy information. |
| `medications` | Stores medication plans, their child link, dosage, time, schedule frequency, interval, start date, and current status. |
| `flyway_schema_history` | Flyway's migration record; required to apply schema changes safely. |

## External APIs Used

- [Nager.Date](https://date.nager.at/) for German public holidays
- [openFDA](https://open.fda.gov/apis/drug-label/) for public medication-label lookups
- [OpenStreetMap](https://www.openstreetmap.org/) tiles, displayed with Leaflet
- [Overpass API](https://overpass-api.de/) for nearby hospitals, pharmacies, and police stations
- PostgreSQL for persistent application data
- Spring Boot for the backend REST service
- Vue and Vite for the browser application
- Render for application deployment

The project does not use Google Maps or the Browser Geolocation API.

## Run locally

Prerequisites: Java 21, Node.js/npm, and PostgreSQL. Docker Desktop and Docker
Compose are the quickest way to start the local database.

```powershell
docker compose up -d db
docker compose ps
.\gradlew.bat bootRun




