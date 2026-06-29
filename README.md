# KinderCare Connect

KinderCare Connect is a university web project for coordinating child profiles, allergies, and medication schedules between parents and the care team. It is a prototype and must not be used with real personal or medical data.

## Deployed application

- Frontend: https://webtechnologien-sommersemester-2026.onrender.com/#/
- Backend: https://kindercare-backend.onrender.com
- Backend configuration check: https://kindercare-backend.onrender.com/api/ai/test
- GitHub: https://github.com/Firasbouzidi654/Webtechnologien-_sommersemester_2026_Bouzidi-Firas-594251

## Features

- Parent and staff sign-in with role-based prototype access.
- Parent dashboard for creating child profiles, managing allergies, and adding one-time medication requests by date and time.
- Staff/admin dashboard for reviewing children, managing medication tasks, updating medication status, and using a medication calendar.
- Alert center with unread badges, mark-read behavior, and temporary toast notifications.
- Emergency mode with an OpenStreetMap/Leaflet map and nearby support from Overpass API.
- OpenFDA medication label search for public medication information.
- AI Child Care Assistant and AI Parent Message Generator through the backend Groq integration.
- Light/dark theme support and responsive layouts for desktop and mobile.



## Database tables

| Table | Purpose |
| --- | --- |
| `users` | Stores sign-in email, BCrypt password hash, and prototype role. |
| `children` | Stores registered child names and allergy information. |
| `medications` | Stores medication plans, their child link, dosage, time, schedule frequency, interval, start date, and current status. |




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

 


```powershell
.\gradlew.bat test
```

Build the frontend locally:


## Render deployment

The repository includes `render.yaml`. The backend is built from the Dockerfile and the frontend is deployed as a static Vite build. Production secrets must be configured in the Render dashboard and must not be committed.

Required Render environment variables:

- `DATABASE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `VITE_API_BASE_URL`
- `GROQ_API_KEY` for AI features
- `GROQ_MODEL`, default: `llama-3.1-8b-instant`

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
