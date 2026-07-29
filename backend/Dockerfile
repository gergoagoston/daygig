FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle build.gradle ./

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew bootJar --no-daemon

FROM gcr.io/distroless/java21-debian13:nonroot
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-Xmx384m", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
