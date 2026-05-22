# ==============================
# Schritt 1: Frontend Build
# ==============================

# Node.js 20 wird verwendet, um das Vue.js-Frontend zu bauen
FROM node:20 AS frontend-build

# Arbeitsverzeichnis für das Frontend (cd)
WORKDIR /app/frontend

# package.json und package-lock.json kopieren
COPY frontend/package*.json ./

# optimise cahce si le code change mais pas les dépendances, il ne refait pas npm install.

# Alle Frontend-Abhängigkeiten installieren -> dépendances sn  le frontend ne peut pas être buildé.
RUN npm install   

# Gesamten Frontend-Code kopieren
COPY frontend/ .

# Vue.js/Vite Production Build erstellen
RUN npm run build


# ==============================
# Schritt 2: Backend Build
# ==============================

# Java 21 Umgebung für Spring Boot
FROM eclipse-temurin:21-jdk-jammy AS backend-build

# Arbeitsverzeichnis für das Backend
WORKDIR /app/backend

# Gesamten Backend-Code kopieren
COPY backend/ .

# Copier le frontend built dans le backend
COPY --from=frontend-build /app/frontend/dist src/main/resources/static

# Maven Wrapper ausführbar machen
RUN chmod +x mvnw

# Spring Boot Projekt bauen und JAR-Datei erzeugen
RUN ./mvnw clean package -DskipTests


# ==============================
# Schritt 3: Finale Anwendung
# ==============================

# Finale Java 21 Runtime Umgebung
FROM eclipse-temurin:21-jdk-jammy

# Arbeitsverzeichnis der finalen App
WORKDIR /app

# Fertige JAR-Datei aus dem Build kopieren
COPY --from=backend-build /app/backend/target/*.jar app.jar

# Port 10000 für Render freigeben
EXPOSE 10000

# Spring Boot Anwendung starten
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]