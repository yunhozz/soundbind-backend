FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/music-service
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/music-service/build/libs/music-service-0.0.1.jar music-service.jar
ENTRYPOINT ["java", "-jar", "music-service.jar"]