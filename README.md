# StayO Backend

Find Smarter, Live Better. StayO is a mobile-first PG (Paying Guest) discovery and booking platform for India. This repo is the Spring Boot REST API.

For the full, code-accurate reference (modules, endpoints, data model, quirks) see [`AI_BE_CONTEXT.md`](AI_BE_CONTEXT.md). Per-module docs live in [`docs/`](docs/README.md).

## Technology Stack

- Java 25, Spring Boot 4.1.0, Maven wrapper
- MongoDB (Spring Data MongoDB, Atlas)
- Spring Security (permissive filter chain) + manual JWT auth (jjwt 0.12.3)
- Twilio SMS for OTP (static OTP dev mode available)
- Cloudinary for all file uploads (profile images, owner documents, property images)
- springdoc-openapi 2.8.5 (Swagger UI)
- Lombok, JUnit, Mockito

## Modules

Modular monolith under `src/main/java/com/stayo/stayo`:

| Module | Purpose |
|---|---|
| `auth` | Phone-OTP login/signup, JWT, logout (token blacklist) |
| `user` | User entity, roles, profile + profile image |
| `property` | PG listings, search, owner CRUD, view tracking, nearby/recommended |
| `booking` | Booking requests; owner accept / reject / confirm-payment; user cancel |
| `owner` | Owner onboarding, verification status, owner dashboard |
| `admin` | Owner-verification review queue; admin management (super admin only) |
| `review` | PG reviews (requires a confirmed + paid booking) |
| `wishlist` | Saved PGs (stored on `User`) |
| `dashboard`, `content`, `search` | Home screen aggregation, seeded content, cities |
| `document`, `storage`, `notification` | Document records, Cloudinary storage, notifications/SMS |
| `config`, `common`, `shared` | Security/CORS/OpenAPI/seeders, health, shared DTOs + exceptions |

Roles: `USER`, `PG_OWNER`, `ADMIN`, `SUPER_ADMIN` (an account can hold several). The mobile number in `app.super-admin.mobile-number` is auto-granted `SUPER_ADMIN` on login.

## API Overview

All authenticated endpoints take `Authorization: Bearer <JWT>`. Responses use the `ApiResponse<T>` envelope (a few user/profile endpoints return raw DTOs).

| Area | Base path |
|---|---|
| Auth | `/api/auth` (`otp/send`, `otp/verify`, `update-details`, `logout`) |
| User | `/api/users/me`, `/api/user/profile`, `/api/user/dashboard` |
| Properties | `/api/properties` (search, details, owner CRUD, deactivate/reactivate, images) |
| Wishlist | `/api/wishlist` |
| Booking | `/api/booking` (user) and `/api/booking/owner/...` (owner) |
| Owner | `/api/owner` (dashboard, onboarding, documents, verify) |
| Admin | `/api/admin` (owners, admins) |
| Reviews | `/api/reviews`, `/api/properties/{pgId}/reviews` |
| Infra | `/health`, `/swagger-ui.html`, `/v3/api-docs` |

Full endpoint tables: [`AI_BE_CONTEXT.md` §6](AI_BE_CONTEXT.md) and [`docs/API/API_REFERENCE.md`](docs/API/API_REFERENCE.md).

## Configuration

`src/main/resources/application.properties`; every secret reads an env var with a dev fallback.

| Env var | Purpose |
|---|---|
| `MONGODB_URI` | MongoDB connection string |
| `JWT_SECRET` | JWT signing key |
| `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_MOBILE_NUMBER` | Twilio SMS |
| `OTP_USE_STATIC` | `true` (default) = OTP is always `123456`; **set `false` in production** |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | File storage |
| `SUPER_ADMIN_MOBILE_NUMBER` | Number auto-granted `SUPER_ADMIN` (E.164) |
| `PORT` | HTTP port (default `8082`; Docker image defaults to `8080`) |

> **Security:** `application.properties` currently contains committed fallback credentials (MongoDB URI, JWT secret, Cloudinary key/secret). Rotate them and rely on env vars only.

Fixed settings: JWT expiry 24 h, OTP expiry 5 min, max 3 OTP attempts, multipart upload limit 8 MB.

## Local Development

Prerequisites: JDK 25, a reachable MongoDB.

```powershell
$env:MONGODB_URI="mongodb://localhost:27017/stayo"
$env:OTP_USE_STATIC="true"
.\mvnw spring-boot:run     # http://localhost:8082
.\mvnw test                # run tests
.\mvnw clean package       # build jar
```

Swagger UI: http://localhost:8082/swagger-ui.html (use **Authorize** to paste a JWT).

Docker: multi-stage `Dockerfile` (`eclipse-temurin:25-jdk` build → `25-jre` runtime), serves on `$PORT` (default 8080).

## Deployment

Hosted on Render: `https://stayo-be.onrender.com` (Swagger at `/swagger-ui.html`). A keep-alive scheduler pings the service so the free-tier instance stays warm.

## Documentation Map

- [`AI_BE_CONTEXT.md`](AI_BE_CONTEXT.md) — source of truth for the backend (read first)
- [`docs/`](docs/README.md) — architecture, modules, API, database, guidelines, roadmap
- [`PRD/`](PRD/) — product requirements and frontend screen planning
