# ⚡ MessageForge Quick Start Guide

Get MessageForge running in **5 minutes** on your local machine.

## Prerequisites Check

```bash
# Java 21
java -version
# Should output: openjdk version "21" or higher

# Maven
mvn --version
# Should output: Apache Maven 3.9.x or higher

# Docker & Docker Compose
docker --version
docker-compose --version
```

If you're missing any, install them first:
- **Java 21:** [AdoptOpenJDK](https://adoptopenjdk.net/) or [Eclipse Temurin](https://adoptium.net/)
- **Maven:** `brew install maven` (macOS) or [Maven Download](https://maven.apache.org/download.cgi)
- **Docker Desktop:** [Download](https://www.docker.com/products/docker-desktop/)

---

## Step 1: Clone & Navigate

```bash
git clone https://github.com/your-org/messageforge.git
cd messageforge
```

---

## Step 2: Start Infrastructure (1 min)

```bash
# Start PostgreSQL, Redis, RabbitMQ in Docker
docker-compose up -d

# Verify all services are healthy
docker-compose ps
# You should see: postgres, redis, rabbitmq, pgadmin (all "Up")

# Wait 10 seconds for PostgreSQL to be ready
sleep 10
```

**Credentials:**
- PostgreSQL: `postgres` / `postgres` on `localhost:5432`
- Redis: `localhost:6379` (no auth)
- RabbitMQ Admin: http://localhost:15672 (guest / guest)
- PgAdmin: http://localhost:5050 (admin@example.com / admin)

---

## Step 3: Setup Environment Variables (1 min)

```bash
# Copy example env file
cp .env.example .env

# For local development, default values work as-is
# No changes needed for .env unless you want to customize

# (Optional) Generate a secure JWT secret for production
openssl rand -base64 32 | tr -d '\n' | cut -c1-64
# Copy this and paste into JWT_SECRET in .env
```

---

## Step 4: Build the Application (2 min)

```bash
# Build as Spring Boot JAR (standard, runs everywhere)
mvn clean package -DskipTests

# This creates: target/messageforge-1.0.0.jar
```

---

## Step 5: Run the Application (1 min)

```bash
# Option A: Run Spring Boot JAR
java -jar target/messageforge-1.0.0.jar

# Option B: Run with Maven
mvn spring-boot:run

# Option C: Run Native Image (requires GraalVM setup - skip for now)
# mvn -Pnative clean package
# ./target/messageforge
```

**Watch for this output:**
```
Started MessageForgeApplication in 2.150 seconds
```

✅ **You're running!**

---

## Step 6: Verify It Works (1 min)

```bash
# In a new terminal tab:

# Check health
curl http://localhost:8080/api/actuator/health
# Response: {"status":"UP",...}

# Check metrics
curl http://localhost:8080/api/actuator/metrics
# Response: {"names":[...]}

# Create test user (register)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@1234",
    "displayName": "Test User"
  }'
# Response: 
# {
#   "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
#   "tokenType": "Bearer",
#   "user": {...}
# }

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@1234"
  }'

# Create a message (use token from login)
TOKEN="your-token-from-above"
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "rawContent": "Hello, world! This is my first message.",
    "title": "First Message"
  }'
# Response: {"id": "uuid...", "status": "DRAFT", ...}

# Get all messages
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/messages
```

---

## Common Commands

### Development
```bash
# Build without tests
mvn clean package -DskipTests

# Run tests only
mvn test

# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.com.messageforge=DEBUG"

# Access Spring Profile (dev/prod)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Database
```bash
# View database (PgAdmin)
# http://localhost:5050
# Email: admin@example.com
# Password: admin
# Then add server: localhost:5432

# Reset database (careful!)
docker-compose down -v
docker-compose up -d postgres
sleep 15
mvn spring-boot:run

# Run migrations manually
mvn flyway:clean
mvn flyway:migrate
```

### Infrastructure
```bash
# View Docker logs
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f rabbitmq

# Stop all services
docker-compose down

# Stop and remove volumes (DELETES DATA)
docker-compose down -v

# Restart services
docker-compose restart
```

### Clean Up
```bash
# Remove Docker containers only (keeps volumes)
docker-compose down

# Full cleanup (removes everything)
docker-compose down -v
rm -rf target/ .m2/

# Kill process on port 8080 (if stuck)
lsof -i :8080
kill -9 <PID>
```

---

## Useful URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| **API** | http://localhost:8080/api | None (JWT protected) |
| **Health** | http://localhost:8080/api/actuator/health | Public |
| **Metrics** | http://localhost:8080/api/actuator/metrics | Public |
| **PgAdmin** | http://localhost:5050 | admin@example.com / admin |
| **RabbitMQ** | http://localhost:15672 | guest / guest |
| **Documentation** | See [README.md](./README.md) | — |

---

## Troubleshooting

### "Connection Refused" on Database
```bash
# PostgreSQL not ready?
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres

# Wait 10 seconds and try again
sleep 10
```

### "Port Already in Use"
```bash
# Find what's using port 8080
lsof -i :8080

# Kill it
kill -9 <PID>

# Try alternative port
java -Dserver.port=8081 -jar target/messageforge-1.0.0.jar
```

### "Build Fails"
```bash
# Clear cache and rebuild
mvn clean
mvn -U clean package -DskipTests

# Check Java version
javac -version
# Should be 21+
```

### "Flyway Migration Failed"
```bash
# Flyway tried to auto-migrate but failed?
# Reset and retry:
docker-compose down -v postgres
docker-compose up -d postgres
sleep 15
mvn spring-boot:run
```

### Tests Fail Locally But Pass CI?
```bash
# Testcontainers might be hanging
# Install Docker before running tests:
docker ps

# Or skip tests during build:
mvn clean package -DskipTests
```

---

## Next Steps

1. **Read the Full README:** [README.md](./README.md)
2. **Explore the API:** Swagger/Springdoc coming in Phase 2
3. **Add a Formatter:** [Contributing Guide](#contributing) (coming soon)
4. **Deploy:** [Deployment Guide](#deployment) in README

---

## Need Help?

- **Issues:** [GitHub Issues](https://github.com/your-org/messageforge/issues)
- **Discussions:** [GitHub Discussions](https://github.com/your-org/messageforge/discussions)
- **Slack:** [Join Workspace](https://slack.messageforge.io)

---

**That's it! You're ready to code. Happy building! 🚀**
