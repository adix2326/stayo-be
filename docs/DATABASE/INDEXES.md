# Database Indexes

Proper indexing is critical for the performance goals of the StayO platform (Dashboard <150ms, Search <150ms). 

Indexes are currently defined via Spring Data MongoDB annotations (`@Indexed`, `@CompoundIndex`).

## Property Indexes (`properties` collection)
The `PG` collection is heavily queried by the Search and Property modules.

1. **City Index**: `@Indexed` on `city`. Optimizes filtering properties by the user's selected city.
2. **Locality Index**: `@Indexed` on `locality`. Optimizes text-based auto-complete or narrow filtering.
3. **Featured Compound Index**: `@CompoundIndex(name = "featured_active_index", def = "{'isFeatured': 1, 'isActive': 1}")`. Drastically speeds up queries for the Hero/Recommendation sections that only want active, featured PGs.
4. **IsActive**: `@Indexed` on `isActive`. Ensures soft-deleted or inactive properties are efficiently excluded from all queries.

## TTL (Time-To-Live) Indexes
StayO utilizes MongoDB's background TTL index feature to automatically prune expired data, saving the backend from running CRON jobs.

1. **OTP Expiration (`otp_requests`)**: 
   - Indexed on `expiryTime`.
   - Documents are automatically deleted by MongoDB when the current time surpasses `expiryTime` (usually 5 minutes after creation).
2. **JWT Blacklist (`blacklisted_tokens`)**:
   - Indexed on `expiryDate`.
   - When a user logs out, their JWT is inserted here. MongoDB automatically drops the document when the JWT's natural expiration date passes, keeping the collection small.

## Missing/Recommended Indexes
1. **Geospatial (`2dsphere`)**: Currently missing on the `PG` entity. To implement `NearbyPGService` effectively, a `location` field (GeoJsonPoint) must be added and indexed with `@GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)`.
2. **Unique Mobile Number**: The `users` collection should enforce a unique index on `mobileNumber` to prevent race conditions during signup.
