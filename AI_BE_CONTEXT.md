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
| File storage | Cloudinary (`cloudinary-http44` SDK) — profile images, owner KYC documents, property images all stored in the cloud, not on local disk |
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

Default port: **8082** (`server.port=${PORT:8082}`, binds `0.0.0.0`). The Dockerfile runs the jar with `--server.port=${PORT:-8080}` for Render.

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
| `health.ping.url` / `health.ping.cron` | — | Self-ping config used by `HealthCheckPingService` (cron `0 */5 * * * *`) |
| `app.super-admin.mobile-number` | `SUPER_ADMIN_MOBILE_NUMBER` | The one number auto-granted `Role.SUPER_ADMIN` on every OTP login (see §5 Roles). Default `+919689104033` |
| `cloudinary.cloud-name` / `api-key` / `api-secret` | `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Cloud file storage; dev fallback points at Cloudinary's public `demo` cloud (uploads will fail without real credentials, same as Twilio) |
| `server.compression.*` / `app.compression.zstd-enabled` / `app.compression.zstd-level` | `ZSTD_ENABLED` (default `true`; `false` removes the zstd filter, gzip stays) | JSON responses ≥1024 B: zstd (level 3) via `config/ZstdCompressionFilter` when the client sends `Accept-Encoding: zstd`; otherwise gzip via Tomcat. Tomcat skips responses that already have `Content-Encoding`, so no double compression. |
| `spring.servlet.multipart.max-file-size` / `max-request-size` | — | `8MB` / `8MB` — stays under Cloudinary's free-tier ~10MB/file cap |

---

## 4. Package / Module Structure

Base package: `com.stayo.stayo`

```
admin/         Internal admin panel API: owner-verification review queue + admin management
  controller/  AdminController
  dto/         AddAdminRequestDTO, AdminUserSummaryDTO
  exception/   SuperAdminAccessRequiredException (403)
  service/     AdminService + impl/AdminServiceImpl (addAdmin, listAdmins — role checks live here)

auth/          OTP login, JWT issuing/validation, logout (token blacklist)
  controller/  AuthController
  dto/         AuthResponse, LogoutResponse, OtpRequestDto, OtpVerifyRequestDto
  entity/      BlacklistedToken
  repository/  BlacklistedTokenRepository
  security/    JwtProvider
  service/     AuthService, OtpService
  util/        AuthUtil (extracts userId from Authorization header)

booking/       Booking request lifecycle
  config/      BookingIndexInitializer (partial unique index at startup)
  controller/  BookingController
  dto/         BookingRequestDTO, BookingResponseDTO, BookingRejectRequestDTO, OccupantDTO
  entity/      Booking, OccupantInfo
  enums/       BookingStatus, MinimumStay, RoomType
  exception/   BookingNotFoundException, DuplicateBookingException,
               InvalidBookingRequestException, InvalidBookingStateException
  mapper/      BookingMapper
  repository/  BookingRepository
  service/     BookingService + impl/BookingServiceImpl

common/        HealthController (GET /health), HealthCheckPingService (keep-alive self-ping)

config/        SecurityConfig (CORS + filter chain), CloudinaryConfig (Cloudinary client bean),
               OpenApiConfig, scheduler/KeepAliveScheduler,
               seeder/CityDataSeeder, seeder/DashboardDataSeeder

content/       Dashboard content: Banner, DashboardCategory, PopularSearch, QuickFilter
               (entity + repository + service + DTO for each)

dashboard/     Aggregation layer: DashboardController, DashboardService(Impl),
               DashboardAssembler, DashboardResponseDTO

document/      Generic verification-document storage (separate from the owner-onboarding
               document upload, which lives on OwnerProfile.documents directly)
  dto/         DocumentResponseDTO
  entity/      Document
  enums/       DocType (AADHAR, PAN, OTHER, ...)
  repository/  DocumentRepository
  service/     DocumentService + impl/DocumentServiceImpl — stores files via the shared
               `storage` module (Cloudinary)

notification/  NotificationService (booking events: requested/accepted/rejected/payment-confirmed), SmsService (Twilio wrapper)

