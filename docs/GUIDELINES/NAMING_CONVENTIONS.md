# Naming Conventions

Consistency in naming is critical for maintainability and searchability across the massive StayO codebase. **Never abbreviate names** (e.g., use `PropertyRepository`, not `PropRepo`).

## Backend (Java)
### Classes & Interfaces (PascalCase)
- **Controllers**: Must end with `Controller` (e.g., `UserProfileController`).
- **Services**: Must end with `Service` for the interface and `ServiceImpl` for the implementation (e.g., `SearchService`, `SearchServiceImpl`).
- **Repositories**: Must end with `Repository` (e.g., `CityRepository`).
- **DTOs**: Must end with `Request` or `ResponseDTO` (e.g., `OtpVerifyRequest`, `DashboardResponseDTO`).
- **Entities**: Singular nouns representing the domain model (e.g., `User`, `Property`, `City`).
- **Exceptions**: Must end with `Exception` (e.g., `ProfileNotCompletedException`).

### Variables & Methods (camelCase)
- Use descriptive names: `calculateProfileCompletionPercentage()` instead of `calcPct()`.
- Boolean getters should use `is` or `has` (e.g., `isProfileCompleted()`).

### API Endpoints (Kebab-case)
- RESTful, lowercase, pluralized resources.
- Correct: `GET /api/properties/{id}`
- Incorrect: `GET /api/getPropertyById`

## Frontend (React/JS)
### Components & Pages (PascalCase)
- File names and component declarations must match exactly.
- **Pages**: Should ideally end with `Page` or reflect the screen (e.g., `SearchPage.jsx`, `UserDashboard.jsx`).
- **Components**: Nouns describing the UI element (e.g., `PropertyCard.jsx`, `FilterBottomSheet.jsx`).

### Hooks (camelCase)
- Must start with `use` (e.g., `useSearch.js`, `useSearchSuggestions.js`).

### Utilities & Functions (camelCase)
- Action-oriented verbs (e.g., `fetchDashboardData()`, `formatCurrency()`).
- File names for utilities should be `camelCase.js` (e.g., `dashboardCache.js`).

### CSS / Tailwind Classes
- Handled natively by Tailwind, but custom classes in `index.css` should use kebab-case (e.g., `.animate-heart-beat`).
