# Distributed Rate Limiter

A production-oriented Spring Boot rate-limiting starter demonstrating distributed request limiting with Apache Ignite, Token Bucket and Sliding Window algorithms.

## Features
- Java 17 / Spring Boot 3
- Reusable annotation-based Spring Boot starter
- Token Bucket and Sliding Window algorithms
- Distributed state with Apache Ignite
- Fail-open / fail-closed behavior
- REST demo endpoint
- Unit tests
- Docker Compose
- k6 load-test script

## Run
```bash
docker compose up -d
./mvnw spring-boot:run
```

Without Maven wrapper:
```bash
mvn spring-boot:run
```

Demo endpoint: `GET http://localhost:8080/api/demo`

## Example
```java
@RateLimit(key = "#request.remoteAddress", limit = 10, windowSeconds = 60)
@GetMapping
public String demo(HttpServletRequest request) { return "ok"; }
```

## Design
The annotation is intercepted by an Aspect. A key is resolved from the request, then the configured algorithm evaluates the request against distributed state. The application returns HTTP 429 when the limit is exceeded.

This repository intentionally documents design trade-offs instead of claiming benchmark numbers that were not measured.
