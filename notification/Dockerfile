FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"] 