# Étape 1 : Build
FROM maven:3.9.4-openjdk-21 AS build

# On s'assure d'être dans le bon répertoire
WORKDIR /app

# On copie d'abord le pom.xml pour optimiser le cache Docker
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# On donne les droits d'exécution au Maven wrapper
RUN chmod +x mvnw

# On copie le code source
COPY src src

# On compile avec Maven (skip tests pour accélérer)
RUN ./mvnw clean package -DskipTests

# Étape 2 : Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]