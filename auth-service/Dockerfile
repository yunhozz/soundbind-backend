FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/auth-service
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/auth-service/build/libs/auth-service-0.0.1.jar auth-service.jar
ENTRYPOINT ["java", "-jar", "auth-service.jar"]