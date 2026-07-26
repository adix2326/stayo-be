# Components

In addition to shared common components, StayO organizes domain-specific components directly within their respective feature folders (e.g., `features/search/components`). This ensures high cohesion.

## Property Components
### `PropertyCard.jsx`
A highly complex, reusable card component used across Search Results, Dashboard, and Wishlist.
- **Features**: Displays thumbnail (with fallback), pricing, location, rating, verified badge, gender categorization, and max 3 amenities.
- **Interactions**:
  - Entire card is clickable (navigates to Property Details).
  - Floating `Heart` icon toggles wishlist status. Includes a custom `animate-heart-beat` class for micro-interaction feedback.
- **Props**: `pg` (Object), `onClick` (Function), `wishlisted` (Boolean), `onWishlist` (Function), `index` (Number).

### `PropertyCardSkeleton.jsx`
The loading state equivalent of `PropertyCard`. Uses Tailwind `animate-pulse` and gray background stubs (`bg-slate-200`) to create a shimmer effect while data is being fetched.

## Search Components
### `SearchBar.jsx`
The primary input for searching locations or PG names.
- Often integrates with `SearchSuggestions.jsx` to render a dropdown of autocomplete results.

### `FilterBottomSheet.jsx`
A mobile-first drawer/bottom sheet for applying search filters.
- Uses Framer Motion for smooth sliding physics.
- Modifies the global or local search state using `useSearch()` hook.

### `ActiveFilterChips.jsx`
A horizontal scrollable row of active filters. Users can tap a chip to quickly remove a specific filter without opening the `FilterBottomSheet`.
