# Booking Module

## Business Purpose
Lets a logged-in user submit a booking request for a PG, track its status, and cancel it while still pending. This is the **request** half of the journey — a user asks to move in, the owner is meant to review and respond. Per the PRD (`PRD/StayO_PRD.md` §11, §18, §20, §44-45), the full journey also includes an owner-facing accept/reject flow and a booking timeline, neither of which exist yet on the backend.

## Responsibilities (current)
- Create a booking request against a PG, snapshotting PG display fields and pricing at creation time.
- Block a second active request from the same user for the same PG.
- List / fetch a user's own bookings (ownership-scoped).
- Let the user cancel a booking, only while `PENDING_OWNER`.
- Fire a (stubbed, log-only) notification to the PG owner on creation.

## Folder Structure
```text
com.stayo.stayo.booking
├── controller/BookingController.java
├── dto/{BookingRequestDTO, BookingResponseDTO, OccupantDTO}.java
├── entity/{Booking, OccupantInfo}.java
├── enums/{BookingStatus, RoomType}.java
├── exception/{BookingNotFoundException, DuplicateBookingException}.java
├── mapper/BookingMapper.java
├── repository/BookingRepository.java
└── service/{BookingService, impl/BookingServiceImpl}.java
```

## APIs (current)
| Method | Path | Notes |
|---|---|---|
| POST | `/api/booking` | Create. 201 + `BookingResponseDTO`. |
| GET | `/api/booking?status=` | List own bookings, optional status filter. |
| GET | `/api/booking/{bookingId}` | Single booking, ownership-checked. |
| DELETE | `/api/booking/{bookingId}` | Cancel — only from `PENDING_OWNER`. |

## Data Model
`bookings` collection — denormalizes `pgName`/`pgLocality`/`pgCity`/`pgOwnerId` and snapshots `monthlyRent`/`securityDeposit`/`maintenanceFee`/`totalPayable` at creation. Compound indexes: `(userId, status, createdAt)`, `(pgId, status)`.

---

## Known Issues & Gaps

1. **No owner accept/reject API.** `OWNER_ACCEPTED`/`OWNER_REJECTED` are unreachable states. The booking journey cannot actually complete.
2. **Bug — wrong HTTP status on cancel.** `BookingServiceImpl.cancelBooking` throws `IllegalStateException` for an invalid-state cancel; `GlobalExceptionHandler` has no mapping for it, so it hits the generic `Exception → 500` handler. Should be a 409/400.
3. **No booking-ID format guard.** `findByIdAndUserId`/`findById` on a malformed Mongo ObjectId throws `IllegalArgumentException` from the driver, which also falls into the generic 500 handler instead of a clean 404/400.
4. **`OccupantDTO` has no validation.** `name`, `phone`, `email` are unconstrained strings on both the primary occupant and every entry in `extraOccupants` (which also isn't annotated `@Valid`, so nested constraints — once added — won't even fire).
5. **No cross-field consistency check.** `occupantCount` is never verified against `1 + extraOccupants.size()`.
6. **`minimumStay` is a free-text `String`.** No `@Pattern`/enum constraint, unlike `RoomType`. Inconsistent values will pile up in the DB.
7. **Pricing ignores `minimumStay`.** `totalPayable = monthlyRent*3 + securityDeposit + maintenanceFee` always assumes a 3-month charge, regardless of which `minimumStay` the user selected.
8. **Duplicate-booking check isn't atomic.** `existsByUserIdAndPgIdAndStatusNotIn(...)` then `save(...)` is check-then-act; two concurrent submits can both pass the check.
9. **No status history / timeline.** The PRD's booking-details screen expects a timeline; currently only `status` + `updatedAt` exist, so history is lost on every transition.
10. **Notifications are one-directional and stubbed.** Only "owner notified on create" exists (and just logs). Nothing notifies the *user* when a decision is made — because no decision path exists yet.
11. **Zero test coverage.** Every other module (`auth`, `dashboard`, `property`) has controller + service tests; `booking` has none.
12. **Doc gap.** This module wasn't listed in `docs/README.md` (fixed as part of this change).

