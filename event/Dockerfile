FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/event-0.0.1.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"] 