FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY event/build/libs/did-0.0.1.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"] 