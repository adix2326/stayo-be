package com.stayo.stayo.config.seeder;

import com.stayo.stayo.content.entity.Banner;
import com.stayo.stayo.content.entity.DashboardCategory;
import com.stayo.stayo.content.entity.PopularSearch;
import com.stayo.stayo.content.entity.QuickFilter;
import com.stayo.stayo.content.repository.BannerRepository;
import com.stayo.stayo.content.repository.DashboardCategoryRepository;
import com.stayo.stayo.content.repository.PopularSearchRepository;
import com.stayo.stayo.content.repository.QuickFilterRepository;
import com.stayo.stayo.property.entity.Image;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.entity.PGImages;
import com.stayo.stayo.property.entity.SharingType;
import com.stayo.stayo.property.enums.RoomSharingType;
import com.stayo.stayo.property.repository.PGImagesRepository;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.enums.Amenity;
import com.stayo.stayo.shared.enums.GenderCategory;
import com.stayo.stayo.shared.enums.SearchType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DashboardDataSeeder implements CommandLineRunner {

    private final BannerRepository bannerRepository;
    private final DashboardCategoryRepository dashboardCategoryRepository;
    private final QuickFilterRepository quickFilterRepository;
    private final PopularSearchRepository popularSearchRepository;
    private final PGRepository pgRepository;
    private final PGImagesRepository pgImagesRepository;

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

        List<PG> savedProperties = pgRepository.saveAll(Arrays.asList(

            // ── BOYS PGs ──────────────────────────────────────────────────────────

            // 1. Boys | Hinjewadi | Budget | Featured | All core amenities
            PG.builder()
                .pgName("TechZone Boys PG")
                .description("Ideal for IT professionals in Hinjewadi. Spacious rooms with AC, high-speed WiFi and meals included.")
                .city("Pune").locality("Hinjewadi")
                .address("Phase 1, Near Infosys Gate 3, Hinjewadi, Pune 411057")
                .genderCategory(GenderCategory.GENTS)
                .sharingType(sharingTypes(9500.0))
                .amenities(amenitiesOf("WiFi", "Food", "AC", "Laundry", "Power Backup", "CCTV"))
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
                .genderCategory(GenderCategory.GENTS)
                .sharingType(sharingTypes(7200.0))
                .amenities(amenitiesOf("WiFi", "AC", "Parking", "Power Backup", "Study Table", "Wardrobe"))
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
                .genderCategory(GenderCategory.GENTS)
                .sharingType(sharingTypes(4500.0))
                .amenities(amenitiesOf("WiFi", "Power Backup"))
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
                .genderCategory(GenderCategory.GENTS)
                .sharingType(sharingTypes(13500.0))
                .amenities(amenitiesOf("WiFi", "Food", "AC", "Parking", "Gym", "Laundry", "Geyser", "Housekeeping"))
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
                .genderCategory(GenderCategory.GENTS)
                .sharingType(sharingTypes(6800.0))
                .amenities(amenitiesOf("WiFi", "Parking", "Laundry", "CCTV"))
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
                .genderCategory(GenderCategory.GENTS)
                .sharingType(sharingTypes(8000.0))
                .amenities(amenitiesOf("WiFi", "AC"))
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
                .genderCategory(GenderCategory.LADIES)
                .sharingType(sharingTypes(5500.0))
                .amenities(amenitiesOf("WiFi", "Security", "CCTV", "Geyser", "Wardrobe"))
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
                .genderCategory(GenderCategory.LADIES)
                .sharingType(sharingTypes(15000.0))
                .amenities(amenitiesOf("WiFi", "Food", "AC", "Gym", "Swimming Pool", "Laundry", "Housekeeping", "Parking"))
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
                .genderCategory(GenderCategory.LADIES)
                .sharingType(sharingTypes(4200.0))
                .amenities(amenitiesOf("WiFi", "Power Backup"))
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
                .genderCategory(GenderCategory.LADIES)
                .sharingType(sharingTypes(7800.0))
                .amenities(amenitiesOf("WiFi", "Food", "Geyser", "Laundry", "CCTV"))
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
                .genderCategory(GenderCategory.LADIES)
                .sharingType(sharingTypes(11000.0))
                .amenities(amenitiesOf("WiFi", "AC", "Parking", "Study Table"))
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
                .sharingType(sharingTypes(12500.0))
                .amenities(amenitiesOf("WiFi", "Food", "AC", "Gym", "Parking", "Laundry", "Swimming Pool", "Power Backup", "Housekeeping", "CCTV"))
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
                .sharingType(sharingTypes(10500.0))
                .amenities(amenitiesOf("WiFi", "AC", "Food", "Laundry", "CCTV", "Power Backup"))
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
                .sharingType(sharingTypes(8800.0))
                .amenities(amenitiesOf("WiFi", "Parking", "Laundry", "Power Backup"))
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
                .sharingType(sharingTypes(22000.0))
                .amenities(amenitiesOf("WiFi", "AC", "Food", "Gym", "Parking", "Laundry", "TV", "Housekeeping", "Attached Bathroom"))
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
                .sharingType(sharingTypes(11200.0))
                .amenities(amenitiesOf("WiFi", "Parking", "AC", "Laundry", "Power Backup", "CCTV", "Study Table"))
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
                .sharingType(sharingTypes(3500.0))
                .amenities(amenitiesOf("WiFi"))
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
                .genderCategory(GenderCategory.GENTS)
                .sharingType(sharingTypes(9800.0))
                .amenities(amenitiesOf("WiFi", "AC", "Food", "Parking", "Laundry", "Shuttle Service", "Power Backup"))
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
                .genderCategory(GenderCategory.LADIES)
                .sharingType(sharingTypes(13000.0))
                .amenities(amenitiesOf("WiFi", "Food", "AC", "Security", "CCTV", "Laundry", "Geyser"))
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
                .sharingType(sharingTypes(20000.0))
                .amenities(amenitiesOf("WiFi", "AC", "Food", "Gym", "Parking", "Laundry", "TV", "Housekeeping", "Attached Bathroom", "Balcony", "Swimming Pool"))
                .rating(4.8).reviewCount(34)
                .isFeatured(true).isActive(true)
                .createdAt(LocalDateTime.now().minusDays(5)).updatedAt(LocalDateTime.now())
                .build()

        ));

        seedPropertyImages(savedProperties);

        log.info("Seeded {} active PG listings (+ 1 inactive) across Pune, Bangalore & Mumbai.", 19);
    }

    // Images now live in their own PGImages collection (see Phase C of the
    // schema restructuring) — this mirrors, in the same order as
    // seedProperties() above, the photo sets that used to be inline
    // PG.images(...) calls.
    private void seedPropertyImages(List<PG> savedProperties) {
        pgImagesRepository.deleteAll();

        List<List<String>> imagesByProperty = Arrays.asList(
            urls("1555854877-bab0e564b8d5", "1631049307264-da0ec9d70304"),
            urls("1600121848594-d8644e57abab", "1586023492125-27b2c045efd7"),
            urls("1560448204-e02f11c3d0e2"),
            urls("1617806118233-18e1de247200", "1595526114035-0d45ed16cfbf"),
            urls("1540518614846-7eded433c457"),
            urls("1493809842364-78817add7ffb"),
            urls("1505693416388-ac5ce068fe85", "1484101403633-562f891dc89a"),
            urls("1564013799919-ab600027ffc6", "1502672023488-70e25813eb80"),
            urls("1598928636135-d146006ff4be"),
            urls("1556909114-f6e7ad7d3136", "1571508601891-ca5e7a713859"),
            urls("1600047509807-ba8f99d2cdde"),
            urls("1522708323590-d24dbb6b0267", "1560448204-e02f11c3d0e2", "1493809842364-78817add7ffb"),
            urls("1631049307264-da0ec9d70304", "1600121848594-d8644e57abab"),
            urls("1586023492125-27b2c045efd7"),
            urls("1600585154340-be6161a56a0c", "1566073771259-6a8506099945"),
            urls("1484101403633-562f891dc89a", "1505693416388-ac5ce068fe85"),
            urls("1540518614846-7eded433c457"),
            urls("1598928636135-d146006ff4be", "1556910103-1c02745aae4d"),
            urls("1564013799919-ab600027ffc6", "1502672023488-70e25813eb80"),
            urls("1566073771259-6a8506099945", "1617806118233-18e1de247200")
        );

        List<PGImages> pgImagesList = new ArrayList<>();
        for (int i = 0; i < savedProperties.size() && i < imagesByProperty.size(); i++) {
            String pgId = savedProperties.get(i).getId();
            List<String> photoIds = imagesByProperty.get(i);

            List<Image> images = new ArrayList<>();
            for (int j = 0; j < photoIds.size(); j++) {
                images.add(Image.builder()
                        .fileId(java.util.UUID.randomUUID().toString())
                        .contentType("image/jpeg")
                        .fileUrl(photoIds.get(j))
                        .sortOrder(j)
                        .isCoverImage(j == 0)
                        .build());
            }

            pgImagesList.add(PGImages.builder().pgId(pgId).images(images).build());
        }

        pgImagesRepository.saveAll(pgImagesList);
    }

    private List<String> urls(String... unsplashPhotoIds) {
        return Arrays.stream(unsplashPhotoIds)
                .map(id -> "https://images.unsplash.com/photo-" + id + "?auto=format&fit=crop&w=600&q=80")
                .collect(java.util.stream.Collectors.toList());
    }

    // Maps the old free-text amenity labels used by this seed data onto the
    // new fixed Amenity enum. Labels with no equivalent in the fixed set
    // (Power Backup, CCTV, Gym, Swimming Pool, Study Table, Wardrobe,
    // Security, Attached Bathroom, Shuttle Service, Balcony) are dropped —
    // there's no reasonable 1:1 mapping for them among the 8 fixed values.
    private List<Amenity> amenitiesOf(String... labels) {
        return Arrays.stream(labels)
                .map(this::toAmenity)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    // Mirrors the old rentByRoomType scaling (SINGLE=base, DOUBLE=85%, TRIPLE=70%) —
    // FOUR_SHARING is dropped since PG.sharingType no longer supports it. Room
    // counts and deposits are new fields the old flat rent fields never had;
    // deposit defaults to rent (same fallback BookingServiceImpl used to apply
    // when securityDeposit was null, which every seeded PG left unset anyway).
    private List<SharingType> sharingTypes(double baseRent) {
        return List.of(
                SharingType.builder().type(RoomSharingType.SINGLE).rent(baseRent).deposit(baseRent).count(4).occupiedCount(0).build(),
                SharingType.builder().type(RoomSharingType.DOUBLE).rent(baseRent * 0.85).deposit(baseRent * 0.85).count(4).occupiedCount(0).build(),
                SharingType.builder().type(RoomSharingType.TRIPLE).rent(baseRent * 0.70).deposit(baseRent * 0.70).count(4).occupiedCount(0).build()
        );
    }

    private Amenity toAmenity(String label) {
        switch (label) {
            case "WiFi": return Amenity.WIFI;
            case "AC": return Amenity.AC;
            case "Food": return Amenity.FOOD;
            case "Laundry": return Amenity.LAUNDRY;
            case "Parking": return Amenity.PARKING;
            case "TV": return Amenity.TV;
            case "Geyser": return Amenity.GEYSER;
            case "Housekeeping": return Amenity.HOUSEKEEPING;
            default: return null;
        }
    }
}
