FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar target/app.jar

EXPOSE 10000

CMD ["sh", "-c", "java -jar target/*.jar"]
