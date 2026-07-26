# Coding Standards

StayO strictly enforces clean, maintainable, and predictable code across both the Frontend and Backend. All developers and AI assistants must adhere to these principles.

## Core Principles
- **SOLID**: Follow single responsibility, open-closed, Liskov substitution, interface segregation, and dependency inversion.
- **DRY (Don't Repeat Yourself)**: Never duplicate business logic, UI components, or DTOs.
- **KISS (Keep It Simple, Stupid)**: Avoid over-engineering. Write code that is easy to read and understand.
- **YAGNI (You Aren't Gonna Need It)**: Do not implement features, fields, or abstractions until they are strictly required.

## Backend (Spring Boot / Java)
- **Controller Layer**: 
  - Must only handle HTTP routing, request validation (`@Valid`), delegating to services, and wrapping the return in `ApiResponse`.
  - **NEVER** place business logic in a controller.
- **Service Layer**: 
  - Contains all business logic and orchestration.
  - Services may call other services (e.g., `DashboardService` calls `PropertyService`).
  - **NEVER** call another module's repository directly.
- **Repository Layer**: 
  - Interfaces extending `MongoRepository`. 
  - No business logic or mapping belongs here.
- **Data Integrity**: 
  - Never return Database Entities directly from APIs. Always map them to DTOs.

## Frontend (React / Vite)
- **Component Design**: 
  - Use Functional Components and Hooks. No class components.
  - Keep components small (preferably < 300 lines). Break complex UIs into smaller, single-responsibility children.
- **Styling**: 
  - Use Tailwind utility classes. Avoid custom CSS unless absolutely necessary (e.g., for complex keyframe animations).
  - strictly adhere to the defined Design System colors and spacing (e.g., `p-4`, `p-6`—never arbitrary values like `p-[17px]`).
- **Responsive**: 
  - Build Mobile-First. Ensure flawless rendering on widths from 360px up to 430px.
- **State**:
  - Lift state only when required. Keep temporary UI state local to the component.

## Error Handling & Logging
- **Backend**: 
  - Use `@RestControllerAdvice` (`GlobalExceptionHandler`). 
  - Throw custom semantic exceptions (e.g., `UserNotFoundException`, `InvalidOtpException`).
  - Log using SLF4J. Never use `System.out.println()`.
- **Frontend**: 
  - Gracefully handle API errors using the `message` from the `ApiResponse`.
  - Provide fallback/empty states and Skeleton loaders.
