# Étape 1 : Build
FROM eclipse-temurin:21-jdk-jammy AS build

# On s'assure d'être dans le bon répertoire
WORKDIR /app

# On copie tous les fichiers (mvnw téléchargera Maven si nécessaire)
COPY . .

# On donne les droits d'exécution au Maven wrapper
RUN chmod +x mvnw


# On compile avec Maven (skip tests pour accélérer)
RUN ./mvnw clean package -DskipTests

# Étape 2 : Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10000"]