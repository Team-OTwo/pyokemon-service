FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/account-0.0.1.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 