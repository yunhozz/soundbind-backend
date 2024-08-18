FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/notification-service
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/notification-service/build/libs/notification-service-0.0.1.jar notification-service.jar
ENTRYPOINT ["java", "-jar", "notification-service.jar"]