# Étape 1 : Build
FROM eclipse-temurin:21-jdk-jammy AS build

# Installation de Node.js et NPM (nécessaire pour ton plugin frontend-maven)
RUN apt-get update && apt-get install -y nodejs npm

WORKDIR /app

# On copie TOUT le projet (backend ET frontend) pour que Maven voit ../frontend
COPY . .

# On se déplace dans le dossier backend pour lancer Maven
WORKDIR /app/backend
RUN chmod +x mvnw

# On compile (Maven va maintenant trouver le frontend grâce au COPY précédent)
RUN ./mvnw clean package -DskipTests

# Étape 2 : Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# On récupère le JAR qui a été généré dans backend/target
COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]