owner/         PG owner business-profile onboarding/verification + dashboard aggregation
               (property CRUD itself lives in the `property` module)
  controller/  OwnerController
  dto/         OwnerOnboardingRequestDTO, OwnerProfileResponseDTO, OwnerVerificationRequestDTO,
               OwnerDashboardResponseDTO, MonthlyRevenuePointDTO
  entity/      OwnerProfile
  enums/       VerificationStatus (PENDING/VERIFIED/REJECTED)
  exception/   OwnerProfileNotFoundException, OwnerAlreadyOnboardedException, InvalidVerificationRequestException,
               OwnerNotVerifiedException
  repository/  OwnerProfileRepository
  service/     OwnerProfileService + impl/OwnerProfileServiceImpl,
               OwnerDashboardService + impl/OwnerDashboardServiceImpl

property/      PG domain: PGController, PG entity, PGView (view tracking),
               PGService(Impl), NearbyPGService, RecommendationService,
               PGCardDTO (list card), PGResponse (full details), PropertyRequestDTO (owner create/update),
               exception/PropertyAccessDeniedException (403, ownership mismatch on update/deactivate/reactivate/image upload)

review/        PG reviews — ⚠️ backend-complete but currently unreachable, see §13
  controller/  ReviewController
  dto/         ReviewRequestDTO, ReviewResponseDTO
  entity/      PGReview
  exception/   DuplicateReviewException (one review per user/PG/booking), ReviewNotEligibleException
  repository/  PGReviewRepository
  service/     ReviewService + impl/ReviewServiceImpl — requires the caller's booking to be
               `BookingStatus.CONFIRMED` + `PaymentStatus.PAID` (see `booking/enums/PaymentStatus`);
               on success calls `PGService.recordReview` to update the PG's rating/reviewCount

search/        City data + search DTOs: City entity, CityService(Impl),
               SearchRequest, SearchDefaultDTO, CityResponse
               (no standalone SearchController — search runs through PGController)

shared/        ApiResponse<T>, ApiError, PageResponse<T>,
               enums (Gender, GenderCategory, SearchType),
               GlobalExceptionHandler + all custom exceptions

