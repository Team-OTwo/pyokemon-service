FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY notification/build/libs/notification-0.0.1.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"] 