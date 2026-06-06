# Stage 1: Build the application
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Build standard fat JAR
RUN mvn clean package -DskipTests

# Stage 2: Lightweight JRE runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/messageforge-1.0.0.jar /app/messageforge.jar

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Add health check script
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080

# Run the JAR application
ENTRYPOINT ["java", "-jar", "/app/messageforge.jar"]

