# Wishlist Module

## Business Purpose
The Wishlist module allows users to save properties they are interested in for later viewing.

## Responsibilities
- Add a PG to a user's wishlist.
- Remove a PG from a user's wishlist.
- Retrieve all wishlisted PGs for a specific user.
- **Data Integrity**: Ensure users can only interact with their own wishlist.

## Folder Structure
```text
com.stayo.stayo.wishlist
├── controller/
│   └── WishlistController.java
└── service/
    ├── WishlistService.java
    └── impl/
        └── WishlistServiceImpl.java
```

*Note: The Wishlist module does not have its own Entity. It modifies the `wishlist` array embedded within the `User` entity.*

## Data Flow & Architecture
Because the wishlist is stored as an array of `propertyIds` inside the `User` collection, the `WishlistServiceImpl` must communicate with the `UserRepository` (violating strict module boundaries) OR communicate through the `UserService`.
Currently, it uses the `UserService` and `PGService` to fulfill requests.

## APIs
- **POST `/api/wishlist/add/{propertyId}`**: Requires Token. Adds the ID.
- **POST `/api/wishlist/remove/{propertyId}`**: Requires Token. Removes the ID.
- **GET `/api/wishlist`**: Requires Token. Retrieves the user, gets the array of IDs, calls `PGService.getPGsByIds(...)`, and returns a `List<PGCardDTO>`.

## Future Improvements
- Refactor to use a dedicated `Wishlist` collection (e.g., `{ userId, propertyId, addedAt }`) instead of embedding an unbounded array inside the `User` document. Embedding arrays can lead to MongoDB document size limits and poor indexing performance as the array grows.
