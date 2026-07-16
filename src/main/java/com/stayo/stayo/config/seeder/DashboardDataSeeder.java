package com.stayo.stayo.config.seeder;

import com.stayo.stayo.content.entity.Banner;
import com.stayo.stayo.content.entity.DashboardCategory;
import com.stayo.stayo.content.entity.PopularSearch;
import com.stayo.stayo.content.entity.QuickFilter;
import com.stayo.stayo.content.repository.BannerRepository;
import com.stayo.stayo.content.repository.DashboardCategoryRepository;
import com.stayo.stayo.content.repository.PopularSearchRepository;
import com.stayo.stayo.content.repository.QuickFilterRepository;
import com.stayo.stayo.booking.enums.RoomType;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.enums.GenderCategory;
import com.stayo.stayo.shared.enums.SearchType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DashboardDataSeeder implements CommandLineRunner {

    private final BannerRepository bannerRepository;
    private final DashboardCategoryRepository dashboardCategoryRepository;
    private final QuickFilterRepository quickFilterRepository;
    private final PopularSearchRepository popularSearchRepository;
    private final PGRepository pgRepository;

    @Override
    public void run(String... args) throws Exception {
        seedBanners();
        seedCategories();
        seedQuickFilters();
        seedPopularSearches();
        seedProperties();
    }

    // ─── BANNERS ─────────────────────────────────────────────────────────────────

    private void seedBanners() {
        bannerRepository.deleteAll();
        log.info("Seeding dashboard banners...");
        bannerRepository.saveAll(Arrays.asList(

            Banner.builder()
                .title("Verified PGs Near You")
                .subtitle("Trusted & inspected — move in with confidence")
                .imageUrl("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80")
                .ctaText("Explore Now")
                .redirectType("SEARCH")
                .redirectValue("verified=true")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),

            Banner.builder()
                .title("Premium PG Stays")
                .subtitle("AC rooms, meals & WiFi — all in one place")
                .imageUrl("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=800&q=80")
                .ctaText("View Premium")
                .redirectType("SEARCH")
                .redirectValue("luxury=true")
                .displayOrder(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),

            Banner.builder()
                .title("Zero Brokerage")
                .subtitle("Book directly — no hidden charges ever")
                .imageUrl("https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80")
                .ctaText("Book Now")
                .redirectType("SEARCH")
                .redirectValue("brokerage=zero")
                .displayOrder(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),

            Banner.builder()
                .title("Budget PGs Under ₹6,000")
                .subtitle("Comfortable stays for students & freshers")
                .imageUrl("https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80")
                .ctaText("Find Budget PG")
                .redirectType("SEARCH")
                .redirectValue("maxPrice=6000")
                .displayOrder(4)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        ));
    }

    // ─── CATEGORIES ──────────────────────────────────────────────────────────────

    private void seedCategories() {
        dashboardCategoryRepository.deleteAll();
        log.info("Seeding dashboard categories...");
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
                .subtitle("Under ₹6,000/mo")
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

    // ─── QUICK FILTERS ───────────────────────────────────────────────────────────

    private void seedQuickFilters() {
        quickFilterRepository.deleteAll();
        log.info("Seeding quick filters...");
        quickFilterRepository.saveAll(Arrays.asList(

            QuickFilter.builder().name("Unisex").icon("unisex").type("GENDER_UNISEX").displayOrder(1)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            QuickFilter.builder().name("Gents").icon("male").type("GENDER_BOYS").displayOrder(2)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            QuickFilter.builder().name("Ladies").icon("female").type("GENDER_GIRLS").displayOrder(3)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            QuickFilter.builder().name("Food").icon("restaurant").type("AMENITY_FOOD").displayOrder(4)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            QuickFilter.builder().name("AC").icon("ac").type("AMENITY_AC").displayOrder(5)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            QuickFilter.builder().name("Parking").icon("parking").type("AMENITY_PARKING").displayOrder(6)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        ));
    }

    // ─── POPULAR SEARCHES ────────────────────────────────────────────────────────

    private void seedPopularSearches() {
        popularSearchRepository.deleteAll();
        log.info("Seeding popular searches...");
        popularSearchRepository.saveAll(Arrays.asList(

            PopularSearch.builder().title("Hinjewadi").type(SearchType.LOCALITY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            PopularSearch.builder().title("Baner").type(SearchType.LOCALITY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            PopularSearch.builder().title("Kothrud").type(SearchType.LOCALITY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            PopularSearch.builder().title("Viman Nagar").type(SearchType.LOCALITY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            PopularSearch.builder().title("MIT WPU").type(SearchType.COLLEGE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            PopularSearch.builder().title("VIT Pune").type(SearchType.COLLEGE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            PopularSearch.builder().title("Pune").type(SearchType.CITY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            PopularSearch.builder().title("Mumbai").type(SearchType.CITY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        ));
    }

    // ─── PROPERTIES (PGs) ────────────────────────────────────────────────────────
    // Covers every test scenario:
    //   • All 3 gender categories: BOYS, GIRLS, UNISEX
    //   • Full rent range: ₹3,500 → ₹22,000
    //   • Every amenity combination (WiFi, Food, AC, Parking, Laundry, Power Backup, etc.)
    //   • Multiple cities: Pune, Mumbai, Bangalore
    //   • Multiple localities within each city
    //   • Featured = true (appears in dashboard carousels) & false
    //   • isActive = false (must NOT appear in search results)
    //   • Ratings from 2.8 → 5.0 (tests sort by rating)
    //   • Zero reviews (edge case)
    //   • Minimal amenities (edge case: empty-ish list)
    //   • High review count (for sort / UI display validation)

    private void seedProperties() {
        pgRepository.deleteAll();
        log.info("Seeding PG listings (comprehensive test dataset)...");

        pgRepository.saveAll(Arrays.asList(

            // ── BOYS PGs ──────────────────────────────────────────────────────────

            // 1. Boys | Hinjewadi | Budget | Featured | All core amenities
            PG.builder()
                .pgName("TechZone Boys PG")
                .description("Ideal for IT professionals in Hinjewadi. Spacious rooms with AC, high-speed WiFi and meals included.")
                .city("Pune").locality("Hinjewadi")
                .address("Phase 1, Near Infosys Gate 3, Hinjewadi, Pune 411057")
                .genderCategory(GenderCategory.BOYS)
                .rent(9500.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 9500.0, RoomType.DOUBLE, 9500.0 * 0.85, RoomType.TRIPLE, 9500.0 * 0.70, RoomType.FOUR_SHARING, 9500.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Food", "AC", "Laundry", "Power Backup", "CCTV"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.7).reviewCount(84)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(10)).updatedAt(LocalDateTime.now())
                .build(),

            // 2. Boys | Baner | Mid-range | No food | High rating
            PG.builder()
                .pgName("Elite Gents Residency")
                .description("Quiet and clean PG for working professionals. No food but fully equipped kitchen available.")
                .city("Pune").locality("Baner")
                .address("Baner Road, Near Baner Gym Khana, Baner, Pune 411045")
                .genderCategory(GenderCategory.BOYS)
                .rent(7200.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 7200.0, RoomType.DOUBLE, 7200.0 * 0.85, RoomType.TRIPLE, 7200.0 * 0.70, RoomType.FOUR_SHARING, 7200.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "AC", "Parking", "Power Backup", "Study Table", "Wardrobe"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.9).reviewCount(142)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(20)).updatedAt(LocalDateTime.now())
                .build(),

            // 3. Boys | Wakad | Budget | Minimal amenities | New listing
            PG.builder()
                .pgName("Budget Stay PG - Wakad")
                .description("Affordable PG for students and freshers. Basic amenities with good connectivity.")
                .city("Pune").locality("Wakad")
                .address("Wakad Chowk, Near D-Mart, Wakad, Pune 411057")
                .genderCategory(GenderCategory.BOYS)
                .rent(4500.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 4500.0, RoomType.DOUBLE, 4500.0 * 0.85, RoomType.TRIPLE, 4500.0 * 0.70, RoomType.FOUR_SHARING, 4500.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Power Backup"))
                .images(Collections.singletonList(
                    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(3.6).reviewCount(12)
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(3)).updatedAt(LocalDateTime.now())
                .build(),

            // 4. Boys | Kothrud | Premium | Food + AC + Parking | Low review count
            PG.builder()
                .pgName("Maple Suites for Men")
                .description("Premium furnished rooms for boys. Daily housekeeping, geyser, gym access and meals.")
                .city("Pune").locality("Kothrud")
                .address("Behind Croma, Karve Road, Kothrud, Pune 411038")
                .genderCategory(GenderCategory.BOYS)
                .rent(13500.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 13500.0, RoomType.DOUBLE, 13500.0 * 0.85, RoomType.TRIPLE, 13500.0 * 0.70, RoomType.FOUR_SHARING, 13500.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Food", "AC", "Parking", "Gym", "Laundry", "Geyser", "Housekeeping"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1617806118233-18e1de247200?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.3).reviewCount(7)
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(2)).updatedAt(LocalDateTime.now())
                .build(),

            // 5. Boys | Aundh | Mid | Parking + WiFi | Moderate rating
            PG.builder()
                .pgName("Greenleaf Gents PG")
                .description("Well-maintained PG in the heart of Aundh with easy access to public transport.")
                .city("Pune").locality("Aundh")
                .address("Aundh Road, Near Westend Mall, Aundh, Pune 411007")
                .genderCategory(GenderCategory.BOYS)
                .rent(6800.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 6800.0, RoomType.DOUBLE, 6800.0 * 0.85, RoomType.TRIPLE, 6800.0 * 0.70, RoomType.FOUR_SHARING, 6800.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Parking", "Laundry", "CCTV"))
                .images(Collections.singletonList(
                    "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(3.9).reviewCount(31)
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(45)).updatedAt(LocalDateTime.now())
                .build(),

            // 6. Boys | INACTIVE — must NOT show up in search
            PG.builder()
                .pgName("Closed PG Hinjewadi")
                .description("This PG is temporarily closed for renovation.")
                .city("Pune").locality("Hinjewadi")
                .address("Phase 2, Hinjewadi, Pune 411057")
                .genderCategory(GenderCategory.BOYS)
                .rent(8000.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 8000.0, RoomType.DOUBLE, 8000.0 * 0.85, RoomType.TRIPLE, 8000.0 * 0.70, RoomType.FOUR_SHARING, 8000.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "AC"))
                .images(Collections.singletonList(
                    "https://images.unsplash.com/photo-1493809842364-78817add7ffb?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(2.8).reviewCount(5)
                .isFeatured(false).isActive(false)   // <── inactive, should be hidden
                .createdAt(LocalDateTime.now().minusDays(90)).updatedAt(LocalDateTime.now())
                .build(),

            // ── GIRLS PGs ─────────────────────────────────────────────────────────

            // 7. Girls | Kothrud | Budget | Featured | Good security
            PG.builder()
                .pgName("Starlight Girls PG")
                .description("Safe and comfortable girls PG close to colleges with 24/7 security and biometric entry.")
                .city("Pune").locality("Kothrud")
                .address("Ideal Colony Road, Near MIT College, Kothrud, Pune 411038")
                .genderCategory(GenderCategory.GIRLS)
                .rent(5500.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 5500.0, RoomType.DOUBLE, 5500.0 * 0.85, RoomType.TRIPLE, 5500.0 * 0.70, RoomType.FOUR_SHARING, 5500.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Security", "CCTV", "Geyser", "Wardrobe"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.6).reviewCount(63)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(15)).updatedAt(LocalDateTime.now())
                .build(),

            // 8. Girls | Viman Nagar | Premium | Full amenities | Top rated
            PG.builder()
                .pgName("Bliss Women's Residency")
                .description("Luxury girls PG with air-conditioned rooms, daily meals, gym and swimming pool access.")
                .city("Pune").locality("Viman Nagar")
                .address("Clover Park, Viman Nagar, Pune 411014")
                .genderCategory(GenderCategory.GIRLS)
                .rent(15000.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 15000.0, RoomType.DOUBLE, 15000.0 * 0.85, RoomType.TRIPLE, 15000.0 * 0.70, RoomType.FOUR_SHARING, 15000.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Food", "AC", "Gym", "Swimming Pool", "Laundry", "Housekeeping", "Parking"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1502672023488-70e25813eb80?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(5.0).reviewCount(211)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(60)).updatedAt(LocalDateTime.now())
                .build(),

            // 9. Girls | Hadapsar | Budget | Zero reviews (edge case)
            PG.builder()
                .pgName("Comfort Corner Girls PG")
                .description("Brand new girls PG in Hadapsar, near Magarpatta City. Spacious rooms at affordable rent.")
                .city("Pune").locality("Hadapsar")
                .address("Magarpatta Road, Hadapsar, Pune 411028")
                .genderCategory(GenderCategory.GIRLS)
                .rent(4200.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 4200.0, RoomType.DOUBLE, 4200.0 * 0.85, RoomType.TRIPLE, 4200.0 * 0.70, RoomType.FOUR_SHARING, 4200.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Power Backup"))
                .images(Collections.singletonList(
                    "https://images.unsplash.com/photo-1598928636135-d146006ff4be?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(0.0).reviewCount(0)   // <── zero reviews edge case
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1)).updatedAt(LocalDateTime.now())
                .build(),

            // 10. Girls | Aundh | Mid | Food + WiFi | Featured
            PG.builder()
                .pgName("Sunrise Ladies PG")
                .description("Homely atmosphere with nutritious meals twice a day. Walking distance to Westend Mall.")
                .city("Pune").locality("Aundh")
                .address("DP Road, Near Aundh Hospital, Aundh, Pune 411007")
                .genderCategory(GenderCategory.GIRLS)
                .rent(7800.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 7800.0, RoomType.DOUBLE, 7800.0 * 0.85, RoomType.TRIPLE, 7800.0 * 0.70, RoomType.FOUR_SHARING, 7800.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Food", "Geyser", "Laundry", "CCTV"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1571508601891-ca5e7a713859?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.4).reviewCount(55)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(30)).updatedAt(LocalDateTime.now())
                .build(),

            // 11. Girls | Baner | High rent | No food | Lowest-rated active PG
            PG.builder()
                .pgName("Serene Villa Girls Stay")
                .description("Independent villa-style accommodation for working women. Self-cook facility. Peaceful locality.")
                .city("Pune").locality("Baner")
                .address("Sus Road, Baner, Pune 411021")
                .genderCategory(GenderCategory.GIRLS)
                .rent(11000.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 11000.0, RoomType.DOUBLE, 11000.0 * 0.85, RoomType.TRIPLE, 11000.0 * 0.70, RoomType.FOUR_SHARING, 11000.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "AC", "Parking", "Study Table"))
                .images(Collections.singletonList(
                    "https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(3.1).reviewCount(9)
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(75)).updatedAt(LocalDateTime.now())
                .build(),

            // ── UNISEX PGs ────────────────────────────────────────────────────────

            // 12. Unisex | Hinjewadi | Premium | Featured | All amenities
            PG.builder()
                .pgName("Urban Nest Co-Living PG")
                .description("Modern co-living space for IT professionals. Private and shared rooms available. Pool, gym and cafe on premises.")
                .city("Pune").locality("Hinjewadi")
                .address("Phase 3, Near Wipro Gate, Hinjewadi, Pune 411057")
                .genderCategory(GenderCategory.UNISEX)
                .rent(12500.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 12500.0, RoomType.DOUBLE, 12500.0 * 0.85, RoomType.TRIPLE, 12500.0 * 0.70, RoomType.FOUR_SHARING, 12500.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Food", "AC", "Gym", "Parking", "Laundry", "Swimming Pool", "Power Backup", "Housekeeping", "CCTV"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1493809842364-78817add7ffb?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.8).reviewCount(197)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(8)).updatedAt(LocalDateTime.now())
                .build(),

            // 13. Unisex | Koramangala | Bangalore | Featured | Mid-range
            PG.builder()
                .pgName("Koramangala Hub PG")
                .description("Vibrant unisex PG in the heart of Koramangala. Close to startups, cafes and shopping.")
                .city("Bangalore").locality("Koramangala")
                .address("5th Block, Koramangala, Bangalore 560095")
                .genderCategory(GenderCategory.UNISEX)
                .rent(10500.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 10500.0, RoomType.DOUBLE, 10500.0 * 0.85, RoomType.TRIPLE, 10500.0 * 0.70, RoomType.FOUR_SHARING, 10500.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "AC", "Food", "Laundry", "CCTV", "Power Backup"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.5).reviewCount(103)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(40)).updatedAt(LocalDateTime.now())
                .build(),

            // 14. Unisex | Andheri | Mumbai | Budget | No AC
            PG.builder()
                .pgName("Andheri West Budget PG")
                .description("Pocket-friendly unisex PG near Andheri metro station. Ideal for media and entertainment professionals.")
                .city("Mumbai").locality("Andheri")
                .address("Oshiwara, Near Andheri Station, Andheri West, Mumbai 400053")
                .genderCategory(GenderCategory.UNISEX)
                .rent(8800.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 8800.0, RoomType.DOUBLE, 8800.0 * 0.85, RoomType.TRIPLE, 8800.0 * 0.70, RoomType.FOUR_SHARING, 8800.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Parking", "Laundry", "Power Backup"))
                .images(Collections.singletonList(
                    "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(3.8).reviewCount(28)
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(55)).updatedAt(LocalDateTime.now())
                .build(),

            // 15. Unisex | Bandra | Mumbai | Luxury | High rent
            PG.builder()
                .pgName("Bandra Premium Living")
                .description("Boutique PG in Bandra West. Sea breeze balconies, private bathrooms, Netflix-enabled smart TVs.")
                .city("Mumbai").locality("Bandra")
                .address("Pali Hill, Bandra West, Mumbai 400050")
                .genderCategory(GenderCategory.UNISEX)
                .rent(22000.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 22000.0, RoomType.DOUBLE, 22000.0 * 0.85, RoomType.TRIPLE, 22000.0 * 0.70, RoomType.FOUR_SHARING, 22000.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "AC", "Food", "Gym", "Parking", "Laundry", "TV", "Housekeeping", "Attached Bathroom"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.9).reviewCount(76)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(120)).updatedAt(LocalDateTime.now())
                .build(),

            // 16. Unisex | Indiranagar | Bangalore | Mid | Parking focus
            PG.builder()
                .pgName("Indiranagar Shared Living")
                .description("Trendy PG in Indiranagar for young professionals. Bicycle parking, rooftop lounge and weekly events.")
                .city("Bangalore").locality("Indiranagar")
                .address("100 Feet Road, Indiranagar, Bangalore 560038")
                .genderCategory(GenderCategory.UNISEX)
                .rent(11200.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 11200.0, RoomType.DOUBLE, 11200.0 * 0.85, RoomType.TRIPLE, 11200.0 * 0.70, RoomType.FOUR_SHARING, 11200.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Parking", "AC", "Laundry", "Power Backup", "CCTV", "Study Table"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.2).reviewCount(47)
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(25)).updatedAt(LocalDateTime.now())
                .build(),

            // 17. Unisex | Kothrud Pune | Cheapest listing (price filter edge case)
            PG.builder()
                .pgName("Frugal Rooms Kothrud")
                .description("Extremely affordable PG for students on tight budgets. Shared rooms with basic facilities.")
                .city("Pune").locality("Kothrud")
                .address("Karve Nagar, Kothrud, Pune 411052")
                .genderCategory(GenderCategory.UNISEX)
                .rent(3500.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 3500.0, RoomType.DOUBLE, 3500.0 * 0.85, RoomType.TRIPLE, 3500.0 * 0.70, RoomType.FOUR_SHARING, 3500.0 * 0.55))
                .amenities(Arrays.asList("WiFi"))
                .images(Collections.singletonList(
                    "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(3.3).reviewCount(20)
                .isFeatured(false).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(180)).updatedAt(LocalDateTime.now())
                .build(),

            // 18. Boys | Whitefield | Bangalore | IT Hub | High demand
            PG.builder()
                .pgName("Whitefield Men's Nest")
                .description("Conveniently located PG for IT professionals near ITPL. Shuttle service available to major tech parks.")
                .city("Bangalore").locality("Whitefield")
                .address("Near ITPL Main Gate, Whitefield, Bangalore 560066")
                .genderCategory(GenderCategory.BOYS)
                .rent(9800.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 9800.0, RoomType.DOUBLE, 9800.0 * 0.85, RoomType.TRIPLE, 9800.0 * 0.70, RoomType.FOUR_SHARING, 9800.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "AC", "Food", "Parking", "Laundry", "Shuttle Service", "Power Backup"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1598928636135-d146006ff4be?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1556910103-1c02745aae4d?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.6).reviewCount(119)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(35)).updatedAt(LocalDateTime.now())
                .build(),

            // 19. Girls | Powai | Mumbai | Near Engineering College
            PG.builder()
                .pgName("Powai Girls Haven")
                .description("Safe girls PG just 500m from IIT Bombay and Hiranandani. 24/7 security with visitor log.")
                .city("Mumbai").locality("Powai")
                .address("Hiranandani Gardens, Powai, Mumbai 400076")
                .genderCategory(GenderCategory.GIRLS)
                .rent(13000.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 13000.0, RoomType.DOUBLE, 13000.0 * 0.85, RoomType.TRIPLE, 13000.0 * 0.70, RoomType.FOUR_SHARING, 13000.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "Food", "AC", "Security", "CCTV", "Laundry", "Geyser"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1502672023488-70e25813eb80?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.7).reviewCount(88)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(50)).updatedAt(LocalDateTime.now())
                .build(),

            // 20. Unisex | Pune | Highest rent listing (price_desc sort edge case)
            PG.builder()
                .pgName("The Grand PG Suites")
                .description("Ultra-luxury studio PG suites in Kalyani Nagar. Private kitchen, balcony, concierge and valet parking.")
                .city("Pune").locality("Kalyani Nagar")
                .address("North Main Road, Kalyani Nagar, Pune 411006")
                .genderCategory(GenderCategory.UNISEX)
                .rent(20000.0)
                .rentByRoomType(Map.of(RoomType.SINGLE, 20000.0, RoomType.DOUBLE, 20000.0 * 0.85, RoomType.TRIPLE, 20000.0 * 0.70, RoomType.FOUR_SHARING, 20000.0 * 0.55))
                .amenities(Arrays.asList("WiFi", "AC", "Food", "Gym", "Parking", "Laundry", "TV", "Housekeeping", "Attached Bathroom", "Balcony", "Swimming Pool"))
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1617806118233-18e1de247200?auto=format&fit=crop&w=600&q=80"
                ))
                .rating(4.8).reviewCount(34)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(5)).updatedAt(LocalDateTime.now())
                .build()

        ));

        log.info("Seeded {} active PG listings (+ 1 inactive) across Pune, Bangalore & Mumbai.", 19);
    }
}
