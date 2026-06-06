# 🚀 MessageForge - Multi-Channel Intelligent Messaging Platform

A production-ready **Spring Native** backend for automated multi-channel message formatting and delivery. Supports Email, SMS, Facebook Messenger, WhatsApp, LinkedIn, Slack, Twitter/X, and Telegram.

## 📋 Overview

MessageForge allows users to write a single message and automatically reformat it for multiple communication channels, each with its own constraints, character limits, and tone requirements. The architecture uses **Design Patterns** (Strategy, Decorator, Factory, Template Method, Observer, Builder) for maximum extensibility and maintainability.

### Key Features
- ✅ Multi-channel message formatting (8 channels)
- ✅ Real-time WebSocket previews
- ✅ JWT-based authentication with refresh tokens
- ✅ Async message processing with RabbitMQ
- ✅ Redis caching for templates
- ✅ PostgreSQL with JSONB for flexible configurations
- ✅ Spring Native for ultra-fast startup (~100ms) and low memory footprint
- ✅ Comprehensive audit logging
- ✅ Rate limiting (100 req/min per user)
- ✅ HTML sanitization for security

---

## 🏗️ Architecture

### Backend Stack
| Component | Technology | Version |
|-----------|------------|---------|
| **Runtime** | Spring Native (GraalVM) | 23.1.1 |
| **Framework** | Spring Boot | 3.3.0 |
| **Database** | PostgreSQL | 16 |
| **Cache** | Redis | 7 |
| **Message Queue** | RabbitMQ | 3.13 |
| **Language** | Java | 21 |
| **Build** | Maven | 3.9.4 |

### Design Patterns Implemented

#### 1. **Strategy Pattern** (Message Formatting)
Each channel (Email, SMS, LinkedIn, etc.) implements `MessageFormatterStrategy`. The `FormatterContext` dynamically selects and executes the appropriate formatter.

```
Channel Selection → FormatterContext → MessageFormatterStrategy → Formatted Output
```

**Benefits:**
- New channels can be added without modifying existing code (Open/Closed Principle)
- Easy to test each formatter in isolation
- Dynamic runtime selection

#### 2. **Decorator Pattern** (Message Enrichment)
Optional decorators (Emoji, Hashtag, Signature, URLShortener) can be chained to enhance messages per-channel.

```
Raw Message → EmojiDecorator → HashtagDecorator → SignatureDecorator → Final Message
```

#### 3. **Factory Pattern** (Sender Creation)
`ChannelSenderFactory` creates the correct sender instance based on channel type, using Spring's component registry.

#### 4. **Template Method Pattern** (Send Workflow)
`AbstractChannelSender` defines the skeleton: validate → format → authenticate → send → log.
Each channel overrides specific steps.

#### 5. **Observer Pattern** (Real-time Events)
Spring's `ApplicationEventPublisher` notifies observers when message status changes (useful for WebSocket updates and statistics).

#### 6. **Builder Pattern** (Object Construction)
`FormattedMessage.Builder` for immutable, fluent message object construction.

---

## 📁 Project Structure

