# EMS — Event Management System

A backend API for running events end-to-end: user auth, event creation, team formation, registrations, and paid entries via Razorpay. Built with **Dropwizard** (Java) and **PostgreSQL**.

## Tech Stack

- **Framework:** Dropwizard (core, hibernate, auth)
- **Database:** PostgreSQL + Hibernate (JPA)
- **Auth:** JWT (`java-jwt`) + BCrypt (`jbcrypt`) password hashing
- **Payments:** Razorpay Java SDK (orders, verification, webhooks)
- **Email:** Jakarta Mail (SMTP)
- **SMS:** Twilio Java SDK (OTP delivery)
- **RateLimiter:** Bucket4j
- **Logging:** SLF4J + Logback, with request-scoped tracing via MDC
- **Build:** Maven, with Spotless, Checkstyle, and PMD wired in for code quality
- **Containerization:** Docker Compose (Postgres)

## Features

**Auth & Verification**
- Register / login with JWT-based sessions
- Email OTP (SMTP) and phone OTP (Twilio SMS) verification flows, pluggable behind a common `SmsService` interface (a `ConsoleSmsService` is also available for local dev without Twilio credentials)
- Role-based access control: `USER` < `MODERATOR` < `ADMIN`, enforced via `@RolesAllowed` and a custom `RoleAuthorizer`

**Events**
- Create, update, publish, cancel, and complete events
- Solo or team-based events, public/private visibility, registration fees, team size limits, registration deadlines
- Draft → Published → Completed/Cancelled lifecycle

**Teams**
- Create teams per event, transfer ownership, remove members
- Join requests and invitations, each with accept/reject flows, guarded by optimistic locking (`@Version`) and tracked member counts to prevent race conditions on concurrent join attempts
- Per-user endpoints (`/api/teams/me`, `/api/team-requests/me`) for a user's own teams and pending requests across events

**Registrations & Payments**
- Register for an event solo or as a team
- Razorpay order creation, payment verification (signature-checked), failure handling, and webhook support for `payment.captured` / `payment.failed`
- Registration/payment status kept in sync (`PENDING → REGISTERED/COMPLETED`, etc.)

**Observability**
- Request-scoped tracing: every request gets an `X-Request-ID` (client-supplied or generated), propagated through logs via SLF4J MDC and echoed back in the response header
- Structured SLF4J logging across resources, services, and DAOs for action-level traceability

**Admin**
- Manage users (create, update, change role, delete), search by email/phone
- Manage teams (view members, transfer ownership, remove members, delete team)
- View all events across the system
- Bootstrap admin account auto-created on startup from config

## Getting Started

### Prerequisites
- Java 17+ and Maven
- Docker (for Postgres) or a local Postgres instance
- A Razorpay test account (for payment features)

### 1. Start the database
```bash
docker compose up -d
```
This starts Postgres on `localhost:5433` (db: `ems`, user/pass: `postgres`/`postgres`).

### 2. Configure the app
Edit `config.yml` — at minimum set real values for:
- `razorpay.keyId` / `razorpay.keySecret` / `razorpay.webhookSecret`
- `jwt.secret` (use a strong secret outside of local dev)
- `emailservice.username` / `password` (SMTP credentials)
- `twilio.accountSid` / `authToken` / `fromPhoneNumber` (for phone OTP SMS)
- `bootstrapadmin.*` (initial admin account)

### 3. Build and run
```bash
mvn clean install
java -jar target/event-management-system-1.0-SNAPSHOT.jar server config.yml
```

- App: `http://localhost:8080`
- Health check: `http://localhost:8081/healthcheck`

### Code quality checks
```bash
./scripts/check.sh
```
Runs Spotless formatting, compile, tests, Checkstyle, and PMD.

## API Overview

| Area | Base path | Notes |
|---|---|---|
| Auth | `/api/auth` | register, login, OTP verify/generate, `/me` |
| Events | `/api/events` | CRUD + publish/cancel/complete (MODERATOR+) |
| Teams | `/api/events/{eventId}/teams` | create/update/delete, membership, ownership transfer |
| Team requests | `/api/events/{eventId}/teams/{teamId}/requests`, `/invitations` | join requests & invites |
| User teams | `/api/teams/me`, `/api/team-requests/me` | current user's teams & pending requests |
| Registrations | `/api/events/{eventId}/registrations` | register/cancel |
| Payments | `/api/payments` | order creation, verification, failure |
| Webhooks | `/webhooks/razorpay` | Razorpay payment webhooks |
| Admin | `/api/admin` | user & team management (ADMIN only) |

### Swagger / OpenAPI Docs

- Swagger home: `http://localhost:8080/swagger`
- Admin docs: `http://localhost:8080/swagger/admin`
- Moderator docs: `http://localhost:8080/swagger/moderator`
- User docs: `http://localhost:8080/swagger/user`

## Project Structure
```
src/main/java/com/rupesh/ems/
├── api/          # Request/response DTOs, grouped by domain
├── auth/         # JWT authenticator, role authorizer, user principal
├── configs/      # Dropwizard config bindings (JWT, email, Razorpay, bootstrap admin)
├── core/         # JPA entities and enums
├── db/           # DAOs (Hibernate-based data access)
├── exceptions/   # Custom API exceptions
├── logging/      # Request-ID filter (MDC-based tracing)
├── mappers/      # JAX-RS exception mappers → consistent error responses
├── resources/    # JAX-RS REST endpoints (controllers)
└── service/      # Business logic (incl. sms/ for SmsService implementations)
```

## Roadmap / Not Yet Implemented

Things still on the todo list for this project:

- **Event search/filtering & pagination** — `getAllEvents` / `getVisibleEvents` currently return unpaginated full lists; needs query params for search, filters (type, status, date range), and pagination.
- **Refresh tokens** — auth currently issues a single JWT with a fixed expiry; no refresh/rotation flow yet.
- **File uploads** — no support yet for event banners/images or user avatars.
- **Notifications** — no email/notification triggers yet for event publish, registration confirmation, or team invites (email service exists but isn't wired into these flows).

Contributions and issues welcome once the repo is public-facing.
