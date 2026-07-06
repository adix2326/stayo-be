# StayO Product Requirements Document (PRD)
## Version 1.0 (MVP Planning)
**Author:** Aditya Dhamale  
**Project:** StayO  
**Platform:** Progressive Web App (PWA)  
**Target Audience:** Students, Working Professionals, PG Owners

---

# 1. Vision

StayO is a modern accommodation discovery platform that helps students and working professionals find verified PGs, hostels, co-living spaces and rental rooms nearby.

The platform focuses on making accommodation search simple, transparent and trustworthy while giving property owners an easy way to manage their listings.

---

# 2. Goals

## User Goals

- Find nearby PGs
- Compare PGs easily
- View verified properties
- Save favourite properties
- Send booking requests
- Contact property owners
- Manage bookings

## PG Owner Goals

- Register business
- List properties
- Manage bookings
- Manage availability
- View analytics

## Admin Goals

- Verify owners
- Approve properties
- Manage users
- Moderate listings
- View platform analytics

---

# 3. User Roles

There are three primary roles in StayO.

```
Guest
   │
   ▼
User (Tenant)
   │
   ▼
PG Owner
   │
   ▼
Admin
```

---

# 4. User Journey

```
Landing Page

↓

Login

↓

OTP Verification

↓

Complete Profile

↓

Home

↓

Search

↓

Property Details

↓

Wishlist

↓

Booking Request

↓

Owner Approval

↓

Booking Confirmed

↓

Move In
```

---

# 5. Application Modules

## Module 1

Authentication

## Module 2

User Profile

## Module 3

Property Listing

## Module 4

Search

## Module 5

Wishlist

## Module 6

Bookings

## Module 7

Notifications

## Module 8

Owner Dashboard

## Module 9

Admin Dashboard

---

# 6. User Mobile Screens

## 1. Splash Screen

Purpose

- App logo
- Auto Login
- Loading animation

Components

- StayO Logo
- Tagline
- Loading Indicator

Buttons

None

---

## 2. Login Screen

Purpose

Authenticate user

Components

- Mobile Number Field
- Country Code Picker

Buttons

- Send OTP

---

## 3. OTP Verification

Components

- OTP Input
- Countdown Timer
- Resend OTP

Buttons

- Verify OTP
- Resend OTP

---

## 4. Complete Profile

Fields

- Name
- Email
- Gender
- Occupation
- College
- Company
- City

Buttons

- Continue

---

# 7. Home Screen (Most Important Screen)

The Home Screen should be the most attractive page of the application.

It should encourage users to continue exploring.

---

## Section 1

Greeting

Example

```
Good Evening,

Aditya 👋
```

---

## Section 2

Search Bar

Placeholder

```
Search PGs, Colleges, Companies...
```

Search Suggestions

- MIT Alandi
- VIT Pune
- Hinjewadi
- Baner
- Wakad

---

## Section 3

Hero Banner Carousel

Auto sliding banners

Examples

- Verified PGs
- Premium Co-Living
- Student Specials
- Working Professionals
- Lowest Rent
- Stay Near College

---

## Section 4

Quick Filters

Horizontal Chips

- Boys
- Girls
- Hostel
- PG
- Flat
- Private Room
- Double Sharing
- Triple Sharing
- With Food
- Without Food
- AC
- Non AC
- Parking
- WiFi

---

## Section 5

Categories

Cards

- Near Colleges
- Near IT Parks
- Luxury
- Budget
- Family Stay
- Monthly Rentals

---

## Section 6

Nearby Properties

Card contains

- Cover Image
- Property Name
- Distance
- Rent
- Rating
- Verified Badge
- Favourite Icon

---

## Section 7

Recommended For You

Based on

- Budget
- City
- College
- Company
- Search History

---

## Section 8

Trending Properties

Horizontal Carousel

---

## Section 9

Recently Viewed

Horizontal Carousel

---

## Section 10

Popular Cities

- Pune
- Bangalore
- Hyderabad
- Mumbai
- Delhi

---

## Bottom Navigation

- Home
- Search
- Bookings
- Wishlist
- Profile

---

# 8. Search Screen

Components

Search Bar

Filter Button

Sort Button

Map Toggle

Property List

---

Filters

Location

Budget

Gender

Property Type

Room Type

Amenities

Food

WiFi

Parking

Laundry

Power Backup

Lift

Security

---

Sort Options

Lowest Price

Highest Price

Highest Rated

Nearest

Newest

---

# 9. Property Details Screen

Components

Image Gallery

Property Name

Address

Google Map

Description

Amenities

Available Rooms

Rent Details

Deposit

Owner Information

Reviews

Policies

Nearby Colleges

Nearby Companies

Nearby Metro

Buttons

Book Now

Call Owner

WhatsApp

Add to Wishlist

Share

---

# 10. Wishlist Screen

Displays

Saved Properties

Buttons

Remove

View Property

---

