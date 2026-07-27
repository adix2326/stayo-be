# Search Module

## Business Purpose
The Search module is responsible for the autocomplete and location discovery aspects of the platform. It helps users find the cities or specific areas they want to search within.

## Responsibilities
- Manage the `City` entity.
- Provide default search coordinates/cities for new users based on IP or defaults.
- (Future) Provide autocomplete suggestions for localities and PG names.

## Folder Structure
```text
com.stayo.stayo.search
├── dto/
│   ├── CityResponse.java
│   ├── SearchDefaultDTO.java
│   └── SearchRequest.java
├── entity/
│   └── City.java
├── repository/
│   └── CityRepository.java
└── service/
    └── CityService.java
```

## Key Components

### `SearchRequest`
- The standard DTO used by the frontend to request filtered PG data.
- Contains: `query`, `gender`, `budgetMin`, `budgetMax`, `amenities`, `sortBy`, `page`, `size`.
- *Note: While defined in the Search module, this DTO is heavily utilized by the Property module (`PGController`).*

### `CityService`
- Interacts with the `CityRepository`.
- Used to populate the `SearchDefaultDTO` which the Dashboard uses to set the user's default city if they haven't explicitly chosen one.

## Future Improvements
- Introduce ElasticSearch integration to replace standard MongoDB regex queries for lightning-fast autocomplete and typo tolerance.
