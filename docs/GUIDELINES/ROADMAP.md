# Product Roadmap

Planned features and architectural upgrades for StayO. Status is as of 2026-10-04; see `AI_BE_CONTEXT.md` §13 for the backend source of truth.

## Done
- **Booking request flow**: create, list, cancel, owner accept/reject, manual owner `confirm-payment` (`CONFIRMED` + `PAID`).
- **Owner portal (backend)**: owner onboarding + document upload, verification, property CRUD (deactivate/reactivate, images), owner dashboard.
- **Admin (backend)**: `ADMIN` / `SUPER_ADMIN` roles, owner-submission review queue, super admin grants `ADMIN` (`/api/admin`).
- **Reviews & ratings**: submit/list reviews; rating and review count aggregated on the PG. Reachable once a booking is `CONFIRMED` + `PAID`.
- **Cloud file storage**: Cloudinary replaces local `uploads/`.

## Upcoming Business Features
### 1. Payment Gateway
- Replace the manual `confirm-payment` stopgap with a real gateway; make `PaymentStatus.FAILED` / `REFUNDED` reachable.
- Replace estimated revenue/occupancy on the owner dashboard with a real ledger.
- Room availability, bed selection and occupancy tracking.

### 2. Admin Panel
- Admin UI on top of `/api/admin` (verify owners, manage admins).
- Endpoint to revoke `ADMIN`; property verification, dispute handling, platform analytics.
- Tests for the admin module (none yet).

### 3. User Engagement
- **Push Notifications**: Firebase Cloud Messaging; `NotificationService` is currently log-only.
- **Referral System**.
- **AI Recommendations** from search history and saved properties.

## Architectural Upgrades
### 1. Security hardening
- Real JWT filter and role-based authorization in the filter chain (today `permitAll()` + per-endpoint checks).
- Remove committed credentials from `application.properties`, rotate them, and disable static OTP in production.
- Refresh tokens.

### 2. Caching Layer (Redis)
- Cache dashboard banners, categories, popular areas, amenities, search suggestions. Goal: dashboard < 50ms.

### 3. Advanced Search (ElasticSearch)
- Fuzzy matching, geospatial radius search, performant filtering at scale.

### 4. Frontend state (React Query)
- Adopt TanStack React Query for server state.

### 5. Authentication Enhancements
- Google and Apple login alongside OTP.

### 6. Data cleanup
- Backfill or clear legacy `/uploads/` image URLs left from before the Cloudinary migration.
