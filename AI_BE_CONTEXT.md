# StayO Backend — Technical Documentation (AI Context)

Version: 2.0
Scope: `stayo-be/` — Spring Boot REST API for the StayO PG (Paying Guest) discovery platform.

This document describes the backend **as it is actually implemented**. Use it as the source of truth when generating or modifying backend code.

---

## 1. Project Overview

StayO is a mobile-first PG discovery platform for India. Users log in with a phone OTP, browse/search PGs, view details, wishlist properties, and submit booking requests to PG owners.

The backend is a **modular monolith**: a single Spring Boot application (`com.stayo.stayo`) organized into feature packages that each own their controller, service, DTOs, entities, and repositories.

Everything models **PGs only** — no hotels, apartments, hostels, or generic property types.

---

## 2. Technology Stack

| Concern | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.0 (parent `spring-boot-starter-parent:4.1.0`) |
| Database | MongoDB (Spring Data MongoDB, Atlas cluster; DB name `stayo`) |
| Security | Spring Security (permissive filter chain) + custom JWT handling |
| JWT | `io.jsonwebtoken` jjwt 0.12.3 |
| SMS / OTP delivery | Twilio SDK 9.2.0 (with a static-OTP dev mode) |
| API docs | springdoc-openapi 2.8.5 — Swagger UI at `/swagger-ui.html`, spec at `/v3/api-docs` |
| Boilerplate | Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) |
| Build | Maven (wrapper: `mvnw` / `mvnw.cmd`) |
| Tests | JUnit + Mockito + Spring test starters (webmvc-test, security-test, mongodb-test) |
| Container | Docker multi-stage build (`eclipse-temurin:25-jdk` → `25-jre`) |
| Hosting | Render — `https://stayo-be.onrender.com` |

---

## 3. Build, Run, Test

```bash
# Run locally (Windows)
mvnw.cmd spring-boot:run

# Build jar
mvnw.cmd clean package

# Run tests
mvnw.cmd test
```

Default port: **8081** (`server.port=${PORT:8081}`, binds `0.0.0.0`). The Dockerfile runs the jar with `--server.port=${PORT:-8080}` for Render.

### Configuration (`src/main/resources/application.properties`)

All secrets resolve from environment variables with development fallbacks:

| Property | Env var | Notes |
|---|---|---|
| `spring.mongodb.uri` | `MONGODB_URI` | Atlas connection string; `auto-index-creation=true` |
| `jwt.secret` | `JWT_SECRET` | HMAC-SHA key; dev fallback baked in |
| `jwt.expiration` | — | `86400000` ms (24 h) |
| `twilio.account-sid` / `auth-token` / `phone-number` | `TWILIO_*` | Real SMS only when configured |
| `otp.expiry-minutes` | — | 5 |
| `otp.max-attempts` | — | 3 |
| `otp.static-code` / `otp.use-static` | `OTP_USE_STATIC` | Dev mode: OTP is always `123456` when `true` (default true) |
| `health.ping.url` / `health.ping.cron` | — | Self-ping config used by `HealthCheckPingService` |

---

## 4. Package / Module Structure

Base package: `com.stayo.stayo`

```
auth/          OTP login, JWT issuing/validation, logout (token blacklist)
  controller/  AuthController
  dto/         AuthResponse, LogoutResponse, OtpRequestDto, OtpVerifyRequestDto
  entity/      BlacklistedToken
  repository/  BlacklistedTokenRepository
  security/    JwtProvider
  service/     AuthService, OtpService
  util/        AuthUtil (extracts userId from Authorization header)

booking/       Booking request lifecycle
  controller/  BookingController
  dto/         BookingRequestDTO, BookingResponseDTO, OccupantDTO
  entity/      Booking, OccupantInfo
  enums/       BookingStatus, RoomType
  exception/   BookingNotFoundException, DuplicateBookingException
  mapper/      BookingMapper
  repository/  BookingRepository
  service/     BookingService + impl/BookingServiceImpl

common/        HealthController (GET /health), HealthCheckPingService (keep-alive self-ping)

config/        SecurityConfig (CORS + filter chain), WebConfig (static /uploads),
               OpenApiConfig, scheduler/KeepAliveScheduler,
               seeder/CityDataSeeder, seeder/DashboardDataSeeder

content/       Dashboard content: Banner, DashboardCategory, PopularSearch, QuickFilter
               (entity + repository + service + DTO for each)

dashboard/     Aggregation layer: DashboardController, DashboardService(Impl),
               DashboardAssembler, DashboardResponseDTO

notification/  NotificationService, SmsService (Twilio wrapper)

property/      PG domain: PGController, PG entity, PGView (view tracking),
               PGService(Impl), NearbyPGService, RecommendationService,
               PGCardDTO (list card), PGResponse (full details)

search/        City data + search DTOs: City entity, CityService(Impl),
               SearchRequest, SearchDefaultDTO, CityResponse
               (no standalone SearchController — search runs through PGController)

shared/        ApiResponse<T>, ApiError, PageResponse<T>,
               enums (Gender, GenderCategory, SearchType),
               GlobalExceptionHandler + all custom exceptions

user/          User entity, Role, OtpRequest entity,
               UserController (/api/users/me), UserProfileController,
               UserProfileService, UserRepository, OtpRepository

wishlist/      WishlistController, WishlistService(Impl)
               (no own collection — stored as wishlistPropertyIds on User)
```

