package com.stayo.stayo.property.service.impl;

import com.stayo.stayo.common.exception.ProfileNotCompletedException;
import com.stayo.stayo.common.exception.UserNotFoundException;
import com.stayo.stayo.property.dto.response.DashboardResponseDTO;
import com.stayo.stayo.property.entity.*;
import com.stayo.stayo.property.service.*;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import com.stayo.stayo.user.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BannerService bannerService;

    @Mock
    private PopularSearchService popularSearchService;

    @Mock
    private QuickFilterService quickFilterService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private NearbyPropertyService nearbyPropertyService;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private DashboardAssembler dashboardAssembler;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboard_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> dashboardService.getDashboard("test-user-id"));
    }

    @Test
    void getDashboard_ProfileNotCompleted_ThrowsException() {
        User user = User.builder()
                .id("test-user-id")
                .profileCompleted(false)
                .build();
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(user));

        ProfileNotCompletedException exception = assertThrows(ProfileNotCompletedException.class, () ->
                dashboardService.getDashboard("test-user-id")
        );
        assertEquals("PROFILE_NOT_COMPLETED", exception.getMessage());
    }

    @Test
    void getDashboard_Success() {
        User user = User.builder()
                .id("test-user-id")
                .profileCompleted(true)
                .city("Pune")
                .build();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(user));
        when(notificationService.getUnreadNotificationCount("test-user-id")).thenReturn(3);
        when(bannerService.getActiveBanners()).thenReturn(Collections.emptyList());
        when(popularSearchService.getPopularSearches()).thenReturn(Collections.emptyList());
        when(quickFilterService.getQuickFilters()).thenReturn(Collections.emptyList());
        when(categoryService.getCategories()).thenReturn(Collections.emptyList());
        when(nearbyPropertyService.getNearbyProperties("Pune")).thenReturn(Collections.emptyList());
        when(recommendationService.getRecommendedProperties("test-user-id")).thenReturn(Collections.emptyList());

        DashboardResponseDTO expectedDto = DashboardResponseDTO.builder().build();
        when(dashboardAssembler.assemble(eq(user), eq(3), anyList(), anyList(), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(expectedDto);

        DashboardResponseDTO response = dashboardService.getDashboard("test-user-id");

        assertNotNull(response);
        verify(userRepository, times(1)).findById("test-user-id");
        verify(notificationService, times(1)).getUnreadNotificationCount("test-user-id");
    }
}
