# Étape 1 : Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Installation de Node.js et NPM (pour que le plugin Maven ne plante pas)
RUN apt-get update && apt-get install -y nodejs npm

COPY . .
RUN chmod +x mvnw

# On compile avec Maven.
# Si tu as un profil spécifique pour le backend seul, on l'utilise.
# Sinon, on le laisse installer Node.js dans le container de build.
RUN ./mvnw clean package -DskipTests

# Étape 2 : Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
# On ne copie que le JAR final
COPY --from=build /app/target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]