None of these are unreachable-code false alarms — they're all reachable from the four existing endpoints or block the two endpoints that don't exist yet.

---

## Implementation Plan

Work top-to-bottom; each phase is independently shippable and builds on the last. Phase 1 is the only one that's a straight bug fix — do it first regardless of what else you tackle.

### Phase 1 — Fix the cancel-status bug (do this first)
- Add `booking/exception/InvalidBookingStateException.java` (same shape as the other two booking exceptions).
- In `BookingServiceImpl.cancelBooking`, throw `InvalidBookingStateException` instead of `IllegalStateException`.
- In `GlobalExceptionHandler`, add a handler mapping it to `409 CONFLICT` (it's a state-conflict, same family as `DuplicateBookingException`).
- While there: wrap the `bookingId` lookups (`findByIdAndUserId`, and the new owner-side lookups from Phase 3) so a malformed ID produces `BookingNotFoundException` (404) rather than propagating a driver `IllegalArgumentException` to the generic 500 handler. Simplest approach: catch `IllegalArgumentException` around the repository call in the service layer and rethrow as `BookingNotFoundException`.

### Phase 2 — Validation hardening
- `OccupantDTO`: add `@NotBlank` on `name`, `@Pattern(regexp = "^\\+[1-9]\\d{1,14}$")` on `phone` (reuse the E.164 pattern already used for `mobileNumber` in auth), `@Email` on `email` if non-blank is required, or leave it optional per product decision.
- `BookingRequestDTO.extraOccupants`: annotate the list `@Valid` so nested `OccupantDTO` constraints actually run; add `@Size(max = 3)` (max 4 occupants total, 1 is primary).
- Add a service-level check in `createBooking`: if `occupantCount != 1 + extraOccupants.size()`, throw a new `InvalidBookingRequestException` (400) — annotation-based validation can't express this cross-field rule cleanly, so do it in `BookingServiceImpl` right after the existing PG/user lookups.
- Replace `minimumStay: String` with an enum, e.g. `MinimumStay { THREE_MONTHS, SIX_MONTHS, TWELVE_MONTHS }`, mirroring `RoomType`. Update `BookingRequestDTO`, `Booking`, `BookingResponseDTO`, `BookingMapper`.

### Phase 3 — Owner accept/reject workflow (the main gap)
There's no `owner` package yet in the backend (the PRD's frontend routing has one — `owner/`, `booking/` as siblings — but backend is still one module). Two viable layouts:
- **(a) Extend `BookingController`** with an `/api/booking/owner/...` sub-path — least churn, keeps everything in one controller since there's no owner module to hang it off yet.
- **(b) New top-level `owner` module** — cleaner long-term if an owner portal is coming per the roadmap, but bigger lift for just this workflow.

Recommendation: **(a) now**, revisit when the owner portal is actually built (`AI_BE_CONTEXT.md §13` lists it as future work) — don't build the module boundary before there's a second owner-facing feature to justify it.

