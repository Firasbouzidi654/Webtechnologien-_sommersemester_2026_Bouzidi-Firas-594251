# Étape 1 : Build
FROM gradle:8.7-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Donne la permission d'exécution au wrapper Gradle
RUN chmod +x gradlew
# Compile sans lancer les tests
RUN ./gradlew build --no-daemon -x test

# Étape 2 : Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
# Cette ligne cherche n'importe quel fichier .jar dans build/libs
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]