```
messageforge/
├── pom.xml                          # Maven configuration with Spring Native
├── docker-compose.yml               # PostgreSQL, Redis, RabbitMQ stack
├── Dockerfile                       # Multi-stage GraalVM native build
├── README.md                        # This file
│
├── src/main/java/com/messageforge/
│   ├── MessageForgeApplication.java         # Entry point
│   │
│   ├── config/
│   │   ├── JwtTokenProvider.java            # JWT token generation/validation
│   │   ├── SecurityConfig.java              # Spring Security configuration
│   │   ├── WebSocketConfig.java             # WebSocket/STOMP configuration
│   │   ├── RedisConfig.java                 # Redis caching setup
│   │   ├── RabbitMqConfig.java              # RabbitMQ queue/exchange definitions
│   │   └── CorsCorsConfig.java              # CORS settings
│   │
│   ├── model/
│   │   ├── User.java                        # User entity (JPA)
│   │   ├── Message.java                     # Raw message entity
│   │   ├── ChannelMessage.java              # Formatted message per channel
│   │   ├── ChannelIntegration.java          # User's channel credentials
│   │   ├── ChannelType.java                 # Enum: EMAIL, SMS, FACEBOOK, etc.
│   │   └── RefreshToken.java                # JWT refresh token entity
│   │
│   ├── dto/
│   │   ├── AuthDtos.java                    # Login/Register request/response
│   │   ├── MessageDtos.java                 # Message CRUD DTOs
│   │   └── ChannelDtos.java                 # Channel integration DTOs
│   │
│   ├── repository/
│   │   ├── UserRepository.java              # User data access
│   │   ├── MessageRepository.java           # Message queries
│   │   ├── ChannelMessageRepository.java    # ChannelMessage queries
│   │   └── ChannelIntegrationRepository.java# Channel integration queries
│   │
│   ├── service/
│   │   ├── AuthService.java                 # Registration, login, token refresh
│   │   ├── MessageService.java              # Message CRUD and preview logic
│   │   ├── ChannelIntegrationService.java   # Channel setup and testing
│   │   └── MessageSenderService.java        # Async message sending orchestration
│   │
│   ├── formatter/
│   │   ├── MessageFormatterStrategy.java    # Strategy interface
│   │   ├── FormatterContext.java            # Strategy pattern context
│   │   ├── EmailFormatterStrategy.java      # Email formatter
│   │   ├── SmsFormatterStrategy.java        # SMS formatter (160 char limit)
│   │   ├── FacebookFormatterStrategy.java   # Facebook/Messenger
│   │   ├── LinkedinFormatterStrategy.java   # LinkedIn (professional tone)
│   │   ├── SlackFormatterStrategy.java      # Slack (Markdown support)
│   │   ├── TwitterFormatterStrategy.java    # Twitter/X (280 char limit)
│   │   └── TelegramFormatterStrategy.java   # Telegram (unlimited)
│   │
│   ├── decorator/
│   │   ├── MessageDecorator.java            # Base decorator interface
│   │   ├── EmojiDecorator.java              # Add relevant emojis
│   │   ├── HashtagDecorator.java            # Add trending hashtags
│   │   ├── SignatureDecorator.java          # Append signature
│   │   └── UrlShortenerDecorator.java       # Shorten long URLs
│   │
│   ├── integration/
│   │   ├── MailgunClient.java               # Email API (HTTP client)
│   │   ├── TwilioClient.java                # SMS API
│   │   ├── MetaGraphApiClient.java          # Facebook/WhatsApp API
│   │   ├── LinkedinApiClient.java           # LinkedIn API
│   │   ├── SlackApiClient.java              # Slack API
│   │   ├── TwitterApiClient.java            # Twitter/X API
│   │   └── TelegramApiClient.java           # Telegram API
│   │
│   ├── controller/
│   │   ├── AuthController.java              # POST /auth/login, /register, /refresh
│   │   ├── MessageController.java           # CRUD messages + send
│   │   ├── ChannelController.java           # Channel config and testing
│   │   └── WebSocketController.java         # STOMP endpoints for previews
│   │
│   ├── websocket/
│   │   ├── StompConfig.java                 # STOMP message handler
│   │   └── PreviewHandler.java              # Real-time preview via WebSocket
│   │
│   ├── exception/
│   │   ├── ApiException.java                # Custom exception
│   │   └── GlobalExceptionHandler.java      # @RestControllerAdvice for error responses
│   │
│   └── util/
│       ├── EncryptionUtil.java              # AES-256 for credential storage
│       ├── ValidationUtil.java              # Input validation helpers
│       └── JsonUtil.java                    # JSON parsing utilities
│
├── src/main/resources/
│   ├── application.yml                      # Spring configuration (dev mode)
│   ├── application-prod.yml                 # Production overrides
│   │
│   └── db/migration/
│       ├── V1__Initial_Schema.sql           # Flyway: Create all tables
│       ├── V2__Add_Indexes.sql              # Add performance indexes
│       └── V3__Initial_Data.sql             # System templates & defaults
│
└── src/test/java/com/messageforge/
    ├── EmailFormatterTests.java
    ├── SmsFormatterTests.java
    ├── MessageServiceTests.java
    └── AuthControllerTests.java
```

---

## 🗄️ Database Schema

### Core Tables

#### **users**
```sql
id (UUID, PK) | email (UNIQUE) | password_hash | display_name | avatar_url | 
is_active | created_at | updated_at | deleted_at
```

#### **messages**
```sql
id (UUID, PK) | user_id (FK) | raw_content (TEXT) | title | status 
| created_at | sent_at | metadata (JSONB) | deleted_at
```

#### **channel_messages**
```sql
id (UUID, PK) | message_id (FK) | channel_type | formatted_content | 
applied_decorators (JSONB) | status | sent_at | external_message_id | 
error_message | retry_count
```

#### **channel_integrations**
```sql
id (UUID, PK) | user_id (FK) | channel_type | is_enabled | 
credentials (BYTEA, encrypted) | settings (JSONB) | last_tested_at | 
test_status | created_at | updated_at
```

