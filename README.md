# PulseCheck

Website and API uptime monitoring platform built with Spring Boot.

PulseCheck periodically checks websites and APIs, opens incidents when a service goes down, notifies the team and keeps uptime statistics.

## Tech stack

- Java 25, Spring Boot 4, Maven
- PostgreSQL, Spring Data JPA, Flyway
- Spring Security with JWT
- Apache Kafka, Redis
- Swagger / OpenAPI
- JUnit 5, Testcontainers
- Docker Compose

## Running locally

Requirements: JDK 25 and Docker Desktop.

```bash
docker compose up -d
./mvnw spring-boot:run
```

The API runs on http://localhost:8080.

## Status

In active development.

## Authentication

The API uses stateless JWT authentication.

1. `POST /api/v1/auth/register` creates an account.
2. `POST /api/v1/auth/login` returns a token: `{ "token": "...", "tokenType": "Bearer" }`.
3. Send the token with every protected request: `Authorization: Bearer <token>`.

Public endpoints: `/api/v1/auth/**`, Swagger UI (`/swagger-ui.html`) and `/status/**`. Everything else requires a valid token and returns `401` without one.

### Configuration

| Variable | Description | Default |
|---|---|---|
| `JWT_SECRET` | Secret used to sign tokens (at least 32 characters) | development-only value |
| `jwt.expiration-ms` | Token lifetime in milliseconds | `3600000` (1 hour) |

Always set `JWT_SECRET` to your own value outside local development.

Passwords must be 8-72 characters and at most 72 bytes in UTF-8, because BCrypt ignores anything beyond 72 bytes.

## Teams

Users can create teams and manage members with roles `OWNER`, `MEMBER` and `VIEWER`. All endpoints require a JWT.

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/teams` | Create a team; the creator becomes its `OWNER` |
| GET | `/api/v1/teams` | List the current user's teams |
| GET | `/api/v1/teams/{teamId}/members` | List members (team members only) |
| POST | `/api/v1/teams/{teamId}/members` | Add a member by email (`OWNER` only) |
| PUT | `/api/v1/teams/{teamId}/members/{userId}/role` | Change a member's role (`OWNER` only) |
| DELETE | `/api/v1/teams/{teamId}/members/{userId}` | Remove a member, or leave the team yourself |

A team always keeps at least one `OWNER`: demoting or removing the last one returns `422`.

### Demo data

Start the application with the `dev` profile to create a demo account and team:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Log in as `demo@pulsecheck.com` / `demo12345` (team `Demo Team`, with `colleague@pulsecheck.com` as a member).