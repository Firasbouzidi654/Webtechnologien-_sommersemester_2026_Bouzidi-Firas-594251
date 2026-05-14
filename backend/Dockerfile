# 1. Étape de construction (Build stage)
FROM gradle:8.7-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# On compile le projet en ignorant les tests pour aller plus vite
RUN ./gradlew build --no-daemon -x test

# 2. Étape d'exécution (Package stage)
FROM eclipse-temurin:21-jdk-jammy
# On récupère le fichier JAR généré à l'étape précédente
COPY --from=build /home/gradle/src/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
# Port standard pour Render
EXPOSE 10000
# Commande pour démarrer l'application
ENTRYPOINT ["java", "-jar", "/app.jar", "--server.port=10000"]