**Module rules:** modules talk to each other only through services; a repository is never used outside its own module (exception: `UserController` reads `UserRepository` directly — legacy, do not copy this pattern).

---

## 5. Authentication Flow

1. `POST /api/auth/otp/send` — body `{ "mobileNumber": "+91XXXXXXXXXX" }`. Number must match E.164 (`^\+[1-9]\d{1,14}$`). OTP stored in `otp_requests` (via `OtpRepository`), delivered by Twilio, or static `123456` when `otp.use-static=true`.
2. `POST /api/auth/otp/verify` — body `{ mobileNumber, otp }`. Verifies OTP (5-min expiry, max 3 attempts). If the user exists → sign-in; otherwise a new `User` is created with `role=USER`, `phoneVerified=true`, `profileCompleted=false`. Returns `AuthResponse` (`accessToken`, `userId`, `mobileNumber`, `name`, `email`, `role`; `refreshToken` field exists but is not populated).
3. `PUT /api/auth/update-details` — completes the profile (name, email, gender, DOB, occupation, college, company, city, state, country, bio, profileImage). Once both name and email are non-blank, `profileCompleted` flips to `true`. Returns a **fresh token** that embeds name/email claims.
4. `POST /api/auth/logout` — blacklists the presented JWT in the `BlacklistedToken` collection (stored with its expiry instant).

### JWT details (`JwtProvider`)
- HS256-family HMAC, subject = userId, claims: `mobileNumber`, optional `name`, `email`.
- 24-hour expiry.
- **There is no JWT authentication filter.** `SecurityConfig` permits **all** requests (`anyRequest().permitAll()`). Authentication is enforced per-endpoint: every controller receives the raw `Authorization` header and calls `AuthUtil.extractUserIdFromToken(token)`, which validates the token and returns the userId (throwing `MissingAuthorizationException` / `InvalidTokenException` otherwise). New endpoints that need auth must follow this same pattern.

### Roles
`Role` enum on the user: `USER`, `OWNER`, `ADMIN`. There is currently **no role-based authorization enforcement** in the filter chain.

---

## 6. API Surface

All authenticated endpoints take the JWT in the `Authorization` header (Bearer format accepted; the raw token also works in most paths).

### Auth — `/api/auth`
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/otp/send` | Send OTP to mobile number |
| POST | `/api/auth/otp/verify` | Verify OTP → login/signup, returns JWT |
| PUT | `/api/auth/update-details` | Update name/email/etc., returns new JWT |
| POST | `/api/auth/logout` | Blacklist current token |

### Users — `/api/users`, `/api/user/profile`
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/users/me` | Current user (`UserResponseDto`) — returns DTO directly, no `ApiResponse` wrapper |
| GET | `/api/user/profile` | Full profile (`UserProfileResponse`) — unwrapped |
| PUT | `/api/user/profile` | Update profile fields — unwrapped |
| POST | `/api/user/profile/image` | Multipart upload (`file`); saved to local `uploads/` dir, served at `/uploads/**` via `WebConfig` |
| DELETE | `/api/user/profile/image` | Remove profile image (204) |

### Dashboard — `/api/user/dashboard`
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/user/dashboard` | Aggregated `DashboardResponseDTO`: user summary, search defaults, popular searches, hero banners, quick filters, categories, nearby PGs, recommended PGs. Throws `ProfileNotCompletedException` if profile incomplete. |

### Properties — `/api/properties`
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/properties/search` | Search + filter + paginate PGs. Query params bind to `SearchRequest`: `searchString`, `city`, `locality`, `gender` (GenderCategory), `minPrice`, `maxPrice`, `amenities`, `sortBy` (`price_asc` \| `price_desc` \| `rating_desc`), `pageNumber` (default 0), `size` (default 10). Returns `PageResponse<PGCardDTO>`. |
| GET | `/api/properties/{id}` | Full details (`PGResponse`); also records a view (`PGView`) |

