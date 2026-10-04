# User Module

## Business Purpose
The User module manages everything related to the user's identity, profile details, and preferences on the StayO platform. It stores personal information, professional information, and handles profile image uploads. It also calculates profile completion percentages which can be used to incentivize users to provide more information.

## Responsibilities
- Retrieving basic user details for the currently logged-in user.
- Fetching and updating extended user profiles.
- Handling profile image uploads via the shared Cloudinary `storage` module.
- Tracking profile completion status automatically based on filled fields.
- Maintaining a list of wishlist property IDs.

## Folder Structure
```text
com.stayo.stayo.user
├── controller/
│   ├── UserController.java
│   └── UserProfileController.java
├── dto/
│   ├── UpdateProfileRequest.java
│   ├── UpdateUserDto.java
│   ├── UserProfileResponse.java
│   ├── UserResponseDto.java
│   └── UserSummaryDTO.java
├── entity/
│   ├── OtpRequest.java
│   ├── Role.java
│   └── User.java
├── repository/
│   ├── OtpRepository.java
│   └── UserRepository.java
└── service/
    └── UserProfileService.java
```

## Data Flow & Architecture

### Sequence Diagram: Update User Profile

```mermaid
sequenceDiagram
    participant Client
    participant UserProfileController
    participant AuthUtil
    participant UserProfileService
    participant UserRepository

    Client->>UserProfileController: PUT /api/user/profile (UpdateProfileRequest)
    UserProfileController->>AuthUtil: extractUserIdFromToken(token)
    AuthUtil-->>UserProfileController: userId
    UserProfileController->>UserProfileService: updateProfile(userId, request)
    UserProfileService->>UserRepository: findById(userId)
    UserRepository-->>UserProfileService: User entity
    
    UserProfileService->>UserProfileService: Update changed fields
    UserProfileService->>UserProfileService: calculateProfileCompleted()
    
    UserProfileService->>UserRepository: save(User)
    UserRepository-->>UserProfileService: Saved User
    UserProfileService-->>UserProfileController: UserProfileResponse
    UserProfileController-->>Client: 200 OK (UserProfileResponse)
```

## Public APIs
- **GET `/api/users/me`**: Fetches basic information for the currently authenticated user.
- **GET `/api/user/profile`**: Fetches the extended profile of the user, including completion percentage.
- **PUT `/api/user/profile`**: Updates the extended profile of the user.
- **POST `/api/user/profile/image`**: Uploads a profile image as `multipart/form-data`.
- **DELETE `/api/user/profile/image`**: Deletes the user's profile image and removes it from Cloudinary.

## Entities
- **User**: The central document in the `users` collection. Contains basic info, personal info, professional info, address, wishlist property IDs, roles (list), and audit timestamps.
- **Role**: Enum (`USER`, `PG_OWNER`, `ADMIN`, `SUPER_ADMIN`). `User.roles` is a list; `isAdmin()` is true for ADMIN or SUPER_ADMIN, `isSuperAdmin()` only for SUPER_ADMIN.
- **OtpRequest**: Tracks OTP requests for login/signup (Note: This is functionally related to Authentication but physically stored in the `user` module).

## Services
- **UserProfileService**: 
  - Retrieves and maps users to response DTOs.
  - Handles updates to all profile fields.
  - Dynamically calculates the completion percentage (out of 12 fields) and boolean `profileCompleted` status.
  - Uploads profile images through `FileStorageService` (Cloudinary), deleting the old image when a new one is uploaded.

## Repositories
- **UserRepository**: Spring Data MongoDB repository for the `users` collection.
- **OtpRepository**: Tracks OTP generation, expiry, and attempts.

## Validation & Exception Handling
- `UserNotFoundException`: Thrown when extracting a `userId` from a JWT that no longer exists in the database.
- File Upload Validation: Ensures the `MultipartFile` is not empty and stores it via `FileStorageService`.

## Security
- All endpoints extract the `userId` natively from the `Authorization` header JWT token via `AuthUtil`, meaning users can only ever access or modify their own data.

## Known Limitations & Technical Debt
- **Legacy image URLs**: records whose image URL still starts with `/uploads/` predate the Cloudinary migration and are dead links; no backfill exists.
- **Module Coupling**: `OtpRequest` and `OtpRepository` reside in the User module instead of the Auth module.

## Future Improvements
- Decouple `OtpRequest` from the user module.
