# Booking Module

## Business Purpose
Lets a user submit a booking request for a PG and track it, and lets the PG owner accept, reject, and confirm payment. There is no payment gateway; the owner's `confirm-payment` call is a manual stopgap that unlocks reviews.

## Responsibilities
- Create a booking request, snapshotting PG display fields and pricing at creation time.
- Block a second active request from the same user for the same PG (service check + partial unique index).
- List / fetch a user's own bookings; list booking requests for an owner's PGs.
- User cancels while `PENDING_OWNER`.
- Owner accepts, rejects (optional reason), and confirms payment.
- Fire notifications through `NotificationService` (requested, accepted, rejected, payment confirmed). These are log-only stubs.

## Folder Structure
```text
com.stayo.stayo.booking
├── config/BookingIndexInitializer.java       # partial unique index at startup
├── controller/BookingController.java
├── dto/{BookingRequestDTO, BookingResponseDTO, BookingRejectRequestDTO, OccupantDTO}.java
├── entity/{Booking, OccupantInfo}.java
├── enums/{BookingStatus, MinimumStay, PaymentStatus, RoomType}.java
├── exception/{BookingNotFoundException, DuplicateBookingException,
│              InvalidBookingRequestException, InvalidBookingStateException}.java
├── mapper/BookingMapper.java
├── repository/BookingRepository.java
└── service/{BookingService, impl/BookingServiceImpl}.java
```

## APIs
| Method | Path | Notes |
|---|---|---|
| POST | `/api/booking` | Create. 201 + `BookingResponseDTO`. |
| GET | `/api/booking?status=` | List own bookings, optional status filter. |
| GET | `/api/booking/{bookingId}` | Single booking, ownership-checked. |
| DELETE | `/api/booking/{bookingId}` | Cancel — only from `PENDING_OWNER`. |
| GET | `/api/booking/owner?status=` | Requests for PGs owned by the caller. |
| PATCH | `/api/booking/owner/{bookingId}/accept` | `PENDING_OWNER → OWNER_ACCEPTED`. |
| PATCH | `/api/booking/owner/{bookingId}/reject` | `PENDING_OWNER → OWNER_REJECTED`; optional `{ "reason" }`. |
| PATCH | `/api/booking/owner/{bookingId}/confirm-payment` | `OWNER_ACCEPTED → CONFIRMED`, `paymentStatus=PAID`. |

## Status Lifecycle
```
PENDING_OWNER ─┬─ accept ──► OWNER_ACCEPTED ── confirm-payment ──► CONFIRMED (PAID)
               ├─ reject ──► OWNER_REJECTED
               └─ cancel ──► CANCELLED
```
Invalid transitions throw `InvalidBookingStateException` (409). Non-owners get `BookingNotFoundException` so booking existence is not leaked.

## Pricing
Snapshotted at creation from the PG's per-sharing rent (`rentByRoomType`): `totalPayable = monthlyRent × months(minimumStay) + securityDeposit`, where `minimumStay` is `THREE_MONTHS` / `SIX_MONTHS` / `TWELVE_MONTHS`.

## Data Model
`bookings` collection — denormalizes `pgName`/`pgLocality`/`pgCity`/`pgOwnerId`; stores `rejectionReason`, `paymentStatus`, and the pricing snapshot. Indexes: `(userId, status, createdAt)`, `(pgId, status)`, and a partial unique `(userId, pgId)` for statuses not in `[CANCELLED, OWNER_REJECTED]` created by `BookingIndexInitializer`.

## Tests
`BookingControllerTest` (integration) and `BookingServiceImplTest` (pricing, duplicates, ownership, state machine, notifications).

## Known Gaps
- No real payment gateway; `PaymentStatus.FAILED` / `REFUNDED` are never set.
- No status history/timeline (only `status` + `updatedAt`).
- Notifications are log-only; nothing is persisted or pushed.