# 11. Booking Screen

Displays

Pending Requests

Approved

Rejected

Cancelled

Completed

Buttons

Cancel Request

View Property

---

# 12. Notifications

Types

Booking Updates

Offers

Announcements

OTP

Owner Messages

---

# 13. Chat Screen (Future)

Features

Owner Chat

Images

Documents

Location Sharing

---

# 14. Profile Screen

Displays

Profile Image

Name

Email

Phone

Occupation

City

Profile Completion

Buttons

Edit Profile

Logout

---

# 15. Settings

Dark Mode

Language

Notification Preferences

Privacy

Delete Account

About

Support

---

# 16. PG Owner Journey

```
Login

↓

Become Owner

↓

Submit Business Details

↓

Upload Documents

↓

Admin Verification

↓

Owner Dashboard
```

---

# 17. PG Owner Onboarding

## Step 1

Basic Information

- Name
- Mobile
- Email

---

## Step 2

Business Information

- Business Name
- Property Name
- GST (Optional)
- PAN

---

## Step 3

Property Verification

Upload

- Aadhaar
- PAN
- Electricity Bill
- Rental Agreement
- Property Images

---

## Step 4

Bank Details

- Account Holder Name
- Account Number
- IFSC

---

## Step 5

Admin Verification

Status

Pending

Approved

Rejected

---

# 18. Owner Dashboard

Dashboard Cards

Total Properties

Available Rooms

Occupied Rooms

Monthly Revenue

Pending Requests

Today's Views

---

## Owner Navigation

Dashboard

My Properties

Bookings

Reviews

Analytics

Profile

Settings

---

# 19. Property Management

Functions

Add Property

Edit Property

Delete Property

Deactivate Listing

Upload Images

Manage Availability

---

# 20. Booking Management

Owner can

Accept

Reject

Contact User

View Booking Details

---

# 21. Analytics

Property Views

Clicks

Booking Requests

Occupancy

Revenue

---

# 22. Admin Dashboard

Cards

Total Users

Owners

Properties

Pending Approvals

Bookings

Revenue

---

Navigation

Dashboard

Users

Owners

Properties

Reports

CMS

Settings

---

# 23. User Management

Admin can

Search Users

Suspend User

Delete User

View Profile

---

# 24. Owner Management

Approve Owner

Reject Owner

View Documents

View Business Details

---

# 25. Property Moderation

Approve Property

Reject Property

Feature Property

Remove Property

Hide Listing

---

# 26. Reports

Revenue

Bookings

Users

Cities

Growth

Occupancy

---

# 27. CMS

Manage

Home Banners

Offers

Cities

FAQs

Terms

Privacy Policy

---

# 28. Notifications System

Users

OTP

Booking Updates

Promotional Offers

Owner Messages

Admins

Owner Verification

Property Approval

Reports

---

# 29. MVP Features

Authentication

User Profile

Home Page

Search

Property Details

Wishlist

Booking Requests

Owner Registration

Admin Approval

Notifications

---

# 30. Phase 2 Features

Chat

Google Maps

Reviews

Ratings

Coupons

Referral

Payments

Bookmarks

Recently Viewed

Push Notifications

---

# 31. Phase 3 Features

AI Recommendations

Voice Search

Image Search

Price Prediction

Roommate Matching

Move-in Checklist

Rent Reminder

Subscription Plans

Owner Analytics

---

# 32. Backend Modules

```
auth/

user/

property/

owner/

booking/

wishlist/

review/

notification/

admin/

common/
```

---

# 33. Estimated Mobile Screens

| Module | Screens |
|---------|---------|
| Authentication | 4 |
| User | 6 |
| Home | 1 |
| Search | 2 |
| Property | 4 |
| Wishlist | 1 |
| Booking | 3 |
| Notifications | 1 |
| Owner | 10 |
| Admin | 10 |

**Total Estimated Screens:** **40–45**

---

# 34. UI Design Priority

The screens should be designed in the following order:

1. Splash Screen
2. Login
3. OTP Verification
4. Complete Profile
5. **Home Screen (Highest Priority)**
6. Search
7. Property Details
8. Wishlist
9. Booking
10. Profile
11. Owner Dashboard
12. Admin Dashboard

---

# 35. Success Metrics (MVP)

- User can register in under 60 seconds.
- User can discover nearby PGs within 2 taps.
- Property search results load in under 2 seconds.
- Owner onboarding can be completed in under 10 minutes.
- Admin can approve a property in under 2 minutes.
- Users can submit a booking request in fewer than 5 interactions.

---

# 36. Future Vision

StayO aims to become a complete accommodation ecosystem by providing:

- Verified accommodation listings
- Transparent pricing
- Easy owner onboarding
- Secure booking requests
- Smart search and recommendations
- Seamless communication between tenants and owners

The long-term vision is to evolve StayO into the preferred platform for discovering and managing student housing, PGs, hostels, and co-living spaces across India.