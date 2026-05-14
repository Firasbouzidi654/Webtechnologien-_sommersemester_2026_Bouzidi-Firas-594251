# Étape 1 : Build du frontend
FROM node:20 AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ .
RUN npm run build

# Étape 2 : Build du backend
FROM eclipse-temurin:21-jdk-jammy AS backend-build

WORKDIR /app/backend

COPY backend/ .

# Copier le frontend built dans le backend
COPY --from=frontend-build /app/frontend/dist src/main/resources/static

# Donner les droits et build
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Étape 3 : Image finale
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]
