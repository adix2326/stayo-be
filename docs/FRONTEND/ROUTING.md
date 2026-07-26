# Routing

The StayO frontend uses **React Router DOM (v7)**. All routes are defined centrally in `src/App.jsx`.

## Route Definitions

### Public/Unauthenticated Routes
- `/` - **Welcome**: Landing page.
- `/login` - **Login**: Phone number entry.
- `/verify-otp` - **OTP Verification**: OTP entry screen.
- `/terms` - Terms and Conditions.
- `/privacy` - Privacy Policy.
- `/about` - About StayO.
- `/help` - Help and Support.

### Authenticated/Private Routes
*Currently all routes are rendered unconditionally in `App.jsx`, but logically these require an authenticated session context.*

**Core Discovery**
- `/dashboard` - Main user dashboard (Home).
- `/search` - Dedicated search screen with suggestions.
- `/search-results` - Listing of PGs based on search criteria.
- `/nearby-properties` - PGs near the user's location.

**Property Interaction**
- `/property-details` - Full details of a specific PG.
- `/property-gallery` - Full-screen image gallery.
- `/property-location` - Map view of the PG.
- `/reviews-ratings` - PG reviews.

**User Actions & Management**
- `/wishlist` - Saved properties.
- `/booking-request` - Initiate a booking.
- `/my-bookings` - List of user's bookings.
- `/booking-details` - Specific booking info.
- `/booking-status` - Track booking progress.

**Profile & Settings**
- `/complete-profile` - Forced screen for incomplete profiles.
- `/profile` - User profile overview.
- `/edit-profile` - Edit name, email, etc.
- `/upload-avatar` - Change profile picture.
- `/settings` - App settings.
- `/notifications` - In-app notifications.
- `/logout-confirm` - Logout confirmation screen.
- `/delete-account` - Account deletion.

### Owner Specific (Future/WIP)
- `/owner-dashboard` - Owner's main view.
- `/owner-onboarding` - Onboarding for new PG owners.

### Fallback
- `*` - Catch-all route that redirects to `/` (Welcome).

## Navigation Patterns
The application utilizes a **Bottom Navigation Bar** (`BottomNav.jsx`) for primary mobile navigation between Home, Search, Bookings, Wishlist, and Profile. The BottomNav is conditionally hidden on authentication screens and full-screen detail pages to maximize screen real estate.
