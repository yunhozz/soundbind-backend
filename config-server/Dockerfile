FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/config-server
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/config-server/build/libs/config-server-0.0.1.jar config-server.jar
ENTRYPOINT ["java", "-jar", "config-server.jar"]