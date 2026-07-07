package com.stayo.stayo.config;

import com.stayo.stayo.property.entity.*;
import com.stayo.stayo.property.enums.GenderCategory;
import com.stayo.stayo.property.enums.PropertyType;
import com.stayo.stayo.property.enums.SearchType;
import com.stayo.stayo.property.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class DashboardDataSeeder implements CommandLineRunner {

    private final BannerRepository bannerRepository;
    private final DashboardCategoryRepository dashboardCategoryRepository;
    private final QuickFilterRepository quickFilterRepository;
    private final PopularSearchRepository popularSearchRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public void run(String... args) throws Exception {
        seedBanners();
        seedCategories();
        seedQuickFilters();
        seedPopularSearches();
        seedProperties();
    }

    private void seedBanners() {
        if (bannerRepository.count() == 0) {
            log.info("Seeding default dashboard banners...");
            bannerRepository.saveAll(Arrays.asList(
                    Banner.builder()
                            .title("Verified PGs")
                            .subtitle("Stay with trust")
                            .imageUrl("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=600&q=80")
                            .ctaText("Explore Now")
                            .redirectType("SEARCH")
                            .redirectValue("verified=true")
                            .displayOrder(1)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Banner.builder()
                            .title("Premium Co-Living")
                            .subtitle("Modern spaces for professionals")
                            .imageUrl("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80")
                            .ctaText("View Luxury")
                            .redirectType("SEARCH")
                            .redirectValue("luxury=true")
                            .displayOrder(2)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }
    }

    private void seedCategories() {
        if (dashboardCategoryRepository.count() == 0) {
            log.info("Seeding default dashboard categories...");
            dashboardCategoryRepository.saveAll(Arrays.asList(
                    DashboardCategory.builder()
                            .title("Near Colleges")
                            .subtitle("Walk to class")
                            .icon("school")
                            .displayOrder(1)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    DashboardCategory.builder()
                            .title("Budget")
                            .subtitle("Affordable stays")
                            .icon("payments")
                            .displayOrder(2)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    DashboardCategory.builder()
                            .title("Luxury")
                            .subtitle("Premium amenities")
                            .icon("star")
                            .displayOrder(3)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }
    }

    private void seedQuickFilters() {
        if (quickFilterRepository.count() == 0) {
            log.info("Seeding default quick filters...");
            quickFilterRepository.saveAll(Arrays.asList(
                    QuickFilter.builder()
                            .name("Boys")
                            .icon("male")
                            .type("GENDER_BOYS")
                            .displayOrder(1)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    QuickFilter.builder()
                            .name("Girls")
                            .icon("female")
                            .type("GENDER_GIRLS")
                            .displayOrder(2)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    QuickFilter.builder()
                            .name("WiFi")
                            .icon("wifi")
                            .type("AMENITY_WIFI")
                            .displayOrder(3)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    QuickFilter.builder()
                            .name("Food")
                            .icon("restaurant")
                            .type("AMENITY_FOOD")
                            .displayOrder(4)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }
    }

    private void seedPopularSearches() {
        if (popularSearchRepository.count() == 0) {
            log.info("Seeding default popular searches...");
            popularSearchRepository.saveAll(Arrays.asList(
                    PopularSearch.builder()
                            .title("Hinjewadi")
                            .type(SearchType.LOCALITY)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PopularSearch.builder()
                            .title("Baner")
                            .type(SearchType.LOCALITY)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PopularSearch.builder()
                            .title("MIT WPU")
                            .type(SearchType.COLLEGE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PopularSearch.builder()
                            .title("VIT Pune")
                            .type(SearchType.COLLEGE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PopularSearch.builder()
                            .title("Pune")
                            .type(SearchType.CITY)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }
    }

    private void seedProperties() {
        if (propertyRepository.count() == 0) {
            log.info("Seeding default properties...");
            propertyRepository.saveAll(Arrays.asList(
                    Property.builder()
                            .propertyName("Elite Boy's PG")
                            .description("Luxury stay with high speed internet and food included")
                            .city("Pune")
                            .locality("Hinjewadi")
                            .address("Phase 1, Hinjewadi, Pune")
                            .genderCategory(GenderCategory.BOYS)
                            .propertyType(PropertyType.PG)
                            .rent(8500.0)
                            .amenities(Arrays.asList("WiFi", "Food", "AC"))
                            .images(Collections.singletonList("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=600&q=80"))
                            .rating(4.5)
                            .reviewCount(25)
                            .isFeatured(true)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Property.builder()
                            .propertyName("Starlight Hostel")
                            .description("Comfy girls PG close to college with strong security")
                            .city("Pune")
                            .locality("Kothrud")
                            .address("Ideal Colony, Kothrud, Pune")
                            .genderCategory(GenderCategory.GIRLS)
                            .propertyType(PropertyType.PG)
                            .rent(6000.0)
                            .amenities(Arrays.asList("WiFi", "Security", "Parking"))
                            .images(Collections.singletonList("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80"))
                            .rating(4.2)
                            .reviewCount(18)
                            .isFeatured(true)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }
    }
}
