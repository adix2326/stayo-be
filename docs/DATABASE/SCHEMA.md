# Database Schemas

The following represents the core schema structures mapping to MongoDB documents via Spring Data `@Document`.

## Core Schemas

### `users`
```javascript
{
  "_id": ObjectId("..."),
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "mobileNumber": "+919876543210",
  "gender": "FEMALE",
  "dateOfBirth": ISODate("2000-01-01T00:00:00.000Z"),
  "occupation": "STUDENT",
  "college": "Pune University",
  "company": null,
  "city": "Pune",
  "state": "Maharashtra",
  "country": "India",
  "bio": "Looking for a quiet place.",
  "profileImage": "/uploads/uuid.jpg",
  "role": "USER",
  "phoneVerified": true,
  "profileCompleted": true,
  "wishlistPropertyIds": ["pgId1", "pgId2"], // Note: Embedded array
  "createdAt": ISODate("2026-07-15T10:00:00.000Z"),
  "updatedAt": ISODate("2026-07-15T10:00:00.000Z"),
  "lastLogin": ISODate("2026-07-15T15:00:00.000Z")
}
```

### `properties` (PG)
```javascript
{
  "_id": ObjectId("..."),
  "pgName": "Sunrise PG",
  "description": "Premium living...",
  "city": "Pune",
  "locality": "Hinjewadi",
  "address": "Phase 1, Near Infosys",
  "genderCategory": "BOYS", // BOYS | GIRLS | UNISEX
  "rent": 8500.0,
  "amenities": ["Wi-Fi", "AC", "Laundry"],
  "images": ["url1", "url2"],
  "rating": 4.5,
  "reviewCount": 120,
  "isFeatured": true,
  "isActive": true,
  "createdAt": ISODate("2026-07-10T10:00:00.000Z"),
  "updatedAt": ISODate("2026-07-10T10:00:00.000Z")
}
```

## Ephemeral Schemas (TTL Managed)

### `otp_requests`
```javascript
{
  "_id": ObjectId("..."),
  "mobileNumber": "+919876543210",
  "otp": "123456",
  "attempts": 1,
  "expiryTime": ISODate("2026-07-15T10:05:00.000Z") // TTL Index triggers deletion here
}
```

### `blacklisted_tokens`
```javascript
{
  "_id": ObjectId("..."),
  "token": "eyJhbGciOiJIUzI1NiIsInR5c...",
  "expiryDate": ISODate("2026-07-16T10:00:00.000Z") // TTL Index triggers deletion here
}
```
