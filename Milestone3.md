# Milestone 3 - Finaler technischer Stand

Die vollständige Projektbeschreibung steht in der [README.md](README.md).

## Architektur

- Frontend: Vue 3 mit Vite
- Backend: Spring Boot mit Spring Data JPA
- Datenbank: PostgreSQL
- Deployment: Render mit Docker-Backend und statischem Frontend

Das Frontend verwendet `VITE_API_BASE_URL`; ohne diese Variable wird lokal `http://localhost:8080` genutzt.

## Persistierte REST-Schnittstellen

| Methode | Endpunkt | Daten |
| --- | --- | --- |
| `POST` | `/api/auth/register` | E-Mail-Adresse, BCrypt-Passwort-Hash, Rolle |
| `POST` | `/api/auth/login` | Anmeldung gegen gespeicherte Konten |
| `GET`, `POST` | `/api/children` | Name und Allergien |
| `PUT`, `DELETE` | `/api/children/{id}` | Kinderprofil ändern oder löschen |
| `GET`, `POST` | `/api/medications` | Name, Kind, Dosierung, Uhrzeit und Status |
| `PUT`, `DELETE` | `/api/medications/{id}` | Medikament ändern oder löschen |

Die Backend-Domäne enthält ausschließlich `User`, `Child` und `Medication`. Die Medikamenten-Tagesansicht lädt Zeit und Status aus PostgreSQL und behält Änderungen nach einem Seiten-Refresh bei.

## Rollenprüfung

Der Header `X-User-Role` ermöglicht eine einfache Kursprojekt-Prüfung: Parents verwalten Kinder und sehen Medikamente; Staff/Admin verwalten Medikamente. Für ein produktives System wäre eine abgesicherte Authentifizierung erforderlich.

## Render

[`render.yaml`](render.yaml) baut das Backend mit dem Dockerfile und das Frontend mit `npm ci && npm run build`. Die Render-Variablen `DATABASE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` und `VITE_API_BASE_URL` werden im Render-Dashboard gesetzt.
