FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :event:bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/event/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"] 