### Wishlist — `/api/wishlist`
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/wishlist/add/{propertyId}` | Add PG to wishlist |
| POST | `/api/wishlist/remove/{propertyId}` | Remove PG (note: POST, not DELETE) |
| GET | `/api/wishlist` | List wishlisted PGs as `PGCardDTO`s |

### Booking — `/api/booking`
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/booking` | Create booking request (201). Duplicate active booking for the same PG → 409 `DuplicateBookingException`. |
| GET | `/api/booking?status=` | List user bookings, optional `BookingStatus` filter |
| GET | `/api/booking/{bookingId}` | Single booking (ownership checked) |
| DELETE | `/api/booking/{bookingId}` | Cancel — only allowed while status is `PENDING_OWNER` |

### Infra
| Method | Path | Purpose |
|---|---|---|
| GET | `/health` | Plain `"OK"` — used by keep-alive pings |
| GET | `/swagger-ui.html`, `/v3/api-docs` | API documentation |

---

## 7. Response & Error Conventions

### Success envelope — `shared/dto/ApiResponse<T>`
```json
{
  "status": 200,
  "success": true,
  "message": "Properties retrieved successfully",
  "data": { ... },
  "timestamp": "2026-07-16T10:15:30"
}
```
Builders: `ApiResponse.success(data, message)`, `ApiResponse.success(status, data, message)`, `ApiResponse.error(status, message)`.

⚠️ Exceptions to the envelope: `UserController.getCurrentUser`, all `UserProfileController` endpoints, and `/health` return raw DTOs/strings. New endpoints should use `ApiResponse`.

### Pagination — `shared/dto/PageResponse<T>`
`content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`.

### Errors — `GlobalExceptionHandler` (`@RestControllerAdvice`)
Custom exceptions map to `ApiError { status, error, message, path }`:

| Exception | HTTP |
|---|---|
| `InvalidMobileNumberException` | 400 |
| `InvalidOtpException`, `OtpExpiredException` | 400-range |
| `OtpNotFoundException`, `UserNotFoundException`, `PropertyNotFoundException`, `BookingNotFoundException` | 404 |
| `MaxOtpAttemptsExceededException` | 429-range |
| `MissingAuthorizationException`, `InvalidTokenException` | 401 |
| `ProfileNotCompletedException` | 4xx (dashboard gate) |
| `DuplicateBookingException` | 409 |
| Bean-validation failures (`MethodArgumentNotValidException`, `ConstraintViolationException`) | 400 with collected field messages |

New failure modes ⇒ create a custom exception + a handler here. Never return raw stack traces.

---

## 8. Data Model (MongoDB collections)

| Collection | Entity | Key fields / indexes |
|---|---|---|
| `users` | `user/entity/User` | name, email, mobileNumber, gender, dateOfBirth, occupation, college, company, city/state/country, bio, profileImage, role, phoneVerified, profileCompleted, `wishlistPropertyIds: List<String>`, audit timestamps |
| `properties` | `property/entity/PG` | pgName, description, city*, locality*, address, genderCategory (`GenderCategory`: BOYS/GIRLS/UNISEX), rent, amenities[], images[], rating, reviewCount, isFeatured*, isActive*, ownerId*; compound index `(isFeatured, isActive)` (* = indexed) |
| `bookings` | `booking/entity/Booking` | userId*, pgId*, denormalized pgName/pgLocality/pgCity/pgOwnerId, roomType (SINGLE/DOUBLE), moveInDate, minimumStay, occupantCount (1–4), primaryOccupant + extraOccupants (`OccupantInfo`), specialNote, financial snapshot (monthlyRent, securityDeposit, maintenanceFee, totalPayable), status*; compound indexes `(userId,status,createdAt)` and `(pgId,status)` |
| `blacklisted_tokens` | `auth/entity/BlacklistedToken` | token, expiryDate |
| otp requests | `user/entity/OtpRequest` | mobileNumber, otp, expiry, attempt count |
| pg views | `property/entity/PGView` | user→PG view tracking (recently viewed / recommendations) |
| cities | `search/entity/City` | seeded by `CityDataSeeder` |
| banners / categories / popular searches / quick filters | `content/entity/*` | dashboard content, seeded by `DashboardDataSeeder` |

