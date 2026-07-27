# Property Module

## Business Purpose
The Property module is the core domain of StayO. It exclusively manages Paying Guest (PG) accommodations. It handles the retrieval, filtering, and aggregation of PG details.

## Responsibilities
- Manage the `PG` entity (name, location, rent, amenities, rules, owner details).
- Provide search and pagination capabilities.
- Provide geographical (nearby) and recommendation algorithms.
- Track property views (`PGView`).

## Folder Structure
```text
com.stayo.stayo.property
├── controller/
│   └── PGController.java
├── dto/
│   ├── PGCardDTO.java
│   └── PGResponse.java
├── entity/
│   ├── PG.java
│   └── PGView.java
├── repository/
│   ├── PGRepository.java
│   └── PGViewRepository.java
└── service/
    ├── PGService.java
    ├── NearbyPGService.java
    └── RecommendationService.java
```

## Core Entities
### `PG.java`
- Contains fields: `id`, `name`, `rent`, `genderCategory` (BOYS/GIRLS/UNISEX), `amenities` (List), `verified` flag, `location` (geo-coordinates), etc.

## APIs
- **GET `/api/properties/search`**: Secured (but token is optional). Accepts a `SearchRequest` (budget, gender, amenities) and returns a `PageResponse<PGCardDTO>`.

## Services
### `PGServiceImpl`
- The heaviest service in the module. Uses MongoDB's `MongoTemplate` and `Criteria` API to construct complex dynamic queries based on the `SearchRequest`.
- Maps the resulting `PG` entities into `PGCardDTO`s.

### `NearbyPGService` & `RecommendationService`
- Invoked by the `DashboardService`.
- Currently returns static/mocked lists or basic queries. Intended to be expanded using MongoDB's `$geoNear` aggregation.

## Future Improvements
- Implement true Geospatial indexing (`2dsphere`) on the `location` field for the `NearbyPGService`.
- Expand the entity to include granular room availability (single, double, triple sharing) rather than a flat `rent`.
