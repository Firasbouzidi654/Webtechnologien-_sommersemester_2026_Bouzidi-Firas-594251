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
```

In a second terminal, start the frontend:

```powershell
cd frontend
npm install
npm run dev
```

## AI Child Care Assistant

The staff dashboard includes an AI Child Care Assistant below the OpenFDA Medication Assistant. It provides concise educational support for medication questions, child symptoms, allergy concerns, and incident reports. It is not a medical service and always reminds staff to follow their local procedures, check the child record, contact parents when appropriate, and seek professional medical support when needed.

For emergency references in Berlin, the assistant is configured to use **112** for medical or fire emergencies and **110** for police emergencies. It does not recommend calling 911.

### AI Parent Message Generator

Directly below the AI Child Care Assistant, staff can use the **AI Parent Message Generator** to turn a factual childcare update into a clear, friendly, professional parent message. The generated message uses this format:

```text
Dear Parent,

[message]

Kind regards,
KinderCare Staff
```

The generator does not diagnose, give medical advice, invent missing facts, or create unnecessary alarm. Generated messages can be copied directly from the dashboard.

### AI configuration

Both AI features use the Groq API through the backend only. Create a local `.env` file or define the following environment variables before starting the backend. Never commit a real API key.

```env
GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=llama-3.1-8b-instant
```

### AI API endpoints

Both endpoints require a staff or admin role through the existing `X-User-Role` request header.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/ai/childcare-assistant` | Returns a concise, safety-focused answer for a staff question. |
| `POST` | `/api/ai/parent-message` | Generates a professional parent message from a staff description. |
| `GET` | `/api/ai/test` | Shows whether Groq is configured and the configured model; it never returns the API key. |

Example parent-message request:

```json
{
  "message": "Emma had a fever at 14:00. Parents were informed and Emma rested."
}
```

Example response:

```json
{
  "parentMessage": "Dear Parent,\n\nToday Emma experienced a mild fever at approximately 14:00...\n\nKind regards,\nKinderCare Staff"
}
```



