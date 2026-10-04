# StayO Technical Documentation

Welcome to the StayO Technical Documentation. This directory serves as the **Single Source of Truth** for the entire StayO project (Frontend + Backend). 

## Project Overview
StayO is a PG Discovery Platform built exclusively for Paying Guest accommodations. It enables users to discover, compare, save, and book PGs. The platform is designed with a modern, mobile-first frontend (React/Vite/Tailwind) and a scalable Modular Monolith backend (Spring Boot/MongoDB).

## Documentation Structure

### 🏗️ Architecture
- [Backend Architecture](ARCHITECTURE/BACKEND_ARCHITECTURE.md)
- [Frontend Architecture](ARCHITECTURE/FRONTEND_ARCHITECTURE.md)

### ⚙️ Backend Modules
Detailed documentation for each backend module:
- [Authentication](MODULES/AUTHENTICATION.md)
- [User](MODULES/USER.md)
- [Dashboard](MODULES/DASHBOARD.md)
- [Search](MODULES/SEARCH.md)
- [Property](MODULES/PROPERTY.md)
- [Wishlist](MODULES/WISHLIST.md)
- [Booking](MODULES/BOOKING.md)
- [Content](MODULES/CONTENT.md)
- [Notification](MODULES/NOTIFICATION.md)
- Owner, Admin, Review, Document, Storage modules: documented in [`AI_BE_CONTEXT.md`](../AI_BE_CONTEXT.md) §4–§8 (no standalone page yet)

### 🔌 API Reference
- [API Reference](API/API_REFERENCE.md)
- [Request & Response DTOs](API/REQUEST_RESPONSE.md)
- [Error Codes & Exception Handling](API/ERROR_CODES.md)

### 🗄️ Database
- [MongoDB Collections](DATABASE/COLLECTIONS.md)
- [Schema Details](DATABASE/SCHEMA.md)
- [Indexes](DATABASE/INDEXES.md)

### 🎨 Frontend
- [Design System](FRONTEND/DESIGN_SYSTEM.md)
- [Routing](FRONTEND/ROUTING.md)
- [Pages](FRONTEND/PAGES.md)
- [Components](FRONTEND/COMPONENTS.md)
- [Shared Components](FRONTEND/SHARED_COMPONENTS.md)
- [Hooks](FRONTEND/HOOKS.md)
- [State Management](FRONTEND/STATE_MANAGEMENT.md)
- [API Integration](FRONTEND/API_INTEGRATION.md)


### 📜 Guidelines
- [Coding Standards](GUIDELINES/CODING_STANDARDS.md)
- [Development Guidelines](GUIDELINES/DEVELOPMENT_GUIDELINES.md)
- [Naming Conventions](GUIDELINES/NAMING_CONVENTIONS.md)
- [AI Development Context](GUIDELINES/AI_CONTEXT.md)
- [Roadmap](GUIDELINES/ROADMAP.md)

---
*Source of truth for the backend is [`AI_BE_CONTEXT.md`](../AI_BE_CONTEXT.md); update it first when code changes. Last full sync: 2026-10-04.*
