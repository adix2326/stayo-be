# State Management

StayO employs a hybrid approach to State Management, prioritizing local state and lifting state only when absolutely necessary, to avoid the overhead of heavy global stores like Redux.

## 1. Local Component State
Managed via `useState` and `useReducer`. 
- Used for UI-specific transient states like toggling modals, handling input field values, or managing the `heartAnimating` state inside a `PropertyCard`.

## 2. Feature-Level State (Custom Hooks)
Complex logic is extracted into custom hooks to share state logic without necessarily sharing the state instance.
- Example: `useSearch.js` manages the massive object of filters, query text, and recent searches, returning simple update functions (`setFilter`, `resetFilters`).

## 3. Global / Server State (Future API Layer)
Currently, data is fetched and stored locally or passed via Router State (e.g., passing filters to `/search-results`).
- **Planned Architecture**: The application is architected to adopt **TanStack React Query**.
- React Query will handle all server state (Dashboards, Property details, Wishlists), providing out-of-the-box caching, deduplication, and background refetching, eliminating the need for a global UI state manager for API data.

## 4. LocalStorage
Used sparingly for persisting non-sensitive user preferences across sessions.
- `stayo_recent_searches`: Array of strings representing past searches.
- Auth Tokens: JWTs are stored securely to maintain sessions.
