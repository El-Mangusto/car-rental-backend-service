# Car Rental Backend Service

> **Status: in progress.** Core domain (users, cars, bookings) is functional and tested; more features are still being added.

A Spring Boot backend for a car rental platform — manages users, a car fleet, and bookings, with availability derived from actual booking overlaps rather than a manually-maintained status flag.

## Features

- **Booking lifecycle** — create, fetch (paginated, filterable by user), and cancel bookings, with a full validation chain: user existence and ban status, car existence and availability, overlap detection against existing bookings, and a cutoff on cancelling once the rental period has started.
- **Availability via overlap queries** — car availability isn't a stored `BOOKED` flag; it's derived by querying for overlapping bookings on the requested time window, so state can't drift out of sync.
- **Hourly/daily billing** — booking price and end time are computed from the car's rate and requested duration based on the selected billing type.
- **Domain-specific exceptions** — `CarUnavailableException`, `UserBannedException`, `BookingConflictException`, `ResourceNotFoundException`, `ResourceAlreadyExistsException`, all normalized into consistent error responses by a global exception handler.
- **API documentation** — OpenAPI/Swagger UI via springdoc.

## Tech Stack

- Java, Spring Boot (Web, Data JPA, Validation)
- PostgreSQL, Flyway (versioned schema migrations)
- MapStruct (DTO mapping), Lombok
- springdoc-openapi

## Testing

- **Unit tests** (Mockito) for service-layer logic — `BookingServiceTest`, `CarServiceTest`, `UserServiceTest`.
- **Repository integration tests** (`@DataJpaTest` + Testcontainers) against a real PostgreSQL instance, via a shared `AbstractPostgresContainerTest` base class — e.g. `BookingRepositoryIT` for the overlap query.
- Maven Failsafe plugin separates the integration test lifecycle (`*IT`) from unit tests (`*Test`), with JaCoCo for coverage reporting.

## Architecture

```
controller/api/v1/   REST endpoints (users, cars, bookings)
service/             business logic, booking validation chain
repository/          Spring Data JPA repositories
entity/              JPA entities + enums (CarStatus, UserStatus, Role)
dto/                 request/response DTOs (MapStruct-mapped)
exception/           domain exceptions + global handler
db/migration/        Flyway migration scripts (V1–V5)
```

## Running Locally

**Prerequisites:** Java, Maven, PostgreSQL, Docker (for integration tests)

1. Create a database:
   ```sql
   CREATE DATABASE car_rental;
   ```
2. Configure `src/main/resources/application.yaml` with your PostgreSQL credentials (or override via environment variables). Flyway will apply migrations on startup.
3. Run:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Run tests (unit + integration, requires Docker for Testcontainers):
   ```bash
   ./mvnw verify
   ```

API docs available at `/swagger-ui.html` once running.
