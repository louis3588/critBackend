FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine
COPY --from=build /app/target/criticaBackend-0.0.1-SNAPSHOT.jar /app.jar
EXPOSE 5051
ENTRYPOINT ["java", "-jar", "/app.jar"]
