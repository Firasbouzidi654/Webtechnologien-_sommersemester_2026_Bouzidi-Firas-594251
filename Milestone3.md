# Milestone 3 – Render Deployment

## 1. Projektübersicht

Das Projekt **KinderCare Connect** ist als Webanwendung aufgebaut. Es enthält ein Frontend mit **Vue.js** und **Vite** im Ordner `frontend/` sowie ein Backend mit **Spring Boot Java** im Ordner `backend/`.

Frontend und Backend sind als getrennte Bereiche im Projekt vorhanden. Für Render ist wichtig, dass das Backend als Java/Spring-Boot-Anwendung läuft und das Frontend mit Vite gebaut werden kann. In den aktuellen Projektdateien ist außerdem ein Dockerfile vorhanden, das Frontend und Backend zusammen bauen kann.

## 2. Backend Deployment

Für das Backend wurden die folgenden Dateien geprüft:

* `backend/render.yaml`: Diese Datei ist im aktuellen Projekt nicht vorhanden. Deshalb kann aus dem Repository kein Render-YAML mit `rootDir`, Build Command oder Start Command direkt nachgewiesen werden.
* `backend/pom.xml`: Diese Datei definiert das Spring-Boot-Backend mit Maven. Das Projekt nutzt Java 21, Spring Boot Web, Spring Data JPA, PostgreSQL, H2 für Laufzeit/Testumgebungen und Flyway.
* `backend/src/main/resources/application.properties`: Diese Datei konfiguriert den Server-Port, PostgreSQL, JPA und Flyway.

Das Backend verwendet als Runtime **Java 21**. Das wird in `backend/pom.xml` über `<java.version>21</java.version>` und zusätzlich in `backend/system.properties` mit `java.runtime.version=21` festgelegt.

Der Maven Build wird über das Backend-Projekt ausgeführt. In `backend/pom.xml` ist das `spring-boot-maven-plugin` vorhanden. Außerdem gibt es einen `maven-resources-plugin`, der den gebauten Frontend-Ordner `frontend/dist` nach `backend/src/main/resources/static` kopieren kann.

Ein direkter Start Command aus `backend/render.yaml` ist nicht vorhanden, weil diese Datei fehlt. Im vorhandenen `Dockerfile` wird die fertige JAR-Datei mit folgendem Befehl gestartet:

```bash
java -jar app.jar --server.port=10000
```

Render verwendet normalerweise die Umgebungsvariable `PORT`. In `backend/src/main/resources/application.properties` steht:

```properties
server.port=${PORT:10000}
server.address=0.0.0.0
```

Damit hört die Anwendung auf den von Render gesetzten Port. Falls `PORT` nicht gesetzt ist, wird Port `10000` verwendet.

Die PostgreSQL-Verbindung wird über Umgebungsvariablen konfiguriert:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
```

In `backend/src/main/resources/application-production.properties` werden zusätzlich diese Variablen genutzt:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER:}
spring.datasource.password=${DATABASE_PASSWORD:}
```

Damit kann das Backend auf eine PostgreSQL-Datenbank von Render zugreifen. Flyway ist ebenfalls aktiviert:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

Eine Migration ist vorhanden unter `backend/src/main/resources/db/migration/V1__Initial_Schema.sql`. Diese Migration erstellt die Tabelle `children`.

### Backend API Link

Die echte Backend-GET-Route im Code ist:

```text
GET /api/children
```

Der vollständige Render-Backend-Link ist im aktuellen Repository nicht eindeutig hinterlegt, weil `backend/render.yaml` und eine produktive `.env`-Datei fehlen. Der einzige konkrete Render-Link im Code ist die erlaubte Frontend-Origin:

```text
https://kindercare-connect.onrender.com
```

Wenn das Backend unter diesem Render-Host erreichbar wäre, wäre der vollständige API-Link:

```text
https://kindercare-connect.onrender.com/api/children
```

Beim Test der URL wurde jedoch `404 Not Found` zurückgegeben. Deshalb ist als sicher nachgewiesene Backend-Route im Projekt nur der Pfad `/api/children` dokumentiert.

