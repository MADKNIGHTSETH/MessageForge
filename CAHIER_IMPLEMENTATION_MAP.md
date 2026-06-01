# MessageForge Cahier Implementation Map

Source: `MessageForge_Cahier_de_Charges.docx`

Note: the cahier specifies Spring Boot 3.x, but this project intentionally uses plain Spring MVC + external Tomcat per the latest project decision.

## Backend Platform

| Cahier Requirement | Current Project Mapping |
| --- | --- |
| Backend framework | Plain Spring Framework 6 MVC, no Spring Boot parent, starters, or `SpringApplication` |
| Java | Java 21 target in Maven |
| API style | REST JSON via Spring MVC controllers |
| Runtime | WAR deployed to Tomcat 10.1 as `/api` |
| Database | PostgreSQL 16 via HikariCP, Hibernate, Spring Data JPA |
| Migrations | Flyway with PostgreSQL-compatible `V1__Initial_Schema.sql` |
| Auth | Spring Security + JWT helper + register/login endpoints |
| Cache | Redis dependency present, not wired yet |
| Queue | RabbitMQ env config present, client layer not wired yet |
| WebSocket | Spring WebSocket dependencies present, endpoints not implemented yet |

## Implemented Code

| Area | Files |
| --- | --- |
| Plain Spring bootstrap | `AppConfig`, `WebMvcConfig`, `PersistenceConfig`, `MessageForgeWebApplicationInitializer`, `SecurityWebApplicationInitializer` |
| Auth endpoints | `AuthController`, `AuthService`, `AuthDtos`, `User`, `UserRepository`, `JwtTokenProvider`, `SecurityConfig` |
| Message model | `Message`, `ChannelMessage`, `ChannelIntegration`, repositories, DTOs |
| Strategy pattern | `MessageFormatterStrategy`, `FormatterContext`, `EmailFormatterStrategy`, `SmsFormatterStrategy` |
| Channel definitions | `ChannelType` enum with all cahier channels |
| Docker runtime | `Dockerfile` builds WAR and deploys to Tomcat as `/api.war` |

## Cahier Feature Coverage

| Feature | Status |
| --- | --- |
| Register/login JWT | Partial: implemented, refresh/logout/me still missing |
| Message CRUD | Model/repositories/DTOs exist, controllers/services missing |
| Multi-channel preview | Formatter base exists for Email/SMS, preview endpoint missing |
| Channel toggles/integrations | Model exists, API and frontend missing |
| Decorator pattern | Missing |
| Template method sender pattern | Missing |
| Sender factory | Missing |
| Observer events | Missing |
| WebSocket preview/progress | Missing |
| Full channel rules | Partial: Email/SMS only, other channels missing |
| Vue frontend | Missing |
| File storage / MinIO | Missing |
| RabbitMQ async sending | Missing |
| Rate limiting / CSP / encryption | Dependencies/config direction exist, implementation missing |

## API Target From Cahier

| Route | Current Status |
| --- | --- |
| `POST /api/auth/register` | Implemented |
| `POST /api/auth/login` | Implemented |
| `POST /api/auth/refresh` | Missing |
| `POST /api/auth/logout` | Missing |
| `GET /api/auth/me` | Missing |
| `/api/messages/**` | Missing controller/service |
| `/api/channels` | Missing |
| `/api/integrations/**` | Missing |
| `/ws-preview` | Missing |

## Next Build Order

1. Complete auth: refresh token persistence, logout, `me`, JWT request filter.
2. Add message service/controller: CRUD, preview, duplicate, send placeholder.
3. Add formatter implementations for LinkedIn, Slack, Twitter/X, Facebook, WhatsApp, Telegram.
4. Add decorator package: emoji, hashtag, signature, URL shortener.
5. Add sender package: template method base, simulated senders, sender factory.
6. Add WebSocket preview/progress events.
7. Add Vue 3 frontend matching the cahier screens.
