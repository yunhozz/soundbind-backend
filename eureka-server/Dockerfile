FROM openjdk:21-slim AS builder
WORKDIR /app
COPY . .
WORKDIR /app/eureka-server
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --info --stacktrace

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/eureka-server/build/libs/eureka-server-0.0.1.jar eureka-server.jar
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]