#### **formatter_templates**
```sql
id (UUID, PK) | user_id (FK, nullable) | channel_type | name | 
template_body (TEXT) | is_default | is_system | created_at
```

### Additional Tables
- **refresh_tokens** — JWT refresh token management
- **message_recipients** — Track delivery per recipient
- **message_attachments** — File storage references
- **audit_logs** — All user actions
- **channel_statistics** — Per-channel message counts

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** (for compilation) or **GraalVM 23.1+** (for native image)
- **Maven 3.9+**
- **Docker & Docker Compose** (optional, for services)
- **Git**

### 1. Clone & Setup

```bash
# Clone the repository
git clone <repo-url>
cd messageforge

# Start infrastructure (PostgreSQL, Redis, RabbitMQ)
docker-compose up -d

# Wait for services to be healthy
docker-compose logs -f postgres

# Verify databases
# PostgreSQL: localhost:5432 (postgres/postgres)
# Redis: localhost:6379
# RabbitMQ Management: http://localhost:15672 (guest/guest)
# PgAdmin: http://localhost:5050 (admin@example.com/admin)
```

### 2. Build the Application

#### Standard Spring Boot JAR
```bash
mvn clean package -DskipTests
java -jar target/messageforge-1.0.0.jar
```

#### Spring Native Native Image (GraalVM)
Requires GraalVM installation:
```bash
# Install GraalVM Native Image
gu install native-image

# Build native executable
mvn -Pnative clean package

# Run (ultra-fast startup!)
./target/messageforge
```

#### Using Docker (Docker Compose)
We have containerized the application and its dependencies (PostgreSQL, Redis, RabbitMQ, pgAdmin) using Docker Compose:

```bash
# Build the JVM JAR and run the entire stack in the background
docker-compose up -d --build

# Check the logs of the backend application
docker-compose logs -f app

# Stop the stack
docker-compose down
```
The application runs on port `8085` on the host machine to avoid conflicts with other local servers running on `8080`.


### 3. Configuration

Create `.env` file:
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/messageforge
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=your-super-secret-key-change-this-in-production-min-64-characters
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# Encryption
ENCRYPTION_KEY=your-32-char-encryption-key

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# API Keys
MAILGUN_API_KEY=your-mailgun-key
MAILGUN_DOMAIN=sandboxXXX.mailgun.org
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_PHONE=+1234567890
META_APP_ID=your-meta-app-id
META_APP_SECRET=your-meta-app-secret
SLACK_BOT_TOKEN=xoxb-your-slack-token
TWITTER_API_KEY=your-twitter-key
TWITTER_API_SECRET=your-twitter-secret

# Frontend
FRONTEND_URL=http://localhost:3000
SERVER_PORT=8080
```

Load via Spring:
```bash
# Option 1: Environment variables
export DB_URL=jdbc:postgresql://localhost:5432/messageforge
export JWT_SECRET=...
java -jar target/messageforge-1.0.0.jar

# Option 2: application.yml
# Edit src/main/resources/application.yml

# Option 3: Docker Compose with .env
docker-compose up -d
```

### 4. Verify Installation

> [!NOTE]
> We use `127.0.0.1` instead of `localhost` in curl commands to bypass any HSTS policies cached on the host machine.

```bash
# Check API health
curl http://127.0.0.1:8085/api/actuator/health

# Check metrics
curl http://127.0.0.1:8085/api/actuator/metrics
```

### 5. Testing the API (Step-by-Step Flow)

Follow this step-by-step flow in your terminal to test user registration, authentication, channel integration config, message drafting, and formatting preview:

#### Step A: Register a User
```bash
curl -X POST http://127.0.0.1:8085/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@1234","displayName":"Test User"}'
```

#### Step B: Log In (Obtain Access Token)
```bash
curl -X POST http://127.0.0.1:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@1234"}'
```
*Copy the `accessToken` string from the returned JSON response to use as `<ACCESS_TOKEN>` below.*

#### Step C: Enable Integrations (SMS and Twitter)
By default, previews are only generated for enabled integrations. Run these commands to activate them for the user:
```bash
# Enable SMS
curl -X PUT http://127.0.0.1:8085/api/integrations/SMS \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{"enabled":true,"credentials":{},"settings":{}}'

