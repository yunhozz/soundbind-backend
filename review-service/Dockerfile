FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/review-service
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/review-service/build/libs/review-service-0.0.1.jar review-service.jar
ENTRYPOINT ["java", "-jar", "review-service.jar"]