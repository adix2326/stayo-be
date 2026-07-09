package com.stayo.stayo.dashboard.service.impl;

import com.stayo.stayo.content.entity.Banner;
import com.stayo.stayo.content.entity.DashboardCategory;
import com.stayo.stayo.content.entity.PopularSearch;
import com.stayo.stayo.content.entity.QuickFilter;
import com.stayo.stayo.content.service.BannerService;
import com.stayo.stayo.content.service.CategoryService;
import com.stayo.stayo.content.service.PopularSearchService;
import com.stayo.stayo.content.service.QuickFilterService;
import com.stayo.stayo.dashboard.assembler.DashboardAssembler;
import com.stayo.stayo.dashboard.dto.DashboardResponseDTO;
import com.stayo.stayo.dashboard.service.DashboardService;
import com.stayo.stayo.notification.service.NotificationService;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.service.NearbyPGService;
import com.stayo.stayo.property.service.RecommendationService;
import com.stayo.stayo.shared.exception.ProfileNotCompletedException;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BannerService bannerService;
    private final PopularSearchService popularSearchService;
    private final QuickFilterService quickFilterService;
    private final CategoryService categoryService;
    private final NearbyPGService nearbyPGService;
    private final RecommendationService recommendationService;
    private final DashboardAssembler dashboardAssembler;

    @Override
    public DashboardResponseDTO getDashboard(String userId) {
        log.info("Request to load user dashboard for ID: {}", userId);
        long startTime = System.currentTimeMillis();

        // 1. Get current user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Security validation: Profile must be completed
        if (!user.isProfileCompleted()) {
            log.warn("Access denied for user {}: Profile is not completed", userId);
            throw new ProfileNotCompletedException("PROFILE_NOT_COMPLETED");
        }

        // 3. Fetch dashboard components in parallel
        String userCity = user.getCity();

        CompletableFuture<Integer> notificationFuture = CompletableFuture.supplyAsync(
                () -> notificationService.getUnreadNotificationCount(userId)
        );
        CompletableFuture<List<Banner>> bannersFuture = CompletableFuture.supplyAsync(
                () -> bannerService.getActiveBanners()
        );
        CompletableFuture<List<PopularSearch>> popularSearchesFuture = CompletableFuture.supplyAsync(
                () -> popularSearchService.getPopularSearches()
        );
        CompletableFuture<List<QuickFilter>> quickFiltersFuture = CompletableFuture.supplyAsync(
                () -> quickFilterService.getQuickFilters()
        );
        CompletableFuture<List<DashboardCategory>> categoriesFuture = CompletableFuture.supplyAsync(
                () -> categoryService.getCategories()
        );
        CompletableFuture<List<PG>> nearbyPropertiesFuture = CompletableFuture.supplyAsync(
                () -> nearbyPGService.getNearbyPGs(userCity)
        );
        CompletableFuture<List<PG>> recommendedPropertiesFuture = CompletableFuture.supplyAsync(
                () -> recommendationService.getRecommendedPGs(userId)
        );

        // Wait for all parallel fetches to complete
        CompletableFuture.allOf(
                notificationFuture,
                bannersFuture,
                popularSearchesFuture,
                quickFiltersFuture,
                categoriesFuture,
                nearbyPropertiesFuture,
                recommendedPropertiesFuture
        ).join();

        int notificationCount = notificationFuture.join();
        List<Banner> banners = bannersFuture.join();
        List<PopularSearch> popularSearches = popularSearchesFuture.join();
        List<QuickFilter> quickFilters = quickFiltersFuture.join();
        List<DashboardCategory> categories = categoriesFuture.join();
        List<PG> nearbyPGs = nearbyPropertiesFuture.join();
        List<PG> recommendedPGs = recommendedPropertiesFuture.join();

        // 4. Assemble DTO
        DashboardResponseDTO response = dashboardAssembler.assemble(
                user,
                notificationCount,
                popularSearches,
                banners,
                quickFilters,
                categories,
                nearbyPGs,
                recommendedPGs
        );

        long duration = System.currentTimeMillis() - startTime;
        log.info("Dashboard successfully loaded for user {} in {}ms (Optimized)", userId, duration);

        return response;
    }
}
