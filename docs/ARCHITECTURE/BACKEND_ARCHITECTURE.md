# Backend Architecture

## Overview
StayO's backend is architected as a **Modular Monolith** using **Spring Boot (v4.1.0)** and **Java 25**. It follows clean architecture principles, ensuring high maintainability and scalability while keeping infrastructure complexity low compared to microservices. 

The application uses **MongoDB** as its primary datastore and heavily relies on **JWT** for stateless authentication.

## Technology Stack
- **Language**: Java 25
- **Framework**: Spring Boot 4.1.0
- **Database**: MongoDB
- **Security**: Spring Security + JWT (`jjwt` 0.12.3)
- **API Docs**: Swagger / OpenAPI (`springdoc-openapi` 2.8.5)
- **External Services**: Twilio (for SMS OTP)
- **Build Tool**: Maven
- **Utilities**: Lombok (boilerplate reduction), Commons-Codec

## Architectural Pattern: Modular Monolith

### Why Modular Monolith?
- **Ease of Deployment**: Single artifact deployable anywhere.
- **Fast Development**: Code sharing and cross-module refactoring are straightforward.
- **Maintainability**: Hard boundaries between modules prevent "spaghetti code."

### Rules of Engagement
1. **Inter-module Communication**: Modules must communicate *only* through Service layers. A module (e.g., `dashboard`) cannot directly inject or access the Repository of another module (e.g., `user`).
2. **Encapsulation**: Repositories and internal logic should not bleed into other modules.
3. **Data Transfer**: Controllers must return DTOs, never raw Entities.
4. **Error Handling**: Uses a global exception handler.

## Directory Structure
The application is structured by feature rather than layer.

```text
com.stayo.stayo
├── auth/          # Authentication, OTP, JWT generation
├── common/        # Shared DTOs, Exceptions, global configurations
├── config/        # Security, Swagger, CORS, Mongo configs
├── content/       # Static content, Banners, Categories
├── dashboard/     # Aggregates data for the home screen
├── notification/  # In-app notifications
├── property/      # Core PG entities, amenities, pricing
├── search/        # Search filters, autocomplete, sorting
├── shared/        # Shared utilities
├── user/          # Profile, preferences, recent searches
└── wishlist/      # Saved properties
```

## Security & Authentication Flow
- **OTP-based Login**: Users authenticate using a phone number and OTP (via Twilio). 
- **Stateless JWT**: Upon successful OTP verification, the backend issues a JWT token.
- **Configuration**: Expiration is set to 24 hours (`86400000` ms). A static OTP fallback exists (`otp.use-static=true`, code: `123456`) for testing/development.

## Data Persistence
- Uses Spring Data MongoDB.
- Auto-index creation is enabled (`spring.mongodb.auto-index-creation=true`).
- Connection URI relies on standard MongoDB Atlas clusters.

## Internal APIs & Extensibility
The modular setup enables an easy transition to Microservices in the future, should the application outgrow a monolithic deployment. The `dashboard` module specifically acts as an internal API Gateway/Aggregator by orchestrating calls to `ContentService`, `PropertyService`, `SearchService`, etc.
