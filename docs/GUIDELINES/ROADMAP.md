# Product Roadmap

This document outlines the planned future features and architectural upgrades for StayO, as defined by the product vision and current AI contexts.

## Upcoming Business Features
### 1. Booking Flow
- Full end-to-end booking of PGs.
- Handling room availability, specific bed selection, and occupancy tracking.
- Integration of a payment gateway.

### 2. Owner Portal & Admin Panel
- **Owner Portal** (`/owner-dashboard`, `/owner-onboarding`): Dedicated interface for PG owners to list properties, manage residents, and view earnings.
- **Admin Panel**: Internal tool for StayO staff to verify properties, manage disputes, and oversee platform analytics.

### 3. Reviews & Ratings
- Allow users to submit detailed reviews for PGs they have booked.
- Aggregate ratings to influence the sorting algorithm (`rating_desc`).

### 4. User Engagement
- **Push Notifications**: Integration with Firebase Cloud Messaging (FCM) to push real-time alerts to mobile PWAs.
- **Referral System**: Incentivize user growth.
- **AI Recommendations**: Personalized PG suggestions based on user search history and saved properties.

## Architectural Upgrades
### 1. Caching Layer (Redis)
- Implement Redis to cache static/semi-static data.
- **Targets**: Dashboard Hero Banners, Categories, Popular Areas, Amenities, and Search Suggestions.
- **Goal**: Further reduce Dashboard load times from <150ms to <50ms.

### 2. Advanced Search (ElasticSearch)
- Migrate the `SearchService` from standard MongoDB queries to ElasticSearch.
- **Goal**: Enable fuzzy matching, geospatial radius searches, and highly performant filtering at scale.

### 3. Global State Management (React Query)
- Fully adopt TanStack React Query on the frontend for robust server state management.
- **Goal**: Automate caching, background refetching, and deduplication of API requests without relying heavily on `useEffect`.

### 4. Authentication Enhancements
- Introduce OAuth2 for **Google Login** and **Apple Login** alongside the existing OTP system.
