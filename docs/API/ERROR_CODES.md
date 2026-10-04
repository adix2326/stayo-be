# Error Codes & Exception Handling

The StayO backend implements a `GlobalExceptionHandler` using `@RestControllerAdvice` to ensure all API errors follow a consistent structure.

## Standard Error Response Format
Most exceptions return an `ApiError` DTO format (except for `InvalidTokenException` which just returns a map):

```json
{
  "status": 400,
  "error": "Invalid OTP",
  "message": "Invalid OTP. 2 attempts remaining.",
  "path": "/api/auth/otp/verify"
}
```

## Exception to HTTP Status Mapping

| Exception Class | HTTP Status Code | Description |
|-----------------|------------------|-------------|
| `InvalidMobileNumberException` | `400 BAD_REQUEST` | Mobile number does not match E.164 format. |
| `InvalidOtpException` | `400 BAD_REQUEST` | OTP is incorrect (includes remaining attempt count). |
| `MethodArgumentNotValidException` | `400 BAD_REQUEST` | Body validation failure (e.g. `@Valid` failed). Returns field error details. |
| `ConstraintViolationException` | `400 BAD_REQUEST` | Path/query param validation failure. |
| `HttpMessageNotReadableException` | `400 BAD_REQUEST` | Malformed JSON in request body. |
| `ProfileNotCompletedException` | `400 BAD_REQUEST` | User attempted to access protected resources (like Dashboard) without a complete profile. |
| `MissingAuthorizationException` | `401 UNAUTHORIZED` | No JWT token provided when accessing a secured endpoint. |
| `InvalidTokenException` | `401 UNAUTHORIZED` | Token is expired, blacklisted, or invalid. |
| `UserNotFoundException` | `404 NOT_FOUND` | User ID extracted from token does not exist in the database. |
| `OtpNotFoundException` | `404 NOT_FOUND` | OTP session does not exist for the provided mobile number. |
| `OtpExpiredException` | `410 GONE` | OTP exists but the 5-minute validity window has expired. |
| `MaxOtpAttemptsExceededException` | `429 TOO_MANY_REQUESTS` | User exceeded 3 attempts for a given OTP session. |
| `Exception` (Generic) | `500 INTERNAL_SERVER_ERROR` | Unhandled fallback exception. |

## Domain Exceptions (booking, owner, property, review, admin)

| Exception Class | HTTP Status Code | Description |
|-----------------|------------------|-------------|
| `PropertyNotFoundException` | `404 NOT_FOUND` | PG does not exist. |
| `BookingNotFoundException` | `404 NOT_FOUND` | Booking does not exist or caller does not own it (existence is not leaked). |
| `OwnerProfileNotFoundException` | `404 NOT_FOUND` | Owner never submitted onboarding. |
| `InvalidBookingRequestException` | `400 BAD_REQUEST` | Occupant count mismatch, unavailable room type, invalid decision. |
| `InvalidVerificationRequestException` | `400 BAD_REQUEST` | Rejecting an owner without a `rejectionReason`. |
| `ReviewNotEligibleException` | `400 BAD_REQUEST` | Booking/PG mismatch or booking not `CONFIRMED` + `PAID`. |
| `TooManyImagesException` | `400 BAD_REQUEST` | PG already has the maximum number of images. |
| `OwnerNotVerifiedException` | `403 FORBIDDEN` | Creating a PG without an approved owner profile. |
| `PropertyAccessDeniedException` | `403 FORBIDDEN` | Modifying a PG the caller does not own. |
| `AdminAccessRequiredException` | `403 FORBIDDEN` | Admin-only action without `ADMIN` or `SUPER_ADMIN`. |
| `SuperAdminAccessRequiredException` | `403 FORBIDDEN` | `POST /api/admin/admins` without `SUPER_ADMIN`. |
| `DuplicateBookingException` | `409 CONFLICT` | Active booking already exists for this user + PG. |
| `InvalidBookingStateException` | `409 CONFLICT` | Illegal status transition (cancel/accept/reject/confirm-payment). |
| `OwnerAlreadyOnboardedException` | `409 CONFLICT` | Resubmitting while `PENDING` or `VERIFIED`. |
| `DuplicateReviewException` | `409 CONFLICT` | Already reviewed this booking. |

Verify exact statuses in `shared/exception/GlobalExceptionHandler.java`.

## Framework & Infrastructure Errors
The catch-all handler in `GlobalExceptionHandler` keeps the status of Spring's own web exceptions instead of returning 500:

| Cause | HTTP Status Code |
|-------|------------------|
| Unknown path (`NoResourceFoundException`) | `404 NOT_FOUND` |
| Wrong HTTP method (includes `Allow` header) | `405 METHOD_NOT_ALLOWED` |
| Unsupported `Content-Type` | `415 UNSUPPORTED_MEDIA_TYPE` |
| Missing request param/part and similar | `400 BAD_REQUEST` |
| Upload over 8MB (`MaxUploadSizeExceededException`) | `413 PAYLOAD_TOO_LARGE` |
| MongoDB unreachable/timeouts (`DataAccessException`) | `503 SERVICE_UNAVAILABLE` |
| Anything else unexpected | `500` with a generic message; the stack trace is logged, never returned |
