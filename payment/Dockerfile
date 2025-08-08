FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY payment/build/libs/payment-0.0.1.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"] 