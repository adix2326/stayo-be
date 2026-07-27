# Request & Response Formats

## The `ApiResponse<T>` Wrapper
To enforce a consistent contract across the entire platform, all successful REST controller endpoints (except specific edge cases like `HealthController` or file uploads) wrap their payload inside a generic `ApiResponse<T>` object.

### Structure
```json
{
  "success": true,
  "message": "Dynamic success message",
  "data": { ... payload based on generic type T ... },
  "timestamp": "2024-05-18T10:15:30.123Z"
}
```

### Example: Dashboard Response
```json
{
  "success": true,
  "message": "Dashboard Loaded Successfully",
  "data": {
    "user": {
      "id": "user123",
      "name": "John Doe",
      "profileImage": "/uploads/uuid.jpg",
      "city": "Pune",
      "wishlistCount": 3,
      "notificationCount": 1
    },
    "searchDefaults": {
      "city": "Pune",
      "pgType": "PG",
      "latitude": 18.5204,
      "longitude": 73.8567
    },
    "heroBanners": [...],
    "nearbyPGs": [...]
  },
  "timestamp": "2026-07-15T15:00:00Z"
}
```

## Pagination: `PageResponse<T>`
For lists of data (like Search results), the `data` field of the `ApiResponse` contains a `PageResponse<T>`.

### Structure
```json
{
  "success": true,
  "message": "Properties retrieved successfully",
  "data": {
    "content": [
      {
        "id": "pg123",
        "name": "Sunny PG",
        "rent": 8500,
        "rating": 4.5
        // ... PGCardDTO fields
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 42,
    "totalPages": 5,
    "last": false
  },
  "timestamp": "2026-07-15T15:00:00Z"
}
```

## Standard Request DTOs

### Auth Request (`OtpVerifyRequestDto`)
```json
{
  "mobileNumber": "+919876543210",
  "otp": "123456"
}
```

### Update Profile (`UpdateProfileRequest`)
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "gender": "FEMALE",
  "city": "Bangalore"
}
```

*Note: The system heavily emphasizes returning Data Transfer Objects (DTOs) from controllers and never raw Database Entities to prevent accidental data leaks of sensitive fields.*
