# Build stage: Gradle builds the frontend (via node-gradle plugin) and the backend JAR in one step
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Gradle wrapper and build scripts first (better Docker layer caching)
COPY gradlew gradlew.bat settings.gradle ./
COPY gradle/ gradle/
COPY backend/build.gradle backend/

# Copy source code
COPY backend/src/ backend/src/
COPY frontend/ frontend/

RUN chmod +x gradlew && ./gradlew :backend:bootJar --no-daemon

# Runtime stage: only the JAR, no build tools
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/backend/build/libs/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
