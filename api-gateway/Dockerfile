FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/api-gateway
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --info --stacktrace

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/api-gateway/build/libs/api-gateway-0.0.1.jar api-gateway.jar
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]