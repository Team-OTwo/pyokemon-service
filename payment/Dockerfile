FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :payment:bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/payment/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"] 