storage/       Shared cloud file-storage abstraction — consumed by user/document/property
               modules for all uploads (profile images, KYC documents, property images)
  dto/         StoredFile (url + Cloudinary public_id)
  service/     FileStorageService + impl/CloudinaryFileStorageService

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
2. `POST /api/auth/otp/verify` — body `{ mobileNumber, otp, viaOwnerOnboarding }`. Verifies OTP (5-min expiry, max 3 attempts). If the user exists → sign-in (their `roles` are never touched here — see Roles below). Otherwise a new `User` is created with `phoneVerified=true`, `profileCompleted=false`, and an initial `roles` list of exactly one role: `[PG_OWNER]` if `viaOwnerOnboarding=true` (the frontend's "Become an Owner" entry point sends this), else `[USER]` (the default, standard Login flow). Returns `AuthResponse` (`accessToken`, `userId`, `mobileNumber`, `name`, `email`, `roles`, `dualRoleAvailable`; `refreshToken` field exists but is not populated).
3. `PUT /api/auth/update-details` — completes the profile (name, email, gender, DOB, occupation, college, company, city, state, country, bio, profileImage). Once both name and email are non-blank, `profileCompleted` flips to `true`. Returns a **fresh token** that embeds name/email claims.
4. `POST /api/auth/logout` — blacklists the presented JWT in the `BlacklistedToken` collection (stored with its expiry instant).

### JWT details (`JwtProvider`)
- HS256-family HMAC, subject = userId, claims: `mobileNumber`, optional `name`, `email`.
- 24-hour expiry.
- **There is no JWT authentication filter.** `SecurityConfig` permits **all** requests (`anyRequest().permitAll()`). Authentication is enforced per-endpoint: every controller receives the raw `Authorization` header and calls `AuthUtil.extractUserIdFromToken(token)`, which validates the token and returns the userId (throwing `MissingAuthorizationException` / `InvalidTokenException` otherwise). New endpoints that need auth must follow this same pattern.

### Roles
`User.roles` is a `List<Role>` — an account can hold more than one of `USER`, `PG_OWNER`, `ADMIN`, `SUPER_ADMIN` at once (there is no separate singular "role" field; it was removed). Becoming an owner (`POST /api/owner/onboarding`, first submission) **appends** `PG_OWNER` to this list without removing any role already present (e.g. `USER`) — see `OwnerProfileServiceImpl.submitOnboarding`. `User.ensureRolesInitialized()` is a defensive-only guard (seeds `[USER]` if the list is ever empty); every real code path that creates a `User` sets `roles` explicitly.

`AuthResponse.dualRoleAvailable` (`AuthService.canChooseRole`) is `true` whenever `roles.contains(PG_OWNER)` — **not** only when an account literally holds both `USER` and `PG_OWNER`. A `PG_OWNER`-only account (e.g. one that signed up straight through "Become an Owner" and never separately holds `USER`) still gets this flag, because nothing in the app gates ordinary browsing/booking behind the `USER` role specifically — a `PG_OWNER` account can always additionally act as a plain tenant. This flag drives the frontend's post-login role-picker screen (`/choose-role`).

**Admin roles.** `SUPER_ADMIN` implicitly includes every `ADMIN` capability: `User.isAdmin()` is true for either role, `User.isSuperAdmin()` only for `SUPER_ADMIN`. `AuthService.ensureSuperAdminRole` adds `SUPER_ADMIN` on every login (new or existing account) when the user's mobile number equals `app.super-admin.mobile-number` — idempotent, no manual DB edit needed. The super admin grants `ADMIN` to other numbers via `POST /api/admin/admins` (creates a stub `User` with `phoneVerified=false` if the number has no account yet). There is no endpoint to revoke admin access.

There is still **no role-based authorization enforcement in the filter chain** (`permitAll()`). Role checks are done manually in the service layer: `OwnerProfileService.verifyOwnerProfile`/`listByStatus` and `AdminService.listAdmins` require `isAdmin()` (else `AdminAccessRequiredException`, 403); `AdminService.addAdmin` requires `isSuperAdmin()` (else `SuperAdminAccessRequiredException`, 403). No admin UI exists yet.

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
| POST | `/api/user/profile/image` | Multipart upload (`file`); stored via Cloudinary, returns an absolute HTTPS URL |
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
| GET | `/api/properties/owner/mine` | List PGs owned by the caller (`List<PGResponse>`) |
| POST | `/api/properties` | Create a PG listing. Requires an **approved** `OwnerProfile` (403 `OwnerNotVerifiedException` otherwise) — see the `owner` module. `ownerId` is set to the caller. |
| PUT | `/api/properties/{id}` | Update a PG. 403 `PropertyAccessDeniedException` if the caller isn't `pg.ownerId`. |
| PATCH | `/api/properties/{id}/deactivate` | Soft-delete (`isActive=false`) — no hard delete, since bookings reference PGs. Ownership-checked. |
| PATCH | `/api/properties/{id}/reactivate` | Undo a deactivation (`isActive=true`). Ownership-checked. |
| POST | `/api/properties/{id}/images` | Multipart image upload; appends to `images`. Ownership-checked. Stored via Cloudinary, returns an absolute HTTPS URL, same as profile images. |

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
| GET | `/api/booking/owner?status=` | List booking requests for PGs owned by authenticated owner |
| PATCH | `/api/booking/owner/{bookingId}/accept` | Accept a pending booking request as the PG owner |
| PATCH | `/api/booking/owner/{bookingId}/reject` | Reject a booking (optional body `{ "reason": "..." }`) |
| PATCH | `/api/booking/owner/{bookingId}/confirm-payment` | Manual stopgap for a payment gateway: owner marks an `OWNER_ACCEPTED` booking as paid → `status=CONFIRMED`, `paymentStatus=PAID`, notifies the user. Other states → 409 `InvalidBookingStateException`. |

### Owner — `/api/owner`
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/owner/dashboard` | Aggregated `OwnerDashboardResponseDTO`: `totalProperties`, `activeProperties`, `occupiedRoomsEstimate` (count of `OWNER_ACCEPTED` bookings across owned PGs), `pendingRequestsCount` (`PENDING_OWNER` bookings), `monthlyRevenueEstimate` (sum of `monthlyRent` for `OWNER_ACCEPTED` bookings), `todaysViews` (`PGView` count for owned PGs today), `revenueTrend` (trailing 6 months, by booking `createdAt` month — a proxy, not a real ledger). Fetches properties + bookings in parallel via `CompletableFuture`, mirroring `DashboardServiceImpl`'s pattern. |
| POST | `/api/owner/onboarding` | Submit (or resubmit after rejection) PG owner business + bank details. Adds `PG_OWNER` to the caller's `roles` list (without removing any existing role) and sets `verificationStatus=PENDING`. 409 if already `PENDING`/`VERIFIED`. |
| GET | `/api/owner/onboarding/status` | Get the caller's `OwnerProfile` + verification status. 404 if never submitted. |
| POST | `/api/owner/onboarding/documents` | Multipart upload of a verification document (Aadhaar/PAN/electricity bill/rental agreement/property images); appends the URL to `documents`. Stored via Cloudinary, returns an absolute HTTPS URL, same as profile images. |
| PATCH | `/api/owner/onboarding/{targetUserId}/verify` | Approve/reject a submission. Requires `ADMIN` or `SUPER_ADMIN` (403 `AdminAccessRequiredException` otherwise). Body `{ status: VERIFIED\|REJECTED, rejectionReason }` — reason required when rejecting. |

### Admin — `/api/admin`
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/admin/owners?status=` | List owner onboarding submissions by `VerificationStatus` (default `PENDING`) as `OwnerProfileResponseDTO`s. Requires ADMIN/SUPER_ADMIN. |
| POST | `/api/admin/admins` | Body `{ mobileNumber }` (E.164). Grants `ADMIN` to that number (201, `AdminUserSummaryDTO`). Requires `SUPER_ADMIN`. |
| GET | `/api/admin/admins` | List every account holding `ADMIN` or `SUPER_ADMIN`. Requires ADMIN/SUPER_ADMIN. |

### Reviews — `/api/reviews`, `/api/properties/{pgId}/reviews`
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/reviews` | Submit a review (`pgId`, `bookingId`, `rating`, `review`). Requires the booking to be `BookingStatus.CONFIRMED` + `PaymentStatus.PAID` — reachable via the owner's manual `confirm-payment` endpoint (see §8). 409 `DuplicateReviewException` if already reviewed; `ReviewNotEligibleException` if the booking/PG mismatch or isn't confirmed+paid. |
| GET | `/api/properties/{pgId}/reviews` | List reviews for a PG (public, no auth) |

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
| `InvalidBookingRequestException` | 400 (occupant count mismatch, room type unavailable, invalid decision) |
| `InvalidBookingStateException` | 409 (e.g., cancel non-PENDING booking, respond to non-PENDING booking) |
| `OwnerProfileNotFoundException` | 404 (no onboarding submission exists yet) |
| `OwnerAlreadyOnboardedException` | 409 (resubmitting while `PENDING`/`VERIFIED`) |
| `InvalidVerificationRequestException` | 400 (rejecting without a `rejectionReason`) |
| `OwnerNotVerifiedException` | 403 (creating a property without an approved `OwnerProfile`) |
| `PropertyAccessDeniedException` | 403 (update/deactivate/image-upload on a PG the caller doesn't own) |
| `AdminAccessRequiredException` | 403 (owner verification / admin listing without ADMIN or SUPER_ADMIN) |
| `SuperAdminAccessRequiredException` | 403 (`POST /api/admin/admins` without `SUPER_ADMIN`) |
| Bean-validation failures (`MethodArgumentNotValidException`, `ConstraintViolationException`) | 400 with collected field messages |

New failure modes ⇒ create a custom exception + a handler here. Never return raw stack traces.

---

## 8. Data Model (MongoDB collections)

| Collection | Entity | Key fields / indexes |
|---|---|---|
| `users` | `user/entity/User` | name, email, mobileNumber, gender, dateOfBirth, occupation, college, company, city/state/country, bio, profileImage, `roles: List<Role>` (USER/PG_OWNER/ADMIN/SUPER_ADMIN, any combination — no singular "role" field), phoneVerified, profileCompleted, `wishlistPropertyIds: List<String>`, audit timestamps |
| `properties` | `property/entity/PG` | pgName, description, city*, locality*, address, genderCategory (`GenderCategory`: BOYS/GIRLS/UNISEX), rent (display price), `rentByRoomType: Map<RoomType, Double>` (per-sharing rent used at booking), `securityDeposit`, amenities[], images[], rating, reviewCount, isFeatured*, isActive*, ownerId*; compound index `(isFeatured, isActive)` (* = indexed) |
| `bookings` | `booking/entity/Booking` | userId*, pgId*, denormalized pgName/pgLocality/pgCity/pgOwnerId, roomType (SINGLE/DOUBLE), moveInDate, minimumStay (THREE_MONTHS/SIX_MONTHS/TWELVE_MONTHS), occupantCount (1–4), primaryOccupant + extraOccupants (`OccupantInfo`), specialNote, `rejectionReason` (populated only on OWNER_REJECTED), financial snapshot (monthlyRent, securityDeposit, totalPayable), status*; compound indexes `(userId,status,createdAt)` and `(pgId,status)`; partial unique index `(userId,pgId)` where status not in [CANCELLED, OWNER_REJECTED] |
| `owner_profiles` | `owner/entity/OwnerProfile` | userId* (unique), businessName, gstNumber, panNumber, bankAccountName, bankAccountNumber (masked as `maskedBankAccountNumber` in responses — never returned raw), bankIfsc, bankName, documents: List<String>, verificationStatus (PENDING/VERIFIED/REJECTED), rejectionReason, submittedAt, reviewedAt |
| pg reviews | `review/entity/PGReview` | pgId, userId, bookingId, rating, review, createdAt, updatedAt; unique per (userId, pgId, bookingId). Requires a CONFIRMED+PAID booking (see §8) |
| documents | `document/entity/Document` | userId, docType (`DocType`), file URL; generic verification-document store, separate from `OwnerProfile.documents` |
| `blacklisted_tokens` | `auth/entity/BlacklistedToken` | token, expiryDate |
| otp requests | `user/entity/OtpRequest` | mobileNumber, otp, expiry, attempt count |
| pg views | `property/entity/PGView` | user→PG view tracking (recently viewed / recommendations) |
| cities | `search/entity/City` | seeded by `CityDataSeeder` |
| banners / categories / popular searches / quick filters | `content/entity/*` | dashboard content, seeded by `DashboardDataSeeder` |

`BookingStatus` lifecycle: `PENDING_OWNER → OWNER_ACCEPTED | OWNER_REJECTED`, user-side `CANCELLED` (only from PENDING_OWNER), `OWNER_ACCEPTED → CONFIRMED` via the owner's `PATCH /api/booking/owner/{id}/confirm-payment` (also sets `paymentStatus=PAID`). Owner accept/reject: `PATCH /api/booking/owner/{id}/accept|reject`.

`PaymentStatus` (`booking/enums/PaymentStatus`): `PENDING`, `PAID`, `FAILED`, `REFUNDED`. **No payment gateway exists** — `PAID` is only set by the owner's manual `confirm-payment` call (a stopgap); `FAILED`/`REFUNDED` are never set. The `review` module's eligibility check (`ReviewServiceImpl`) requires `CONFIRMED`+`PAID`, so reviews work end-to-end once an owner confirms payment.

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
- `auth/service/AuthServiceTest` — Mockito unit test (multi-role login flow: entry-point-based initial role on signup, roles never overwritten at login, `dualRoleAvailable` true whenever `PG_OWNER` is present including `PG_OWNER`-only accounts)
- `booking/controller/BookingControllerTest` — Integration test (create, duplicate, cancel, get by ID, list)
- `booking/service/impl/BookingServiceImplTest` — Mockito unit test (pricing math, duplicates, ownership, cancel/accept/reject state machine, notifications)
- `dashboard/controller/DashboardControllerTest`, `dashboard/service/impl/DashboardServiceImplTest`
- `admin/` — no tests yet (`AdminService`, `AdminController`, super-admin grant in `AuthService`, `listByStatus` are untested)
- `property/controller/PGControllerTest` — includes owner property CRUD (create/update/deactivate/list-mine, ownership & verification checks)
- `property/service/impl/PGServiceImplTest` — Mockito unit test for the owner CRUD methods
- `common/service/HealthCheckPingServiceTest`
- `owner/controller/OwnerControllerTest` — Integration test (submit onboarding, duplicate/rejected resubmission, status, verify incl. non-admin rejection, dashboard aggregation)
- `owner/service/impl/OwnerProfileServiceImplTest` — Mockito unit test (role flip, resubmission after rejection, masking, verify validation incl. `AdminAccessRequiredException`)
- `owner/service/impl/OwnerDashboardServiceImplTest` — Mockito unit test (stat aggregation, revenue trend, zeroed-empty case)
- `user/service/UserProfileServiceTest` — Mockito unit test (profile image upload/re-upload/delete via mocked `FileStorageService`)
- `document/service/impl/DocumentServiceImplTest` — Mockito unit test (upload via mocked `FileStorageService`, empty-file guard, list-for-user)
- `property/service/impl/PGServiceImplTest` — also covers `uploadPropertyImage` (cover-image/sort-order, `MAX_IMAGES_PER_PG` cap, ownership check, via mocked `FileStorageService`)

Pattern: `@SpringBootTest` integration tests for controllers (direct controller invocation, not MockMvc), Mockito unit tests for services. New features should ship with matching tests. `CloudinaryFileStorageService` itself is intentionally not unit-tested (thin SDK wrapper — verify manually against a real Cloudinary account instead); all callers mock the `FileStorageService` interface.

---

## 12. Known Quirks / Watch-outs (do not "fix" silently)

1. **Security chain is `permitAll`** — auth is manual per-controller via `AuthUtil`. Adding a proper JWT filter is a deliberate architectural change, not a drive-by fix.
2. **Token blacklist is consulted in `AuthUtil.extractUserIdFromToken`** (`existsByToken` → `InvalidTokenException`), so blacklisted tokens are rejected on every endpoint using it. It is the only enforcement point — no filter.
3. **`refreshToken` in `AuthResponse` is never populated** — refresh flow is future work.
4. Wishlist remove uses **POST** `/api/wishlist/remove/{id}`, not DELETE — the frontend depends on this; changing it is a breaking API change.
5. File uploads (profile images, KYC documents, property images) now go through Cloudinary via the `storage` module — **any record whose stored URL still starts with `/uploads/` predates this migration and is a permanently dead link** (the local files were on Render's ephemeral disk and are gone). No backfill/re-upload path exists yet; sizing and fixing this is a deliberate follow-up, not done here.
6. Some endpoints bypass the `ApiResponse` envelope (see §7) — frontend already parses both shapes.
7. ⚠️ Dev MongoDB URI (with credentials), JWT secret and Cloudinary key/secret fallbacks are committed in `application.properties` — production must override via env vars, and the committed credentials should be rotated and removed from the file and git history.
8. `KeepAliveScheduler` pings the production Render URL even from local runs.
9. The super-admin number is a config default committed in the repo; override `SUPER_ADMIN_MOBILE_NUMBER` in production. With `otp.use-static=true` (default) anyone can log in as that number with OTP `123456` — set `OTP_USE_STATIC=false` in production.

---

## 13. Future / Planned (not implemented — do not build unless asked)

Implemented: owner onboarding/verification, owner property CRUD (incl. reactivate), owner dashboard, `ADMIN`/`SUPER_ADMIN` roles with the `/api/admin` module, reviews, and manual payment confirmation (see §4/§6/§8).

Still not implemented: a real payment gateway (revenue/occupancy on the owner dashboard are estimates from bookings, not a ledger), an admin revoke/demote endpoint and Admin Panel UI, tests for the admin module, refresh tokens, Google/Apple login, Redis caching, ElasticSearch-backed search, Firebase push notifications, a JWT filter / broader role-based authorization (filter chain is `permitAll()`), and backfill of legacy `/uploads/` URLs. Full plan: `docs/GUIDELINES/ROADMAP.md`.

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
