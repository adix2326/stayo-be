# API Integration

The frontend communicates with the Spring Boot backend using standard REST principles. All endpoints are centrally managed in `src/urls.js` to prevent hardcoded strings spread across components.

## The API Client
API calls are performed using either standard `fetch()` or `axios` instances.
- **Authorization**: A request interceptor (or manual header injection) attaches the JWT from local storage to the `Authorization: Bearer <token>` header for all protected routes.

## Handling the `ApiResponse<T>` Wrapper
The backend universally wraps data in an `ApiResponse` object (`{ success, message, data, timestamp }`).
- The frontend must specifically destructure or access `.data` from the JSON payload to reach the actual requested information (e.g., the array of `PGCardDTO`s or the `DashboardResponseDTO`).

## Loading and Error States
Every API-driven component must implement three critical UI states:
1. **Loading**: Displaying Skeleton components (e.g., `PropertyCardSkeleton`) rather than generic spinners whenever possible to prevent Layout Shift.
2. **Error**: Displaying graceful error boundaries or toast notifications (using the backend's provided `.message`).
3. **Empty**: Handling `data.length === 0` (e.g., rendering `EmptyState.jsx` in search results if no PGs match the filters).

## Pagination
For endpoints like `/api/properties/search`, the frontend consumes the `PageResponse<T>` nested inside the `ApiResponse`.
- The UI tracks `pageNumber` and uses the `last` boolean flag to determine whether to hide the "Load More" button or stop infinite scrolling.
