# Stage 1: Build the application using standard Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the Maven project files
COPY pom.xml ./
COPY src ./src

# Build the plain Spring MVC .war file
RUN mvn clean package -DskipTests

# Stage 2: Runtime with external Tomcat, not Spring Boot embedded Tomcat
FROM tomcat:10.1-jdk21-temurin

# Install curl for the health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Deploy as /api so existing routes stay http://host:8080/api/...
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/target/messageforge.war /usr/local/tomcat/webapps/api.war

EXPOSE 8080

CMD ["sh", "-c", "ls -lah /usr/local/tomcat/webapps && catalina.sh run"]
