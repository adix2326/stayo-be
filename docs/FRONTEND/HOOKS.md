# Custom Hooks

StayO extracts complex UI logic into custom React hooks located within feature directories (e.g., `features/search/hooks`).

## Search Hooks
### `useSearch.js`
The central brain for the search experience.
- **State Managed**: `query` (string), `filters` (object), `recentSearches` (array).
- **Default Filters**: Gender (Any), PropertyType (Any), Budget (2k-20k), Food, Amenities, SortBy (Relevance).
- **Computed Properties**: Dynamically generates `activeFilterChips` for the UI based on which filters deviate from defaults.
- **Recent Searches**: Uses `localStorage` (key: `stayo_recent_searches`) to persist the last 6 searches. Provides `addToRecent`, `removeFromRecent`, and `clearRecent` methods.
- **Navigation**: The `doSearch()` method programmatically navigates to `/search-results`, passing the `query` and `filters` via React Router's `state` object.

### `useSearchSuggestions.js`
Handles the debounced API calls for autocomplete.
- Watches the user's keystrokes, applies a debounce (typically ~300ms), and fetches matching cities, localities, or PG names from the backend.
