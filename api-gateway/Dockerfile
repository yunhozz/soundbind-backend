FROM openjdk:21-slim
COPY . .
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "/build/libs/api-gateway-0.0.1.jar"]
RUN apt-get -y -qq update