# Étape 1 : Build
FROM gradle:8.7-jdk21 AS build

# On s'assure d'être dans le bon répertoire
WORKDIR /home/gradle/src

# On copie d'abord uniquement les fichiers de configuration Gradle
# Cela aide Docker à mieux voir les fichiers
COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle build.gradle .
COPY --chown=gradle:gradle settings.gradle .

# On copie ensuite le reste du code source
COPY --chown=gradle:gradle . .

# On donne les droits et on compile
RUN chmod +x gradlew
RUN ./gradlew build --no-daemon -x test

# Étape 2 : Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]