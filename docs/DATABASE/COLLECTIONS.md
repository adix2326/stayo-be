# MongoDB Collections

StayO uses MongoDB as its primary database. The database is heavily de-normalized to favor read-performance, which is critical for a discovery platform.

## Total Collections: 10

### 1. Core Domain
- **`users`**: Stores all registered users (owners and tenants). Contains embedded wishlist arrays.
- **`properties`**: The central collection storing all PG data (name, rent, location, amenities).

### 2. Authentication & Sessions
- **`otp_requests`**: Temporary collection tracking active OTP verification sessions. Managed by TTL index.
- **`blacklisted_tokens`**: Stores logged-out JWTs to prevent replay attacks. Managed by TTL index.

### 3. Content & Dashboard
- **`dashboard_banners`**: Active promotional banners displayed on the dashboard hero section.
- **`dashboard_categories`**: PG Categories (e.g., "Premium", "Boys", "Girls").
- **`quick_filters`**: Filters displayed on the dashboard for one-tap searching.
- **`popular_searches`**: Promoted localities or cities.

### 4. Search & Analytics
- **`cities`**: Master list of supported cities for the search autocomplete.
- **`property_views`**: Analytics collection tracking how many times a property was viewed.

---

## Future Collections
Based on the roadmap and module separation guidelines, the following collections are expected in the future:
- **`wishlist`**: To migrate the embedded array out of `users`.
- **`notifications`**: To persist in-app alerts.
- **`bookings`**: To track transaction flows.
- **`reviews`**: To store user ratings for PGs.