## 3. Frontend Deployment

Für das Frontend wurden die folgenden Dateien geprüft:

* `frontend/package.json`
* `frontend/vite.config.js`
* `.env`-Dateien im Ordner `frontend/`

In `frontend/package.json` ist Vue.js als Dependency enthalten. Das Frontend wird mit Vite gebaut. Der Build Command lautet:

```bash
npm run build
```

Dieser Command führt intern aus:

```bash
vite build
```

Die Datei `frontend/vite.config.js` nutzt das Vue Plugin:

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
})
```

Im aktuellen Projekt sind keine produktiven `.env`-Dateien im Ordner `frontend/` vorhanden. Der Frontend-Code kann aber eine Backend-URL über die Vite-Variable `VITE_API_BASE_URL` lesen. Das steht in `frontend/src/services/weatherService.js`:

```js
if (import.meta.env.VITE_API_BASE_URL) {
  return import.meta.env.VITE_API_BASE_URL;
}
```

Wenn diese Variable auf Render gesetzt wird, kennt das Vue-Frontend die URL des Backends. Wenn sie nicht gesetzt ist, verwendet der Code einen leeren Basis-Pfad und würde relative API-Routen verwenden.

## 4. Frontend Calls Backend API

Die API-Aufrufe im Frontend wurden im Ordner `frontend/src/` gesucht.

Der konkrete Fetch-Aufruf zu einer Backend-Route ist in dieser Datei vorbereitet:

```text
frontend/src/services/weatherService.js
```

Dort wird folgende Backend-Route aufgebaut:

```js
return fetchJson(`${apiBaseUrl()}/api/weather/health?${params.toString()}`);
```

Diese Route passt zum Spring-Boot-Controller:

```text
backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/WeatherController.java
```

Der Controller definiert:

```text
GET /api/weather/health?lat=...&lon=...
```

Die Daten werden in der Komponente `frontend/src/components/WeatherHealthCard.vue` angezeigt. Diese Komponente ruft `loadWeatherHealthData()` auf und zeigt danach Temperatur, Wetterbeschreibung, Standort, Luftfeuchtigkeit, Wind, Regenwahrscheinlichkeit und Health Alerts in der Oberfläche an.

Wichtig: Im aktuellen Code wird `fetchWeatherFromBackend()` zwar vorbereitet, aber in `loadWeatherHealthData()` nicht aktiv verwendet. Die aktuelle Wetteranzeige nutzt direkt OpenWeather oder Open-Meteo. Die Kinder- und Medikationsdaten im Frontend kommen aktuell aus Mock-Daten bzw. dem lokalen Vue Store, nicht aus einem aktiven `fetch` zu `/api/children`.

Die Backend-Route für Kinder ist trotzdem vorhanden:

```text
GET /api/children
```

Sie ist in `backend/src/main/java/de/htw_berlin/KinderCareConnect/rest/controller/ChildController.java` definiert.

## 5. Render Deployment Structure

```text
User Browser
↓
Frontend (Vue.js on Render)
↓ HTTP GET
Backend API (Spring Boot on Render)
↓
PostgreSQL Database
```

## 6. Conclusion

Das Projekt enthält ein Vue.js-Frontend und ein Spring-Boot-Backend. Das Backend ist für Render mit Java 21, Maven, PostgreSQL-Umgebungsvariablen und Flyway vorbereitet.

Die Backend-Route `GET /api/children` ist im Code vorhanden. Außerdem ist im Frontend ein GET-Aufruf zu `GET /api/weather/health` vorbereitet. Die aktuelle Oberfläche zeigt Wetterdaten in `WeatherHealthCard.vue` an, verwendet dafür aber im Moment direkte externe Wetterdienste und nicht aktiv die Backend-Funktion.

Damit ist das Projekt grundsätzlich für ein gemeinsames Render-Deployment von Frontend, Backend und PostgreSQL vorbereitet. Für eine vollständig nachweisbare getrennte Render-Dokumentation fehlen im aktuellen Repository jedoch `backend/render.yaml` und eine konkrete produktive Backend-URL.
