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
