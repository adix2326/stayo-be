# Content Module

## Business Purpose
The Content module is responsible for managing dynamic UI elements that populate the application, particularly the Dashboard. This includes promotional banners, categories, popular search areas, and quick filters. By isolating these elements into a dedicated module, StayO can update the app's look and feel without hardcoding values or requiring frontend deployments.

## Responsibilities
- Serve Hero Banners (promotional images, discounts).
- Serve Categories (e.g., "Boys PG", "Girls PG", "Premium").
- Serve Popular Searches (e.g., "Hinjewadi", "Koramangala").
- Serve Quick Filters.

## Folder Structure
```text
com.stayo.stayo.content
├── dto/
│   ├── BannerDTO.java
│   ├── CategoryDTO.java
│   ├── PopularSearchDTO.java
│   └── QuickFilterDTO.java
├── entity/
│   ├── Banner.java
│   ├── Category.java
│   ├── PopularSearch.java
│   └── QuickFilter.java
├── repository/
│   ├── BannerRepository.java
│   ├── CategoryRepository.java
│   ├── PopularSearchRepository.java
│   └── QuickFilterRepository.java
└── service/
    ├── BannerService.java
    ├── CategoryService.java
    ├── PopularSearchService.java
    └── QuickFilterService.java
```

## Data Flow & Architecture
This module generally does not expose its own Controllers. Instead, its services are invoked directly by the `DashboardService` to aggregate the UI.

1. **DashboardServiceImpl** calls `BannerService.getActiveBanners()`.
2. `BannerService` queries the `BannerRepository` for active banners.
3. The entities are mapped to `BannerDTO`s and returned to the Dashboard assembler.

## Future Improvements
- **Caching**: The content returned by these services is highly static (changes maybe once a week). These service methods are prime candidates for Redis caching (`@Cacheable`) to significantly reduce MongoDB read load on every app launch.