# Enable Twitter/X
curl -X PUT http://127.0.0.1:8085/api/integrations/TWITTER \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{"enabled":true,"credentials":{},"settings":{}}'
```

#### Step D: Create a Message Draft
```bash
curl -X POST http://127.0.0.1:8085/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{"title":"First Message","rawContent":"Hello World! Please check out https://google.com/deepmind for more details #welcome"}'
```
*Copy the message `"id"` UUID from the response to use as `<MESSAGE_ID>` below.*

#### Step E: Preview Channel Formatting
```bash
curl -X GET http://127.0.0.1:8085/api/messages/<MESSAGE_ID>/preview \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
This will return a list showing how the message was custom-formatted for SMS (where URLs are formatted to `[URL]`) and Twitter/X (with character constraints applied).

---


## 📡 API Endpoints

### Authentication
```
POST   /auth/register           Register new user
POST   /auth/login              Login (returns JWT + refresh token)
POST   /auth/refresh            Refresh access token
POST   /auth/logout             Logout (invalidate refresh token)
GET    /auth/me                 Get current user profile
```

### Messages
```
GET    /messages                List user's messages (paginated)
POST   /messages                Create new draft message
GET    /messages/{id}           Get message details
PUT    /messages/{id}           Update message content
DELETE /messages/{id}           Soft-delete message
POST   /messages/{id}/send      Send to selected channels
GET    /messages/{id}/preview   Get previews for all active channels
POST   /messages/{id}/duplicate Duplicate to new draft
```

### Channels & Integration
```
GET    /channels                List all available channels
GET    /integrations            Get user's channel integrations
PUT    /integrations/{channel}  Configure/enable channel
DELETE /integrations/{channel}  Disable channel
POST   /integrations/{channel}/test  Test connection
GET    /channels/{channel}/rules Get formatting rules for channel
```

### WebSocket (Real-time Preview)
```
WS     /ws-preview              WebSocket endpoint
  → /app/preview.request         Send raw content + active channels
  ← /topic/preview.{userId}      Receive formatted previews
  ← /topic/send.progress.{userId}  Receive send progress updates
```

---

## 🔐 Security Features

### Authentication & Authorization
- **JWT (HS512)** with 15-minute expiration
- **Refresh tokens** (7 days) stored in HttpOnly cookies
- **Spring Security** filter chain with stateless authentication
- **Rate limiting** (100 req/min per user) using Bucket4j

### Data Protection
- **AES-256 encryption** for API credentials in database
- **HTML sanitization** (OWASP) for message content
- **Password hashing** (BCrypt) for user authentication
- **HTTPS enforced** in production (HSTS headers)

### Audit & Monitoring
- **Audit logs** for all user actions
- **Encrypted credentials** — never logged in plaintext
- **SQL injection prevention** via parameterized queries (JPA)
- **CSRF protection** (disabled for REST API + CORS)

---

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests (with Testcontainers)
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
# Open target/site/jacoco/index.html
```

### Load Testing (k6)
```bash
k6 run tests/load-test.js
```

---

## 📊 Monitoring & Observability

### Actuator Endpoints
```
GET /actuator/health            Health check (with db, redis, rabbitmq probes)
GET /actuator/metrics           Prometheus-compatible metrics
GET /actuator/prometheus        Prometheus scrape endpoint
GET /actuator/info              Application info
GET /actuator/env               Configuration properties
```

### Grafana Dashboard
```
# Run Grafana
docker run -d -p 3000:3000 grafana/grafana:latest

# Add Prometheus data source: http://localhost:8080/api/actuator/prometheus
# Import dashboard JSON
```

### Logs
```bash
# Development (console)
tail -f logs/messageforge.log

# Docker
docker logs messageforge -f

# Structured logging (JSON)
export LOG_FORMAT=json
java -jar target/messageforge-1.0.0.jar
```

---

## 🔧 Development Workflow

### Create a New Formatter (Example: WhatsApp)

1. **Create Strategy class:**
```java
@Component
public class WhatsappFormatterStrategy implements MessageFormatterStrategy {
    @Override
    public String format(String rawContent, JsonNode metadata) {
        // WhatsApp-specific logic (65536 char limit, supports media, emojis)
        return formatted;
    }
    
    @Override
    public ChannelType getChannelType() { return ChannelType.WHATSAPP; }
    
    @Override
    public int getCharacterLimit() { return 65536; }
    
