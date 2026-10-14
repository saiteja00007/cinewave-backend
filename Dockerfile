# Stage 1: Build the Spring Boot application using Maven and Java 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Package application without running unit tests during Docker build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight Java 21 JRE runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create unprivileged application user
RUN addgroup -S cinewave && adduser -S cinewave -G cinewave
USER cinewave:cinewave

# Copy compiled JAR from builder stage
COPY --from=builder --chown=cinewave:cinewave /build/target/*.jar app.jar

# Dynamic PORT support for Render, Railway, Heroku, etc.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -jar app.jar"]
