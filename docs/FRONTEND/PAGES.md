# Pages & Features

StayO's frontend is divided into modular "Features", each owning its respective pages.

## Auth Feature (`features/auth/pages/`)
- **Login.jsx**: The entry point for unauthenticated users. Requests a mobile number.
- **OTPVerification.jsx**: Accepts the OTP. Upon success, stores the JWT and redirects to Dashboard (or Profile completion if required).
- **LogoutConfirmation.jsx**: A modal/page to confirm intent to logout.

## Dashboard Feature (`features/dashboard/pages/`)
- **UserDashboard.jsx**: The main user landing screen. Displays the Hero Banner, Quick Filters, Nearby Properties, and generic Search entry points.
- *(Note: Includes future-proofing pages for Students and Wardens like `StudentHome.jsx`, `WardenOperations.jsx`)*.

## Search Feature (`features/search/pages/`)
- **SearchPage.jsx**: The dedicated search experience with recent searches and auto-suggestions.
- **SearchResults.jsx**: Displays the list of `PropertyCard.jsx` components based on the applied filters.
- **FiltersSort.jsx / FilterBottomSheet.jsx**: A slide-up bottom sheet allowing users to filter by Budget, Gender, Amenities, and sort by Price/Rating.

## Property Feature (`features/property/pages/`)
- **PropertyDetails.jsx**: The comprehensive view of a single PG. Shows images, amenities, exact rent, rules, owner details, and a "Book Now" CTA.
- **PropertyGallery.jsx**: A full-screen masonry or slider view of all property images.
- **NearbyProperties.jsx**: Dedicated list view for geo-located nearby PGs.

## Profile Feature (`features/profile/pages/`)
- **CompleteProfile.jsx**: Forced screen post-login if the user lacks a name/email.
- **MyProfile.jsx**: Overview of the user's details and completion percentage.
- **EditProfile.jsx**: Form to update personal and professional info.

## Wishlist Feature (`features/wishlist/pages/`)
- **MyWishlist.jsx**: Displays all saved `PropertyCard`s. Handles empty states gracefully ("No saved properties yet").

## Booking Feature (`features/booking/pages/`)
- **BookingRequest.jsx**: The checkout flow to finalize a booking for a PG.
- **MyBookings.jsx / BookingStatus.jsx**: Tracks active and past bookings.
