# KinderCare Connect

> Gesundheits- und Medikationsübersicht für Kindergärten

KinderCare Connect ist eine Webanwendung für Eltern und Kindergartenpersonal. Sie bündelt Kinderprofile, Allergien und Medikamentenaufgaben in einer klaren Tagesansicht.

## Hauptfunktionen

- Registrierung und Anmeldung mit den Rollen **Parent** und **Staff**
- Kinderprofile mit Name und Allergien anlegen, ändern und löschen
- Medikamente mit Kind, Dosierung, Uhrzeit und Status verwalten
- Persistente Tagesansicht mit `Upcoming`, `Pending`, `Taken` und `Missed`
- Wetter, deutsche Feiertage, öffentliche Medikamenteninformationen und eine Notfallkarte
- Heller/dunkler Modus sowie englische/deutsche Oberflächentexte

## Rollen

| Rolle | Berechtigungen |
| --- | --- |
| **Parent** | Verwaltet Kinder und Allergien und sieht die Medikamenten-Tagesansicht. |
| **Staff** | Sieht Kinder und verwaltet Medikamente einschließlich Uhrzeit und Status. |
| **Admin** | Keine eigene registrierbare Rolle; das Admin-Dashboard ist die Arbeitsansicht für **Staff**. |

Für dieses Kursprojekt prüft das Backend den vom Frontend gesendeten Header `X-User-Role`. Das ist bewusst einfach gehalten und kein Ersatz für produktive Authentifizierung.

## Persistierte Daten

| Bereich | Daten |
| --- | --- |
| Benutzerkonto | E-Mail-Adresse, BCrypt-Passwort-Hash, Rolle |
| Kind | Name, Allergien |
| Medikament | Name, Kind, Dosierung, Uhrzeit, Status |

Chronische Erkrankungen und Rezept-Dateinamen sind Oberflächen-Demonstrationen und werden nicht gespeichert. Notfallkontakte, Nachrichten und eine eigenständige Admin-Verwaltung sind nicht implementiert.

## Technologie

- Vue 3 und Vite
- Java 21, Spring Boot und Spring Data JPA
- PostgreSQL 16
- JUnit/Spring MockMvc und Vitest

## Lokal starten

Voraussetzungen: Java 21, Node.js mit npm, Docker Desktop und Docker Compose.

```powershell
docker compose up -d db
.\gradlew :backend:bootRun
```

In einem zweiten Terminal:

```powershell
cd frontend
npm install
npm run dev
```

Das Backend läuft unter `http://localhost:8080`; Vite startet standardmäßig unter `http://localhost:5173`.

## REST-Schnittstelle

| Methode | Endpunkt | Zweck |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Benutzerkonto registrieren |
| `POST` | `/api/auth/login` | Benutzer anmelden |
| `GET`, `POST` | `/api/children` | Kinder lesen bzw. anlegen |
| `PUT`, `DELETE` | `/api/children/{id}` | Kind ändern bzw. löschen |
| `GET`, `POST` | `/api/medications` | Medikamente lesen bzw. anlegen |
| `PUT`, `DELETE` | `/api/medications/{id}` | Medikament ändern bzw. löschen |

## Tests

```powershell
.\gradlew :backend:test

cd frontend
npm test
```

## Deployment auf Render

[`render.yaml`](render.yaml) definiert ein Docker-Backend und ein statisches Frontend. Auf Render müssen `DATABASE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` und `VITE_API_BASE_URL` gesetzt werden. Die Platzhalter stehen in [`.env.example`](.env.example); echte Zugangsdaten gehören nicht ins Repository.