`BookingStatus` lifecycle: `PENDING_OWNER → OWNER_ACCEPTED | OWNER_REJECTED`, user-side `CANCELLED` (only from PENDING_OWNER), `CONFIRMED` reserved for post-payment.

Booking deliberately **denormalizes** PG display fields and snapshots pricing at creation time — keep this pattern when extending it.

---

## 9. Background Jobs & Seeders

- `config/scheduler/KeepAliveScheduler` — every 4 minutes POSTs a dummy OTP request to the **production** Render URL to keep the free-tier dyno warm.
- `common/service/HealthCheckPingService` — cron self-ping (`health.ping.*`) against `/health`.
- `config/seeder/CityDataSeeder`, `config/seeder/DashboardDataSeeder` — populate cities and dashboard content (banners, categories, quick filters, popular searches) on startup if missing. **Never hardcode dashboard content in services — it comes from these seeded collections.**

---

## 10. Cross-cutting Standards

- **Controllers**: validate (`@Valid` + jakarta annotations), extract userId via `AuthUtil`, delegate to service, wrap in `ApiResponse`. No business logic.
- **Services**: business logic, orchestration, exception throwing. Interface + `impl/` class for booking/dashboard/property/search/wishlist; plain classes elsewhere.
- **Repositories**: Spring Data `MongoRepository` interfaces only. No logic, no DTOs.
- **DTO in / DTO out** — entities never cross the API boundary (`PGCardDTO` for lists, `PGResponse` for detail, etc.).
- **Logging**: SLF4J via Lombok `@Slf4j`; log auth events, business events, errors. Never `System.out.println`.
- **Validation**: annotation-based (`@NotBlank`, `@NotNull`, `@Min/@Max`, `@Size`, `@FutureOrPresent`…), never manual checks in controllers.
- **Naming**: `XController`, `XService`/`XServiceImpl`, `XRepository`, `XRequestDTO`/`XResponseDTO`. No abbreviations.
- **CORS** (in `SecurityConfig`): allowed origins `https://stay-o-frontend.vercel.app`, `http://localhost:3000`, `http://localhost:5173`, `https://*.devtunnels.ms`; credentials allowed; `Authorization` header exposed.

---

## 11. Testing

Tests live under `src/test/java/com/stayo/stayo/`:
- `auth/controller/AuthControllerTest`
- `dashboard/controller/DashboardControllerTest`, `dashboard/service/impl/DashboardServiceImplTest`
- `property/controller/PGControllerTest`
- `common/service/HealthCheckPingServiceTest`

Pattern: MockMvc slice tests for controllers, Mockito unit tests for services. New features should ship with matching tests.

---

## 12. Known Quirks / Watch-outs (do not "fix" silently)

1. **Security chain is `permitAll`** — auth is manual per-controller via `AuthUtil`. Adding a proper JWT filter is a deliberate architectural change, not a drive-by fix.
2. **Token blacklist is not consulted on requests** — logout blacklists the token, but `AuthUtil`/`JwtProvider` validation paths must be checked before assuming blacklisted tokens are rejected everywhere.
3. **`refreshToken` in `AuthResponse` is never populated** — refresh flow is future work.
4. Wishlist remove uses **POST** `/api/wishlist/remove/{id}`, not DELETE — the frontend depends on this; changing it is a breaking API change.
5. Profile image upload stores files on **local disk** (`uploads/`) — ephemeral on Render; a cloud storage migration is future work.
6. Some endpoints bypass the `ApiResponse` envelope (see §7) — frontend already parses both shapes.
7. Dev MongoDB URI and JWT secret fallbacks are committed in `application.properties` — production must override via env vars.

---

## 13. Future / Planned (not implemented — do not build unless asked)

Owner portal APIs, admin module, reviews, payments/`CONFIRMED` booking flow, refresh tokens, Google/Apple login, Redis caching, ElasticSearch-backed search, Firebase push notifications, role-based authorization.

---

## 14. AI Development Rules

1. Search the project for existing classes before creating new ones — no duplicate DTOs, services, repositories, or utilities.
2. Respect module boundaries: cross-module access only through services.
3. Follow the manual-auth pattern (`AuthUtil.extractUserIdFromToken`) for any authenticated endpoint.
4. Wrap new responses in `ApiResponse<T>`; paginate with `PageResponse<T>`.
5. Add a custom exception + `GlobalExceptionHandler` entry for new failure modes.
6. Keep the API backward compatible — the deployed Vercel frontend consumes it directly.
7. Never hardcode business data; use seeders/config.
8. Ensure `mvnw clean package` succeeds after every change; add/maintain tests.
