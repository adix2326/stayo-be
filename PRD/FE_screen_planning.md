# StayO Frontend Screen Planning
## Version 1.0 (MVP UI Planning)

**Project:** StayO  
**Platform:** Progressive Web App (PWA)  
**Design Approach:** Mobile First  
**Primary Roles:**
- Guest
- End User (Tenant)
- PG Owner
- Admin

---

# Objective

Before developing the frontend and backend, every application screen should be designed.

The backend APIs will later be developed according to the requirements of these screens.

This approach ensures:

- Better UI/UX planning
- Proper API design
- Minimal backend changes
- Faster development
- Consistent user experience

---

# User Roles

```
Guest

↓

User (Tenant)

↓

PG Owner

↓

Admin
```

---

# Module 1 — Public Module (Before Login)

## 1. Splash Screen

### Purpose

- Show StayO branding
- Check authentication status
- Auto login if JWT exists

### Components

- StayO Logo
- Tagline
- Loading Animation

---

## 2. Welcome / Landing Page

### Purpose

Introduce StayO to first-time users.

### Components

- Hero Banner
- Features
- Benefits
- CTA Button

### Buttons

- Login
- Explore Properties

---

## 3. Login Screen

### Components

- Mobile Number Input
- Country Code Picker

### Button

- Send OTP

---

## 4. OTP Verification

### Components

- OTP Input
- Countdown Timer
- Resend OTP

### Buttons

- Verify OTP
- Resend OTP

---

## 5. Complete Profile

### Fields

- Name
- Email
- Gender
- Occupation
- College
- Company
- City

### Button

- Continue

---

# Module 2 — End User

---

# Home & Discovery

---

## 6. Home Dashboard ⭐

This should be the most attractive screen in the application.

### Sections

- Greeting
- Search Bar
- Hero Banner Carousel
- Quick Filters
- Categories
- Nearby Properties
- Recommended Properties
- Trending Properties
- Recently Viewed
- Popular Cities

### Bottom Navigation

- Home
- Search
- Bookings
- Wishlist
- Profile

---

## 7. Search Screen

### Components

- Search Bar
- Filter Button
- Sort Button
- Search Suggestions

---

## 8. Search Results

Displays

- Property Cards
- Pagination
- Empty State

---

## 9. Filter & Sort

### Filters

- City
- Area
- Budget
- Property Type
- Gender
- Occupancy
- Food
- WiFi
- Parking
- Laundry
- AC
- Power Backup

### Sort

- Lowest Price
- Highest Price
- Nearest
- Highest Rated
- Newest

---

## 10. Nearby Properties (Map View)

### Components

- Google Maps
- Property Pins
- Nearby List

---

## 11. Property Details

### Sections

- Image Gallery
- Property Name
- Rent
- Deposit
- Description
- Amenities
- Owner Details
- Reviews
- Available Rooms
- Policies
- Nearby Colleges
- Nearby Companies
- Google Map

### Buttons

- Book Now
- Call Owner
- WhatsApp
- Wishlist
- Share

---

## 12. Property Gallery

Displays

- All Property Images
- Full Screen Viewer

---

## 13. Reviews & Ratings

Displays

- User Reviews
- Average Rating
- Rating Breakdown

---

## 14. Property Location

Displays

- Google Maps
- Nearby Places
- Distance

---

# Booking Module

---

## 15. Booking Request

Displays

- Property Summary
- Booking Form

Buttons

- Submit Request

---

## 16. My Bookings

Displays

- Active Bookings
- Pending
- Completed
- Cancelled

---

## 17. Booking Details

Displays

- Booking Timeline
- Property
- Owner Details

---

## 18. Booking Status

Status

- Pending
- Approved
- Rejected
- Cancelled
- Completed

---

# Wishlist

---

## 19. Wishlist

Displays

- Saved Properties

Buttons

- Remove
- View Property

---

# Profile

---

## 20. My Profile

Displays

- Profile Image
- Name
- Mobile
- Email
- Occupation
- Profile Completion

---

## 21. Edit Profile

Editable Fields

- Name
- Email
- Occupation
- College
- Company
- Bio
- City

---

## 22. Upload Profile Picture

Displays

- Camera
- Gallery

---

## 23. Settings

Options

- Dark Mode
- Language
- Notifications
- Privacy

---

## 24. Notifications

Displays

- Booking Updates
- Offers
- Promotions
- Messages

---

## 25. Help & Support

Options

- FAQs
- Contact Support
- Report Issue

---

## 26. About StayO

Displays

- App Version
- Company
- Terms
- Privacy Policy

---

## 27. Privacy Policy

