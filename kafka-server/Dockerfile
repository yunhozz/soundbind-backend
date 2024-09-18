FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
WORKDIR /app/kafka-server
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/kafka-server/build/libs/kafka-server-0.0.1.jar kafka-server.jar
ENTRYPOINT ["java", "-jar", "kafka-server.jar"]