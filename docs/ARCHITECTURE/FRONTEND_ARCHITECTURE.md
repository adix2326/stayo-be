# Frontend Architecture

## Overview
StayO's frontend is a modern, single-page application (SPA) built with **React**, **Vite**, and **Tailwind CSS**. It is designed with a **Mobile-First** approach to provide an app-like experience in the browser. 

The application is structured as a Progressive Web App (PWA) with offline capabilities and service workers enabled via `vite-plugin-pwa`.

## Technology Stack
- **Framework**: React 19
- **Build Tool**: Vite 8
- **Styling**: Tailwind CSS v4 (PostCSS)
- **Icons**: Lucide React
- **Routing**: React Router DOM (v7)
- **Animations**: Framer Motion
- **Package Manager**: npm

## Directory Structure
The codebase follows a feature-driven architecture. Instead of grouping all components or pages together, files are co-located by feature (e.g., `auth`, `dashboard`, `search`, `wishlist`).

```text
src/
├── assets/          # Static images (hero, logos, placeholders)
├── components/      # Shared/common components (Button, Input, BottomNav)
├── features/        # Feature-driven modules (Core Logic)
│   ├── auth/        # Login, OTP verification
│   ├── dashboard/   # User and owner dashboards
│   ├── search/      # Search logic, filters, results
│   ├── property/    # PG details, gallery, nearby
│   ├── wishlist/    # Saved properties
│   ├── profile/     # User profile, settings
│   ├── booking/     # Booking requests and history
│   └── notification/# Notifications
├── layouts/         # Page wrappers (e.g., AuthLayout)
├── pages/           # High-level static/root pages (Welcome, Privacy)
├── App.jsx          # Root router configuration
├── main.jsx         # Entry point and Service Worker registration
├── index.css        # Global CSS and Tailwind directives
└── urls.js          # API endpoint configurations
```

## Progressive Web App (PWA)
The app utilizes `vite-plugin-pwa` to register a service worker (`registerSW({ immediate: true })` in `main.jsx`). This enables caching of static assets and provides an installable app-like experience on mobile devices. A global `<PWAInstallBanner />` component manages the prompt for users to install the app.

## State Management & Data Fetching
Currently, state is managed locally within components or lifted up via props. API calls are managed via standard fetch/axios promises. 
*(Note: As per architecture guidelines, React Query and Context are slated for future implementation to manage server and global UI state respectively).*
