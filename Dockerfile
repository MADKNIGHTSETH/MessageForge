# Stage 1: Build the application
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Build Spring Native native image
RUN mvn -Pnative clean native:compile -DskipTests

# Stage 2: Runtime with Spring Native binary
FROM ubuntu:23.10

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    ca-certificates \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the native executable from builder
COPY --from=builder /app/target/messageforge /app/messageforge

# Add health check script
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080

# Run the native executable
ENTRYPOINT ["/app/messageforge"]
CMD ["-Dspring.profiles.active=prod"]
