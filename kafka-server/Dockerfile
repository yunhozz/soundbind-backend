FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/kafka-server
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/kafka-server/build/libs/kafka-server-0.0.1.jar kafka-server.jar
ENTRYPOINT ["java", "-jar", "kafka-server.jar"]