---

## 28. Terms & Conditions

---

## 29. Delete Account

Confirmation Screen

---

## 30. Logout Confirmation

Confirmation Dialog

---

# Module 3 — PG Owner

---

# Owner Onboarding

---

## 31. Become a PG Owner

### Introduction

Benefits of joining StayO

Button

Become Owner

---

## 32. Business Details

Fields

- Owner Name
- Business Name
- Mobile
- Email
- GST (Optional)
- PAN

---

## 33. Property Verification

Upload

- Aadhaar
- PAN
- Electricity Bill
- Rental Agreement
- Property Images

---

## 34. Bank Details

Fields

- Account Holder
- Account Number
- IFSC

---

## 35. Verification Pending

Status Screen

---

## 36. Verification Approved

Congratulations Screen

---

## 37. Verification Rejected

Reason for rejection

Re-upload Documents

---

# Owner Dashboard

---

## 38. Dashboard

Cards

- Total Properties
- Active Listings
- Pending Bookings
- Occupied Rooms
- Monthly Revenue
- Total Views

---

## 39. My Properties

Displays

- Property List

Buttons

- Add Property
- Edit
- Delete

---

## 40. Add Property

Complete Property Form

---

## 41. Edit Property

Update Property Details

---

## 42. Property Images

Upload Images

Delete Images

---

## 43. Property Availability

Manage

- Available Rooms
- Occupancy

---

## 44. Booking Requests

Displays

- New Requests
- Accepted
- Rejected

---

## 45. Booking Details

Tenant Information

Booking Timeline

---

## 46. Reviews

View

Reply

---

## 47. Analytics

Charts

- Views
- Clicks
- Bookings
- Revenue

---

## 48. Owner Profile

Business Details

Verification Status

---

## 49. Owner Settings

Notifications

Bank Details

Password

---

# Module 4 — Admin

---

## 50. Admin Dashboard

Cards

- Users
- Owners
- Properties
- Bookings
- Revenue
- Pending Approvals

---

## 51. User Management

Actions

- Search
- Suspend
- Delete

---

## 52. User Details

Displays

Complete User Information

---

## 53. Owner Management

Approve

Reject

View Documents

---

## 54. Owner Details

Business Information

Verification Documents

---

## 55. Property Management

Approve Listings

Hide Listings

Delete Listings

---

## 56. Property Approval

Property Verification Workflow

---

## 57. Reports & Analytics

Charts

- Revenue
- Users
- Properties
- Cities
- Occupancy

---

## 58. CMS Management

Manage

- Home Banners
- Offers
- FAQs
- Cities

---

## 59. Admin Settings

General Platform Settings

---

# Shared Components

These components should be reusable across all pages.

- App Bar
- Bottom Navigation
- Search Bar
- Property Card
- Hero Banner
- Image Carousel
- Buttons
- Filter Chips
- Loading Screen
- Empty State
- Error State
- Confirmation Dialog
- Rating Component
- Profile Avatar
- OTP Input

---

# Design Priority

## Phase 1

- Splash Screen
- Welcome Screen
- Login
- OTP Verification
- Complete Profile

---

## Phase 2

- Home Dashboard ⭐
- Search
- Search Results
- Property Details
- Wishlist

---

## Phase 3

- Booking Flow
- Profile
- Settings
- Notifications

---

## Phase 4

- Owner Onboarding
- Owner Dashboard
- Property Management
- Analytics

---

## Phase 5

- Admin Dashboard
- User Management
- Property Approval
- Reports

---

# Estimated Screen Count

| Module | Screens |
|---------|---------|
| Public | 5 |
| End User | 25 |
| PG Owner | 19 |
| Admin | 10 |

**Total Estimated Screens: ~59**

---

# UI Design System (Before Designing Screens)

Create the following reusable components first:

## Colors

- Primary
- Secondary
- Accent
- Success
- Warning
- Error
- Background
- Surface

## Typography

- Display
- Heading
- Subheading
- Body
- Caption

## Components

- Primary Button
- Secondary Button
- Input Field
- Search Bar
- Cards
- Chips
- Bottom Navigation
- App Bar
- Dialogs
- Badges

## Icons

- Home
- Search
- Wishlist
- Booking
- Profile
- Settings
- Notifications
- Property
- Owner
- Admin

---

# Next Step

After completing all UI screens:

1. Finalize the design system.
2. Create high-fidelity mockups.
3. Review all user flows.
4. Derive backend APIs from each screen.
5. Implement the frontend.
6. Build backend APIs based on finalized UI requirements.
7. Integrate frontend with backend.
8. Perform end-to-end testing.