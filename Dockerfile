# Stage 1: Build the application using standard Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the Maven wrapper and project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Build the standard Spring Boot .jar file
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Stage 2: Runtime with standard Java 21 JRE
FROM eclipse-temurin:21-jre-jammy

# Install curl for the health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the compiled .jar file from the builder stage
# (Spring Boot creates a fat jar, so we just grab the main one)
COPY --from=builder /app/target/*.jar app.jar

# Add health check script
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080

# Run the standard Java application
ENTRYPOINT ["java", "-jar", "app.jar"]