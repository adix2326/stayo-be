# Development Guidelines

These guidelines define the operational workflow and architectural rules for developing features in StayO.

## Backend Modular Boundaries
StayO uses a **Modular Monolith** architecture.
- **Isolation**: Each module (Auth, User, Dashboard, Property) owns its Controllers, Services, DTOs, and Repositories.
- **Communication**: If Module A needs data from Module B, it must call Module B's Service. 
  - *Example*: `DashboardServiceImpl` calls `PropertyService.getNearbyPGs()`. It **cannot** auto-wire `PropertyRepository`.

## API Contracts & Data Flow
- **Response Wrapper**: Every single successful API response must be wrapped in `{ success, message, data, timestamp }`.
- **Validation**: Use Jakarta Validation (`@NotBlank`, `@Pattern`, `@Size`) on Request DTOs. Do not write manual `if (dto.getName() == null)` checks in the controller.
- **Hardcoding**: Never hardcode business data (e.g., Categories, Cities, Filter options) in the frontend. All dynamic lists must be driven by backend APIs.

## Frontend UI/UX
- **Whitespace & Minimalisim**: Prefer whitespace over dense UIs. The app should feel premium, similar to Airbnb or CRED.
- **Loading States**: Always use Shimmer/Skeleton loaders (`PropertyCardSkeleton`) for initial data fetches. Avoid generic full-screen spinners to prevent Layout Shift.
- **Forms**: Use controlled inputs. Disable submit buttons while requests are in flight.
- **Accessibility**: Ensure touch targets are at least 44px (mobile standard).

## Performance Optimization
- **Backend**:
  - Use `CompletableFuture` for independent parallel fetching (e.g., in `DashboardService`).
  - Index frequently searched MongoDB fields.
  - Avoid embedding massive documents; use references for unbounded arrays (e.g., User -> Bookings).
- **Frontend**:
  - Lazy load routes (`React.lazy()`) if bundle size grows.
  - Lazy load images using native `loading="lazy"`.
  - Memoize expensive child components using `React.memo` to prevent cascading re-renders during state updates.

## Workflow Before Creating New Code
Before you build a new feature or file:
1. **Search**: Check if a similar component, utility, or service already exists.
2. **Reuse**: Import and use existing shared elements.
3. **Extend**: If an existing component is close to what you need, extend its props rather than creating a duplicate.