    @Override
    public FormattingResult validate(String content) { /* ... */ }
}
```

2. **Register automatically** (Spring scans `@Component`)

3. **Use in FormatterContext:**
```java
formatterContext.format(rawContent, ChannelType.WHATSAPP, metadata);
```

### Add a New Channel Sender

1. Create `WhatsappChannelSender extends AbstractChannelSender`
2. Implement `authenticate()`, `send()`, `validateRecipient()`
3. Register in `ChannelSenderFactory` (via `@Component`)
4. Configure API key in `application.yml`
5. Done! Ready to send messages.

---

## 🚢 Deployment

### Local Development
```bash
docker-compose up -d
mvn spring-boot:run
```

### Production with Spring Native

#### Benefits of Spring Native
- **Instant startup:** ~100ms vs 2-3s with Spring Boot
- **Low memory footprint:** 50-150MB vs 300-500MB
- **Serverless-ready:** AWS Lambda, Google Cloud Run, Azure Functions
- **Predictable performance:** No JIT warmup period

#### Build Native Image
```bash
mvn -Pnative clean package

# Result: target/messageforge (executable binary)
# Size: ~80-100MB (including JVM)
```

#### Docker Production
```dockerfile
# See Dockerfile in repo (multi-stage build)
docker build -t messageforge:latest .
docker push registry.example.com/messageforge:latest
```

#### Kubernetes Deployment
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: messageforge
spec:
  replicas: 3
  selector:
    matchLabels:
      app: messageforge
  template:
    metadata:
      labels:
        app: messageforge
    spec:
      containers:
      - name: messageforge
        image: registry.example.com/messageforge:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: value
        livenessProbe:
          httpGet:
            path: /api/actuator/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/actuator/health/readiness
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 10
```

---

## 🤝 Contributing

### Code Standards
- **Java 21+** with `var` for local variables
- **Spring conventions** (repositories, services, controllers)
- **DTO pattern** for API contracts
- **Test coverage** >80% (unit + integration)
- **Lombok** for boilerplate reduction

### Branching Strategy
```
main          → Production (tags for releases)
develop       → Staging/Integration
feature/*     → New features/bugs
```

### Commit Convention
```
feat: Add WhatsApp formatter strategy
fix: Handle emoji truncation in SMS
docs: Update API documentation
test: Add unit tests for decorator pattern
refactor: Simplify FormatterContext
```

---

## 📚 Additional Resources

### Documentation
- [Spring Boot 3.3 Documentation](https://spring.io/projects/spring-boot)
- [Spring Native Guide](https://spring.io/projects/spring-native)
- [Spring Data JPA Reference](https://spring.io/projects/spring-data-jpa)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8949)

### Design Patterns
- [Strategy Pattern](https://refactoring.guru/design-patterns/strategy)
- [Decorator Pattern](https://refactoring.guru/design-patterns/decorator)
- [Factory Pattern](https://refactoring.guru/design-patterns/factory-method)
- [Observer Pattern](https://refactoring.guru/design-patterns/observer)

### API References
- [Mailgun API](https://documentation.mailgun.com/en/latest/api-intro.html)
- [Twilio SMS API](https://www.twilio.com/docs/sms)
- [Meta Graph API](https://developers.facebook.com/docs/graph-api)
- [Slack API](https://api.slack.com/)
- [Twitter API v2](https://developer.twitter.com/en/docs/twitter-api)

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process on port 8080
lsof -i :8080
# Kill process
kill -9 <PID>
```

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Reset database
docker-compose down -v
docker-compose up -d postgres
mvn flyway:clean flyway:migrate
```

### JWT Token Expired
```bash
# Token expires in 15 minutes
# Refresh using: POST /auth/refresh with refresh_token
# Refresh token expires in 7 days (refresh login)
```

### Memory Issues with Native Build
```bash
# Increase Maven heap
export MAVEN_OPTS="-Xmx4g"
mvn -Pnative clean package
```

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/your-repo/issues)
- **Discussions:** [GitHub Discussions](https://github.com/your-repo/discussions)
- **Email:** support@messageforge.io

---

## 📄 License

MessageForge is licensed under the **MIT License**. See [LICENSE](./LICENSE) file for details.

---

## 🎯 Roadmap

### Phase 1 (Completed)
- ✅ Spring Native setup
- ✅ Database schema & migrations
- ✅ Core formatters (Email, SMS, LinkedIn)
- ✅ JWT authentication

### Phase 2 (In Progress)
- 🔄 Additional formatters (Facebook, WhatsApp, Slack, Twitter, Telegram)
- 🔄 Real-time WebSocket previews
- 🔄 API integrations

### Phase 3 (Planned)
- ⏳ Advanced decorator system
- ⏳ Message scheduling
- ⏳ A/B testing
- ⏳ Analytics dashboard

---

**Built with ❤️ for developers who value clean architecture and production readiness.**