Concretely:
- `BookingRepository`: add `findByPgOwnerIdOrderByCreatedAtDesc(String pgOwnerId)`, `findByPgOwnerIdAndStatusOrderByCreatedAtDesc(...)`, `findByIdAndPgOwnerId(String bookingId, String pgOwnerId)` (ownership check for mutations, mirroring the existing `findByIdAndUserId` pattern for the user side).
- `BookingService`: add `getOwnerBookings(String ownerUserId, BookingStatus status)` and `respondToBooking(String ownerUserId, String bookingId, BookingStatus decision, String reason)`.
  - `respondToBooking` validates: booking exists and `pgOwnerId == ownerUserId` (else `BookingNotFoundException` — don't leak existence to non-owners), current status is `PENDING_OWNER` (else `InvalidBookingStateException`), `decision` is one of `OWNER_ACCEPTED`/`OWNER_REJECTED` only.
- `BookingController`: add
  - `GET /api/booking/owner?status=` → `getOwnerBookings`
  - `PATCH /api/booking/owner/{bookingId}/accept`
  - `PATCH /api/booking/owner/{bookingId}/reject` (body: optional `{ "reason": "..." }`)
- Auth: same manual pattern as everywhere else — `AuthUtil.extractUserIdFromToken(token)`, then the ownership check above stands in for real RBAC (there's no role enforcement in the filter chain per `AI_BE_CONTEXT.md §5`, so ownership-by-data is the only thing actually protecting these endpoints — don't skip it).
- Notification: extend `NotificationService` with `notifyUserBookingAccepted(userId, pgName)` / `notifyUserBookingRejected(userId, pgName, reason)`, called from `respondToBooking`.

### Phase 4 — Status history / timeline
- Add an embedded list to `Booking`: `List<BookingStatusChange> statusHistory`, each entry `{ status, changedAt, changedBy, note }`.
- Append an entry on every transition: creation (`PENDING_OWNER`), accept, reject, cancel.
- Surface it on `BookingResponseDTO` — this directly backs the PRD's "Booking Timeline" on the booking-details screen (`PRD/StayO_PRD.md` §17).

### Phase 5 — Pricing correctness
- Make `totalPayable` actually depend on the selected `minimumStay` (e.g., 3/6/12 months of rent) instead of the hardcoded `monthlyRent * 3`.
- Pull the calculation out of `BookingServiceImpl` into a small `BookingPricingCalculator` component once it depends on more than `roomType` — not needed yet at 2 inputs, but flag it so it doesn't grow back into the service as more pricing rules (deposits per room type, promo codes, etc.) get added.

### Phase 6 — Concurrency hardening (optional, do if this is going to production)
- Current check-then-insert has a race window. Two options, pick one:
  - Add a partial unique index on `bookings`: `{ userId: 1, pgId: 1 }` with a partial filter `{ status: { $nin: ["CANCELLED", "OWNER_REJECTED"] } }` (created via a Mongo migration/init script, not the `@CompoundIndex` annotation, since partial filters aren't expressible there) — then catch `DuplicateKeyException` in `createBooking` and translate it to `DuplicateBookingException`.
  - Or accept the race window as a known MVP limitation (traffic is low enough it's unlikely) and just leave a comment — reasonable given the PRD is still request-based, not real-time-inventory-based.

### Phase 7 — Tests
Mirror the existing patterns:
- `booking/controller/BookingControllerTest` — MockMvc slice test, same shape as `PGControllerTest`. Cover: create (success, duplicate → 409, validation failure → 400), get by id (own vs. not-found), cancel (valid state, invalid state → 409 once Phase 1 lands).
- `booking/service/impl/BookingServiceImplTest` — Mockito unit tests. Cover: pricing math, duplicate check, ownership checks on owner accept/reject once Phase 3 lands.

### Phase 8 — Docs
- This file (`docs/MODULES/BOOKING.md`) now exists — link it from `docs/README.md` under Backend Modules (already done alongside this plan).
- Once Phase 3 ships, update `AI_BE_CONTEXT.md` §6 (API Surface) and §8 (Data Model) — that file states `CONFIRMED` is "reserved for post-payment" and owner APIs are future work; the accept/reject endpoints added here are pre-payment and still request-lifecycle, so they belong in this doc, not in the "Future / Planned" section, once built.

---

## Suggested order if implementing incrementally
1. Phase 1 (bug fix — 30 min)
2. Phase 2 (validation — straightforward, self-contained)
3. Phase 3 (owner workflow — the big one, unblocks the actual product journey)
4. Phase 7 (tests — write alongside Phase 3, not after)
5. Phase 4, 5, 6 in whatever order suits — all independent of each other
6. Phase 8 last