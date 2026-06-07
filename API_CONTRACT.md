# MessageForge API Contract

This document outlines all the available REST API endpoints in the MessageForge application.

## 1. Authentication Endpoints (`/api/auth`)

These endpoints manage user registration, login, and session tokens.

### POST `/api/auth/register`
- **Description:** Registers a new user.
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "Password123!",
    "displayName": "User Name"
  }
  ```
- **Response:** `201 Created` with `UserDto`.

### POST `/api/auth/login`
- **Description:** Authenticates a user and returns JWT tokens.
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "Password123!"
  }
  ```
- **Response:** `200 OK` with `AuthResponse` (contains `accessToken` and `refreshToken`).

### POST `/api/auth/refresh`
- **Description:** Refreshes an expired access token using a refresh token.
- **Request Body:**
  ```json
  {
    "refreshToken": "your-refresh-token-string"
  }
  ```
- **Response:** `200 OK` with `AuthResponse`.

### POST `/api/auth/logout`
- **Description:** Logs out the current user (requires authentication).
- **Response:** `204 No Content`.

### GET `/api/auth/me`
- **Description:** Retrieves the profile of the currently authenticated user.
- **Response:** `200 OK` with `UserDto`.

---

## 2. Channel & Integration Endpoints (`/api`)

These endpoints manage supported channels and user-specific third-party integrations.

### GET `/api/channels`
- **Description:** Returns a list of all supported channel types and their metadata.
- **Response:** `200 OK` with a list of channel objects.

### GET `/api/channels/{channel}/rules`
- **Description:** Returns specific formatting rules and constraints for a given channel (e.g., `EMAIL`, `TWITTER`).
- **Response:** `200 OK` with rules object.

### GET `/api/integrations`
- **Description:** Retrieves the current user's configured channel integrations.
- **Response:** `200 OK` with a list of `IntegrationResponse`.

### PUT `/api/integrations/{channel}`
- **Description:** Configures or updates an integration for a specific channel.
- **Request Body:** `ConfigureIntegrationRequest` (contains credentials/settings).
- **Response:** `200 OK` with updated `IntegrationResponse`.

### DELETE `/api/integrations/{channel}`
- **Description:** Disables and removes an integration for a specific channel.
- **Response:** `204 No Content`.

### POST `/api/integrations/{channel}/test`
- **Description:** Tests the connection/credentials for a configured integration.
- **Response:** `200 OK` with `IntegrationResponse` indicating the test status.

---

## 3. Message Management Endpoints (`/api/messages`)

These endpoints handle CRUD operations, formatting, and delivery for multi-channel messages.

### GET `/api/messages`
- **Description:** Lists all messages belonging to the user with pagination.
- **Query Params:** `page` (default: 0), `size` (default: 10).
- **Response:** `200 OK` with `MessageListResponse`.

### POST `/api/messages`
- **Description:** Creates a new multi-channel message draft.
- **Request Body:** `CreateMessageRequest`
  ```json
  {
    "title": "Campaign Title",
    "rawContent": "The main message content"
  }
  ```
- **Response:** `201 Created` with `MessageResponse`.

### POST `/api/messages/preview`
- **Description:** Generates formatted previews directly from compose text without first saving a draft.
- **Request Body:** `PreviewRequest`
  ```json
  {
    "rawContent": "The main message content",
    "activeChannels": ["EMAIL", "SMS"],
    "decorators": { "emoji": true, "hashtag": false, "signature": true }
  }
  ```
- **Response:** `200 OK` with a list of `PreviewResponse`.

### GET `/api/messages/{id}`
- **Description:** Retrieves detailed information about a specific message, including its formatted channel sub-messages.
- **Response:** `200 OK` with `MessageResponse`.

### PUT `/api/messages/{id}`
- **Description:** Updates an existing message draft.
- **Request Body:** `UpdateMessageRequest`
- **Response:** `200 OK` with updated `MessageResponse`.

### DELETE `/api/messages/{id}`
- **Description:** Deletes a message.
- **Response:** `204 No Content`.

### POST `/api/messages/{id}/duplicate`
- **Description:** Duplicates an existing message.
- **Response:** `201 Created` with the new `MessageResponse`.

### GET `/api/messages/{id}/preview`
- **Description:** Generates formatted previews of the message for all enabled channels without sending it.
- **Response:** `200 OK` with a list of `PreviewResponse`.

### POST `/api/messages/{id}/send`
- **Description:** Dispatches the message to the requested channels.
- **Request Body:** `SendMessageRequest` (specifies target channels and recipients).
- **Response:** `200 OK` with `MessageResponse` reflecting updated delivery statuses.

---

## 4. System Endpoints (`/api/actuator`)

### GET `/api/actuator/health` (also maps to `/health/readiness` and `/health/liveness`)
- **Description:** Basic health check endpoint.
- **Response:** `200 OK` with `{"status": "UP"}`.

---

## 5. WebSockets (`/api/ws-preview`)
- **Description:** STOMP over WebSocket endpoint used for real-time formatting previews while composing messages.
- **Message Mapping:** `@MessageMapping("/preview.request")`
