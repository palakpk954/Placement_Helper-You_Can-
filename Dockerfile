# ==============================================================
# Dockerfile for You_can — JECRC University Placement Portal
# Multi-stage build optimized for Render / Cloud deployments (Java 21)
# ==============================================================

# Stage 1: Build the application with Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build the production artifact
COPY backend ./backend
COPY frontend ./frontend
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, secure JRE runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root application user for container security
RUN groupadd -r youcan && useradd -r -g youcan youcan

# Copy the compiled executable JAR from the builder stage
COPY --from=build /app/target/youcan-1.0.0.jar app.jar

# Create directory for persistent embedded database and upload storage
RUN mkdir -p data uploads/resumes && chown -R youcan:youcan /app

USER youcan

# Render sets the $PORT environment variable dynamically (defaults to 8080 locally)
ENV PORT=8080
EXPOSE 8080

# Configure JVM memory limits for Render free tier (512MB RAM) and dynamic port binding
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -Xmx384m -Xms256m -Dserver.port=${PORT} -jar app.jar"]
