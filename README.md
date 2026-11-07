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