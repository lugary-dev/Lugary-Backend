# Etapa 1: compilación con Gradle
FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test --no-daemon

# Etapa 2: ejecución del JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]