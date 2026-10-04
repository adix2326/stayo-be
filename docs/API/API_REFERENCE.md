# API Reference

REST endpoints of the StayO backend. Responses use the standard `ApiResponse<T>` envelope except where noted. "Token" means `Authorization: Bearer <JWT>`; auth is enforced manually per endpoint (`AuthUtil`), and role checks happen in services. Full detail: `AI_BE_CONTEXT.md` §6.

## Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/otp/send` | Send OTP to mobile number (E.164) | No |
| POST | `/api/auth/otp/verify` | Verify OTP, login/signup; body has `viaOwnerOnboarding` to pick initial role | No |
| PUT | `/api/auth/update-details` | Complete profile; returns a fresh token | Token |
| POST | `/api/auth/logout` | Blacklist the current token | Token |

## User & Profile
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/users/me` | Current user (raw DTO) | Token |
| GET | `/api/user/profile` | Full profile (raw DTO) | Token |
| PUT | `/api/user/profile` | Update profile (raw DTO) | Token |
| POST | `/api/user/profile/image` | Upload profile image (multipart `file`, stored on Cloudinary) | Token |
| DELETE | `/api/user/profile/image` | Delete profile image (204) | Token |
| GET | `/api/user/dashboard` | Aggregated home screen data | Token |

## Properties
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/properties/search` | Search/filter/paginate PGs | Optional |
| GET | `/api/properties/{id}` | PG details (records a view) | Optional |
| GET | `/api/properties/owner/mine` | PGs owned by caller | Token |
| POST | `/api/properties` | Create PG (approved owner only) | Token |
| PUT | `/api/properties/{id}` | Update PG (owner of PG) | Token |
| PATCH | `/api/properties/{id}/deactivate` | Soft-delete (owner of PG) | Token |
| PATCH | `/api/properties/{id}/reactivate` | Reactivate (owner of PG) | Token |
| POST | `/api/properties/{id}/images` | Upload image (multipart `file`) | Token |

## Wishlist
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/wishlist/add/{propertyId}` | Add PG | Token |
| POST | `/api/wishlist/remove/{propertyId}` | Remove PG (POST, not DELETE) | Token |
| GET | `/api/wishlist` | List wishlisted PGs | Token |

## Booking
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/booking` | Create booking request (201) | Token |
| GET | `/api/booking?status=` | List own bookings | Token |
| GET | `/api/booking/{bookingId}` | Get own booking | Token |
| DELETE | `/api/booking/{bookingId}` | Cancel (only `PENDING_OWNER`) | Token |
| GET | `/api/booking/owner?status=` | Booking requests for the owner's PGs | Token |
| PATCH | `/api/booking/owner/{bookingId}/accept` | Accept | Token (owner) |
| PATCH | `/api/booking/owner/{bookingId}/reject` | Reject (optional `reason`) | Token (owner) |
| PATCH | `/api/booking/owner/{bookingId}/confirm-payment` | Mark `OWNER_ACCEPTED` booking paid → `CONFIRMED` | Token (owner) |

## Owner
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/owner/dashboard` | Owner stats + revenue trend | Token |
| POST | `/api/owner/onboarding` | Submit/resubmit business + bank details | Token |
| GET | `/api/owner/onboarding/status` | Own verification status | Token |
| POST | `/api/owner/onboarding/documents` | Upload verification document (multipart) | Token |
| PATCH | `/api/owner/onboarding/{targetUserId}/verify` | Approve/reject submission | ADMIN / SUPER_ADMIN |

## Admin
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/admin/owners?status=` | List owner submissions (default `PENDING`) | ADMIN / SUPER_ADMIN |
| POST | `/api/admin/admins` | Grant `ADMIN` to a mobile number (201) | SUPER_ADMIN |
| GET | `/api/admin/admins` | List admins and super admins | ADMIN / SUPER_ADMIN |

## Reviews
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/reviews` | Submit review (needs `CONFIRMED` + `PAID` booking) | Token |
| GET | `/api/properties/{pgId}/reviews` | List reviews for a PG | No |

## System
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/health` | Health check (plain `OK`) | No |
| GET | `/swagger-ui.html`, `/v3/api-docs` | API docs | No |
