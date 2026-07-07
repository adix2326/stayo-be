package com.stayo.stayo.property.service.impl;

import com.stayo.stayo.common.exception.ProfileNotCompletedException;
import com.stayo.stayo.common.exception.UserNotFoundException;
import com.stayo.stayo.property.dto.response.DashboardResponseDTO;
import com.stayo.stayo.property.entity.*;
import com.stayo.stayo.property.service.*;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import com.stayo.stayo.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final NearbyPropertyService nearbyPropertyService;
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

        // 3. Fetch dashboard components
        int notificationCount = notificationService.getUnreadNotificationCount(userId);
        List<Banner> banners = bannerService.getActiveBanners();
        List<PopularSearch> popularSearches = popularSearchService.getPopularSearches();
        List<QuickFilter> quickFilters = quickFilterService.getQuickFilters();
        List<DashboardCategory> categories = categoryService.getCategories();
        
        // Fetch nearby properties based on user's city
        String userCity = user.getCity();
        List<Property> nearbyProperties = nearbyPropertyService.getNearbyProperties(userCity);

        // Fetch recommended properties
        List<Property> recommendedProperties = recommendationService.getRecommendedProperties(userId);

        // 4. Assemble DTO
        DashboardResponseDTO response = dashboardAssembler.assemble(
                user,
                notificationCount,
                popularSearches,
                banners,
                quickFilters,
                categories,
                nearbyProperties,
                recommendedProperties
        );

        long duration = System.currentTimeMillis() - startTime;
        log.info("Dashboard successfully loaded for user {} in {}ms", userId, duration);

        return response;
    }
}
