# 🚗 Car Rental Backend Service
 
> REST API for a car rental platform: vehicle catalog, bookings with automatic price calculation, authentication, and a role-based access model.
 
Built entirely in Java/Spring Boot with a layered architecture (controller → service → repository), with a strong focus on data consistency: car availability isn't stored as a flag but derived on the fly from overlapping bookings, and car status changes are protected by a pessimistic database lock.
 
## Table of Contents
 
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Data Model](#data-model)
- [API](#api)
- [Security](#security)
- [Testing](#testing)
- [Running the Project](#running-the-project)
- [Roadmap](#roadmap)
## Features
 
- **User management** — registration, authentication, current-user profile (`/me`), admin operations (ban/unban, role assignment) with paginated search.
- **Car catalog** — public search with filters (brand, model, price range); admins get a full search across all statuses with extra fields; car creation and status transitions (`AVAILABLE` / `MAINTENANCE` / `SCRAPPED`).
- **Bookings** — creation goes through a full validation chain (user not banned, car exists and is available, no overlap with existing bookings), paginated listing (optionally filtered by user), and cancellation with a cutoff once the rental period has started.
- **Availability without duplicated state** — a car's availability for a given interval is determined by querying for overlapping bookings (`findOverlappingBookings`) rather than a separate boolean flag, so status can never drift out of sync with actual bookings.
- **Hourly/daily billing** — booking price and end time are computed from the car's rate and the selected `BillingType` (hourly or daily).
- **Race condition closed at the DB level** — car status updates use a `PESSIMISTIC_WRITE` lock (`findByIdForUpdate`), preventing race conditions from concurrent requests on the same car.
- **Role hierarchy** — `USER` / `ADMIN` / `SUPER_ADMIN`, where `SUPER_ADMIN` inherits `ADMIN` privileges via `RoleHierarchy`; changing a user's role is restricted to `SUPER_ADMIN`.
- **Admin bootstrap** — a `CommandLineRunner` creates a super-admin from environment variables on first startup if no `SUPER_ADMIN` exists yet.
- **Centralized error handling** — domain exceptions (`CarUnavailableException`, `UserBannedException`, `BookingConflictException`, `ResourceNotFoundException`, `ResourceAlreadyExistsException`) are normalized by a `GlobalExceptionHandler` into consistent JSON responses, including structured validation errors.
- **Self-documenting API** — OpenAPI/Swagger UI via springdoc.
## Tech Stack
 
| Category | Technologies |
|---|---|
| Language / platform | Java 21, Spring Boot 4.1 |
| Web / API | Spring Web (MVC), springdoc-openapi (Swagger UI) |
| Data | Spring Data JPA (Hibernate), PostgreSQL, Flyway (versioned migrations) |
| Security | Spring Security, JWT (jjwt: api/impl/jackson), BCrypt, role hierarchy |
| Mapping / boilerplate | MapStruct, Lombok |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Testing | JUnit 5, Mockito, Testcontainers (PostgreSQL), `@DataJpaTest`, Maven Failsafe (unit/`*IT` split), JaCoCo (coverage) |
| Containerization | Docker (multi-stage build), Docker Compose |
| Build | Maven (`mvnw`) |
 
## Architecture
 
A layered architecture with a clear separation of concerns, and public/admin controllers kept apart:
 
```
config/                       Spring configuration (Security, OpenAPI, admin bootstrap)
controller/api/v1/            public REST endpoints (auth, cars, bookings, users)
controller/admin/v1/          admin REST endpoints (cars, users)
service/                      business logic, booking validation chain
repository/                   Spring Data JPA repositories
repository/specification/     JPA Criteria Specifications for dynamic car filtering
security/                     JWT filter, JwtService, UserDetails/UserDetailsService
entity/                       JPA entities + enums (CarStatus, UserStatus, Role)
dto/                          request/response/filter DTOs (mapped via MapStruct)
mapper/                       MapStruct entity ↔ DTO mappers
exception/                    domain exceptions + global handler
db/migration/                 Flyway migration scripts (V1–V5)
```
 
A key architectural decision is the **split between public and admin views** of the same entity: a regular user only sees available cars with a limited set of fields (`CarPublicResponse`), while an admin sees every status with an extended set of fields (`CarAdminResponse`) — implemented via separate filters (`CarSearchFilter` / `CarAdminFilter`) and separate service methods.
 
## Data Model
 
```
User        — id, email, firstName, lastName, phoneNumber, balance, login, password, role, status
Car         — id, brand, model, registrationNumber, dateRegistration, status, pricePerHour, pricePerDay
Booking     — id, user (FK), car (FK), startTime, endTime, price, cancelled
```
 
Enums: `Role (USER, ADMIN, SUPER_ADMIN)`, `UserStatus (ACTIVE, BANNED)`, `CarStatus (AVAILABLE, MAINTENANCE, SCRAPPED)`.
 
Schema evolution is tracked through Flyway migrations: booking price and a cancellation flag were added later, while an unused `condition` field on cars and a `status` field on bookings were dropped — reflecting the domain's iterative refinement.
 
## API
 
Base path: `/api/v1`. Full interactive documentation is available via Swagger UI (`/swagger-ui.html`) once the app is running.
 
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/auth/register` | public | Register a new user |
| POST | `/auth/login` | public | Authenticate, receive a JWT |
| GET | `/users/me` | authenticated | Current user's profile |
| GET | `/users/{id}` | authenticated | User profile by id |
| GET | `/users` | `ADMIN` | Paginated user list |
| GET | `/cars` | public | Search available cars with filters |
| GET | `/cars/{id}` | public | Public car details |
| GET | `/bookings` | authenticated | List bookings (own / by `userId`) |
| GET | `/bookings/{id}` | authenticated | Booking by id |
| POST | `/bookings` | authenticated | Create a booking |
| PATCH | `/bookings/{id}/cancel` | authenticated | Cancel a booking |
| GET | `/admin/cars` | `ADMIN` | Search cars across all statuses |
| GET | `/admin/cars/{id}` | `ADMIN` | Admin car details |
| POST | `/admin/cars` | `ADMIN` | Create a car |
| PATCH | `/admin/cars/{id}/status` | `ADMIN` | Change a car's status |
| GET | `/admin/users/{id}` | `ADMIN` | Admin user details |
| GET | `/admin/users` | `ADMIN` | Paginated user list (admin view) |
| PATCH | `/admin/users/{id}/ban` | `ADMIN` | Ban / unban a user |
| PATCH | `/admin/users/{id}/role` | `SUPER_ADMIN` | Change a user's role |
 
## Security
 
- Authentication is **stateless JWT**: login issues a token, and subsequent requests pass through a custom `JwtAuthFilter` placed in the Spring Security chain before `UsernamePasswordAuthenticationFilter`.
- Passwords are hashed with `BCryptPasswordEncoder`.
- Route-level authorization is configured declaratively in `SecurityConfig` (`authorizeHttpRequests`), with a role hierarchy where `SUPER_ADMIN` implies `ADMIN`.
- A banned user cannot authenticate with an otherwise valid token — the filter explicitly checks `isEnabled()` and short-circuits the chain with a `DisabledException`.
- No sessions are used (`SessionCreationPolicy.STATELESS`), and CSRF is disabled, since the API is fully token-based.
## Testing
 
The project has solid unit and integration test coverage (~1,600 lines of test code):
 
- **Unit tests** for the service layer using Mockito — `BookingServiceTest`, `CarServiceTest`, `UserServiceTest`, `AuthServiceTest`.
- **Repository integration tests** (`@DataJpaTest` + Testcontainers) against a real PostgreSQL instance, via a shared `AbstractPostgresContainerTest` base class — e.g. `BookingRepositoryIT` verifies the overlapping-bookings query.
- **Maven Failsafe** separates the unit test lifecycle (`*Test`) from integration tests (`*IT`), keeping `mvn test` fast.
- **JaCoCo** generates a coverage report during the `verify` phase.
## Running the Project
 
### With Docker Compose (recommended)
 
Only Docker is required. Create a `.env` file in the project root:
 
```env
DB_NAME=car_rental
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-me-to-a-long-random-secret
SPRING_PROFILES_ACTIVE=default
SUPER_ADMIN_BOOTSTRAP_PASSWORD=change-me
SUPER_ADMIN_BOOTSTRAP_EMAIL=admin@example.com
SUPER_ADMIN_BOOTSTRAP_FIRSTNAME=Super
SUPER_ADMIN_BOOTSTRAP_LASTNAME=Admin
SUPER_ADMIN_BOOTSTRAP_PHONENUMBER=+10000000000
```
 
Then:
 
```bash
docker compose up --build
```
 
The API will be available at `http://localhost:8080`; PostgreSQL is exposed on port `5433`.
 
### Locally
 
**Prerequisites:** Java 21, Maven, PostgreSQL, Docker (for integration tests via Testcontainers).
 
1. Create a database:
```sql
   CREATE DATABASE car_rental;
```
2. Configure the required environment variables (see `application.yaml`). Flyway will apply migrations on startup.
3. Run:
```bash
   ./mvnw spring-boot:run
```
4. Run tests (unit + integration, requires Docker for Testcontainers):
```bash
   ./mvnw verify
```
 
API docs are available at `http://localhost:8080/swagger-ui.html`.
 
## Roadmap
 
- Extended booking rules (e.g. late-cancellation penalties)
- Refresh tokens
- Explicit `@Schema` annotations for cleaner Swagger schemas
- CI pipeline (GitHub Actions) running `mvnw verify` with a JaCoCo report
 
