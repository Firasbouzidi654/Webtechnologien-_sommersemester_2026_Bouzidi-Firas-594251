# KinderCare Connect

KinderCare Connect is a university web project that supports communication between parents and kindergarten staff regarding child health and wellbeing.

The application allows parents to manage child profiles, allergies, and medication tasks for specific dates and times, while staff members can review information, update medication status, and coordinate daily care activities.

This project is a university prototype and must not be used with real personal or medical data.

---

## Live Application

- Frontend: https://webtechnologien-sommersemester-2026.onrender.com
- Backend: https://kindercare-backend.onrender.com
- Health Check: https://kindercare-backend.onrender.com/api/health
- GitHub Repository:
  https://github.com/Firasbouzidi654/Webtechnologien-_sommersemester_2026_Bouzidi-Firas-594251

---

## Features

### Authentication & Multi-User Support

- Parent and staff accounts
- Role-based access control (PARENT / STAFF)
- BCrypt password hashing
- Backend validation for user registration

### Child Management

- Create child profiles
- Manage allergies and health information
- Parent-child ownership relationships
- Emergency information management

### Medication Management

- Create medication tasks for specific dates and times
- Independent medication status:
  - Pending
  - Taken
  - Missed
  - Upcoming
- Staff dashboard for medication tracking
- Monthly medication calendar
- Daily medication statistics

### External API Integration

- German public holidays via Nager.Date
- Public medication information via OpenFDA
- Nearby emergency services using OpenStreetMap and Overpass API

### AI Features

- AI Child Care Assistant
- AI Parent Message Generator
- Backend integration using Groq (Llama 3.1 8B Instant)

### User Experience

- Responsive design
- Light and dark mode
- Mobile-friendly layouts
- Interactive dashboards

---

## Database Structure

| Table | Purpose |
|--------|----------|
| users | Stores user accounts,  password , and roles |
| children | Stores child profiles, allergies, and parent ownership |
| medications | Stores medication tasks with child references, dosage, date, time, and status |

---

## External APIs

- Nager.Date (German public holidays)
- OpenFDA (medication information)
- OpenStreetMap
- Overpass API
- Groq API

---



## Local Development

Start the local PostgreSQL database first:

```powershell
docker compose up -d db
```

Check that the database container is running:

```powershell
docker ps
```

```powershell
.\gradlew.bat :backend:bootRun
```





