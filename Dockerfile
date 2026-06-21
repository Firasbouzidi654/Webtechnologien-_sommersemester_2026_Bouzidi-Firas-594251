FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app
COPY gradlew settings.gradle ./
COPY gradle gradle
COPY backend backend
RUN chmod +x gradlew && ./gradlew :backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
