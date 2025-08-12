FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY booking/build/libs/booking-0.0.1.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"] 